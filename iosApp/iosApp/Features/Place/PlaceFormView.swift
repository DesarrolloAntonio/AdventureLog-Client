import SwiftUI
import MapKit
import PhotosUI
import ComposeApp

/// New or edit place, as the Mist-PlaceForm artboard draws it: the name, its category as chips, a
/// rating, whether it was visited, where it is (searched, with a small map), the description, its
/// website, price and tags, its photos (from the library, the camera, a link or Wikipedia, removed with their x) and its trails. Each visit is changed from
/// the place itself.
struct PlaceFormView: View {
    var placeId: String?
    /// A new place made from a trip goes into it.
    var intoCollectionId: String?
    let onSaved: () -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state: AddEditAdventureUiState?
    @State private var base: LocationFormData?
    @State private var initial: LocationFormData?
    @State private var name = ""
    @State private var about = ""
    @State private var category: ComposeApp.Category?
    @State private var rating = 0
    @State private var visited = false
    @State private var lockedVisited = false
    @State private var isPublic = false
    @State private var whereText = ""
    @State private var latitude: String?
    @State private var longitude: String?
    @State private var searching = false
    /// The text a picked result put in the field, which is not a new search.
    @State private var picked: String?
    @FocusState private var whereFocused: Bool
    @State private var photos: [ImageFormData] = []
    @State private var picking: [PhotosPickerItem] = []
    @State private var readingPhotos = false
    /// Where a new photo comes from, besides the library: the camera, a link, Wikipedia.
    @State private var pickingLibrary = false
    @State private var takingPhoto = false
    @State private var addingLink = false
    @State private var photoLink = ""
    @State private var photoNote: String?
    @State private var trails: [TrailRow] = []
    @State private var link = ""
    @State private var price = ""
    @State private var currency = "USD"
    @State private var tags: [String] = []
    @State private var newTag = ""
    /// The New Category alert, and the name of one just created, to pick once the list has it.
    @State private var creatingCategory = false
    @State private var newCategoryName = ""
    @State private var newCategoryIcon = ""
    @State private var pendingCategory: String?
    /// The map picker, and whether the address of a spot picked on it is on its way.
    @State private var pickingOnMap = false
    @State private var awaitingAddress = false
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditAdventureViewModel { host.owner.addEditAdventureViewModel(adventureId: placeId) }

    private var form: LocationFormData? {
        guard let base else { return nil }
        let edited = IosForms.shared.place(base: base, name: name, description: about, category: category, rating: Int32(rating),
                                           location: whereText, latitude: latitude, longitude: longitude, isPublic: isPublic,
                                           visited: visited, today: ISODates.format(.now, withTime: false),
                                           timezone: TimeZone.current.identifier)
        let withTrails = IosForms.shared.withTrails(base: edited, trails: trails.map {
            IosForms.shared.trail(id: $0.serverId, name: $0.name.trimmingCharacters(in: .whitespaces), link: $0.url)
        })
        let withPhotos = IosForms.shared.withPhotos(base: withTrails, photos: photos)
        return IosForms.shared.withDetails(base: withPhotos, link: link, price: price, currency: currency, tags: tags)
    }

    var body: some View {
        FormScaffold(
            title: placeId == nil ? "New Place" : "Edit Place",
            canSave: base != nil && !name.trimmingCharacters(in: .whitespaces).isEmpty && !readingPhotos
                && !trails.contains(where: \.isHalf),
            saving: state?.isSavingLocation ?? false,
            dirty: form != nil && form != initial,
            error: base == nil ? nil : state?.errorMessage,
            clearError: { vm.clearError() },
            save: { if let form { vm.saveLocation(formData: form, intoCollectionId: intoCollectionId) } }
        ) {
            if base == nil {
                if let error = state?.loadError {
                    ContentUnavailableView("Couldn't load the place", systemImage: "wifi.exclamationmark", description: Text(error))
                    Button("Try again") { vm.retryLoad() }.buttonStyle(.glassProminent)
                } else {
                    ProgressView().frame(maxWidth: .infinity).padding(.top, 60)
                }
            } else {
                FormCard {
                    FormField(placeholder: "Name of the place", text: $name, font: .title3.weight(.semibold))
                }
                if let categories = state?.categories, !categories.isEmpty {
                    SectionLabel(text: "Category").padding(.bottom, -8)
                    ChoiceChips(
                        options: categories.map { ($0.id, "\($0.icon) \($0.displayName)") },
                        selection: Binding(get: { category?.id ?? "" },
                                           set: { id in category = categories.first { $0.id == id } })
                    )
                }
                Button { newCategoryName = ""; newCategoryIcon = ""; creatingCategory = true } label: {
                    Label("New Category", systemImage: "plus")
                        .font(.subheadline.weight(.semibold))
                }
                .buttonStyle(.plain)
                .foregroundStyle(Theme.accent)
                .padding(.horizontal, 6)
                .accessibilityIdentifier("newCategory")
                FormCard {
                    HStack {
                        Text("Rating").foregroundStyle(Theme.ink)
                        Spacer()
                        Stars(value: $rating)
                    }
                    .padding(.horizontal, 16).frame(minHeight: 52)
                    FormDivider()
                    ToggleRow(title: "Visited", isOn: $visited).disabled(lockedVisited)
                }
                SectionLabel(text: "Where it is").padding(.bottom, -8)
                FormCard {
                    HStack(spacing: 10) {
                        Image(systemName: "mappin.and.ellipse").foregroundStyle(Theme.accent)
                        TextField("Search for a place or address", text: $whereText)
                            .focused($whereFocused)
                            .submitLabel(.search)
                        if state?.isSearchingLocation == true || awaitingAddress { ProgressView() }
                    }
                    .padding(.horizontal, 16).frame(minHeight: 52)
                    FormDivider(inset: 44)
                    Button { whereFocused = false; pickingOnMap = true } label: {
                        Label("Pick on the Map", systemImage: "map")
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 16).frame(minHeight: 48)
                    }
                    .buttonStyle(.plain)
                    .foregroundStyle(Theme.accent)
                    .accessibilityIdentifier("pickOnMap")
                    if searching, let results = state?.locationSearchResults, !results.isEmpty {
                        ForEach(Array(results.prefix(5).enumerated()), id: \.offset) { _, result in
                            FormDivider(inset: 44)
                            Button { pick(result) } label: {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(result.name).foregroundStyle(Theme.ink)
                                    Text(result.displayName).font(.footnote).foregroundStyle(Theme.muted).lineLimit(2)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.leading, 44).padding(.trailing, 16).padding(.vertical, 10)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    if let coordinate {
                        Map(position: .constant(.region(MKCoordinateRegion(center: coordinate, latitudinalMeters: 4000, longitudinalMeters: 4000))),
                            interactionModes: []) {
                            Marker(name.isEmpty ? "Here" : name, coordinate: coordinate).tint(Theme.accent)
                        }
                        .frame(height: 150)
                        .clipShape(.rect(cornerRadius: 14))
                        .padding(8)
                        .accessibilityLabel("Map preview of \(whereText.isEmpty ? name : whereText)")
                    }
                }
                SectionLabel(text: "Description").padding(.bottom, -8)
                FormCard {
                    FormField(placeholder: "What made it worth the trip?", text: $about, axis: .vertical)
                    FormDivider()
                    // The server writes it from the Wikipedia article of that name, as Android and the web do.
                    Button { vm.generateDescription(name: name) { text in about = text } } label: {
                        HStack(spacing: 8) {
                            if state?.isGeneratingDescription == true { ProgressView() } else { Image(systemName: "book.pages") }
                            Text(about.isEmpty ? "Write It from Wikipedia" : "Replace with Wikipedia")
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 16).frame(minHeight: 48)
                    }
                    .buttonStyle(.plain)
                    .foregroundStyle(Theme.accent)
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty || state?.isGeneratingDescription == true)
                    .accessibilityIdentifier("wikipedia")
                }
                detailSection
                tagSection
                photoSection
                trailSection
                FormCard { ToggleRow(title: "Make public", isOn: $isPublic) }
            }
        }
        .alert("New Category", isPresented: $creatingCategory) {
            TextField("Name", text: $newCategoryName)
            TextField("Emoji (optional)", text: $newCategoryIcon)
            Button("Cancel", role: .cancel) {}
            Button("Create") {
                let name = newCategoryName.trimmingCharacters(in: .whitespaces)
                guard !name.isEmpty else { return }
                pendingCategory = name
                let icon = newCategoryIcon.trimmingCharacters(in: .whitespaces)
                vm.createCategory(name: name, icon: icon.isEmpty ? "📍" : String(icon.prefix(2)))
            }
        } message: {
            Text("It is picked for this place once it exists.")
        }
        .sheet(isPresented: $pickingOnMap) {
            MapPicker(title: name.isEmpty ? "Here" : name, initial: coordinate) { spot in
                latitude = String(format: "%.6f", spot.latitude)
                longitude = String(format: "%.6f", spot.longitude)
                searching = false
                vm.clearLocationSearch()
                awaitingAddress = true
                vm.reverseGeocode(latitude: spot.latitude, longitude: spot.longitude)
            }
        }
        .onChange(of: picking) { _, items in
            guard !items.isEmpty else { return }
            picking = []
            Task { await add(items) }
        }
        .onChange(of: whereText) { _, text in
            guard whereFocused, text != picked else { return }
            searching = true
            vm.searchLocations(query: text)
        }
        .task {
            for await s in vm.uiState {
                state = s
                handleWikipedia(s.wikipediaImageState)
                if let pending = pendingCategory, let made = s.categories.first(where: { $0.displayName == pending }) {
                    category = made
                    pendingCategory = nil
                }
                if awaitingAddress, let address = s.reverseGeocodeResult?.displayName, !address.isEmpty {
                    awaitingAddress = false
                    picked = address
                    whereText = address
                }
                if s.errorMessage != nil { awaitingAddress = false; pendingCategory = nil }
                if base == nil, s.loadError == nil, placeId == nil || s.existingLocation != nil {
                    seed(IosForms.shared.place(location: s.existingLocation, defaultCurrency: vm.defaultCurrency))
                }
                if s.isSaved { NotificationCenter.default.post(name: .placeSaved, object: nil); onSaved(); dismiss() }
            }
        }
    }

    // MARK: Website, price and tags

    /// The website and what it cost, in the currency picked beside it.
    private var detailSection: some View {
        FormCard {
            HStack(spacing: 10) {
                Image(systemName: "link").foregroundStyle(Theme.accent).frame(width: 22)
                TextField("Website", text: $link)
                    .keyboardType(.URL)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
            }
            .padding(.horizontal, 16).frame(minHeight: 50)
            FormDivider()
            HStack(spacing: 10) {
                Image(systemName: "creditcard").foregroundStyle(Theme.accent).frame(width: 22)
                TextField("Price", text: $price).keyboardType(.decimalPad)
                Menu {
                    ForEach(Currencies.shared.options, id: \.first) { pair in
                        Button("\(pair.first ?? "") · \(pair.second ?? "")") { currency = String(pair.first ?? "") }
                    }
                } label: {
                    HStack(spacing: 4) {
                        Text(currency).monospaced()
                        Image(systemName: "chevron.up.chevron.down").font(.caption2)
                    }
                    .font(.subheadline.weight(.semibold))
                }
                .accessibilityLabel("Currency, \(currency)")
            }
            .padding(.horizontal, 16).frame(minHeight: 50)
        }
    }

    /// Each tag as a chip with an x, and a field that adds one on return.
    private var tagSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionLabel(text: "Tags", trailing: tags.isEmpty ? nil : "\(tags.count)")
            FormCard {
                if !tags.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(tags, id: \.self) { tag in
                                HStack(spacing: 6) {
                                    Text(tag).font(.subheadline)
                                    Button { tags.removeAll { $0 == tag } } label: {
                                        Image(systemName: "xmark").font(.caption2.weight(.bold))
                                    }
                                    .buttonStyle(.plain)
                                    .accessibilityLabel("Remove \(tag)")
                                }
                                .padding(.horizontal, 12).padding(.vertical, 7)
                                .background(Theme.Tone.teal.fill, in: .capsule)
                                .foregroundStyle(Theme.Tone.teal.ink)
                            }
                        }
                        .padding(.horizontal, 16).padding(.top, 12)
                    }
                }
                HStack(spacing: 10) {
                    Image(systemName: "tag").foregroundStyle(Theme.accent).frame(width: 22)
                    TextField("Add a tag", text: $newTag)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .submitLabel(.done)
                        .onSubmit(addTag)
                    if !newTag.trimmingCharacters(in: .whitespaces).isEmpty {
                        Button("Add", action: addTag).font(.subheadline.weight(.semibold))
                    }
                }
                .padding(.horizontal, 16).frame(minHeight: 50)
            }
        }
    }

    private func addTag() {
        let tag = newTag.trimmingCharacters(in: .whitespaces)
        if !tag.isEmpty, !tags.contains(tag) { tags.append(tag) }
        newTag = ""
    }

    // MARK: Photos

    /// The place's photos and the ones added here, each with an x, and a tile to add more.
    private var photoSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionLabel(text: "Photos", trailing: photos.isEmpty ? nil : "\(photos.count)")
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    Menu {
                        Button("Photo Library", systemImage: "photo.on.rectangle") { pickingLibrary = true }
                        if UIImagePickerController.isSourceTypeAvailable(.camera) {
                            Button("Take Photo", systemImage: "camera") { takingPhoto = true }
                        }
                        Button("From a Link", systemImage: "link") { photoLink = ""; addingLink = true }
                        Button("From Wikipedia", systemImage: "book.pages") {
                            vm.searchWikipediaImage(query: name.trimmingCharacters(in: .whitespaces))
                        }
                        .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                    } label: {
                        VStack(spacing: 6) {
                            if readingPhotos || searchingWikipedia { ProgressView() } else { Image(systemName: "photo.badge.plus").font(.title2) }
                            Text("Add photos").font(.caption.weight(.semibold))
                        }
                        .foregroundStyle(Theme.accent)
                        .frame(width: 92, height: 92)
                        .mistCard(radius: 16)
                    }
                    .disabled(readingPhotos || searchingWikipedia)
                    .accessibilityIdentifier("addPhotos")
                    ForEach(photos, id: \.uri) { photo in
                        PhotoThumb(photo: photo) { photos.removeAll { $0.uri == photo.uri } }
                    }
                }
                .padding(.vertical, 2)
            }
            .scrollClipDisabled()
        }
        .photosPicker(isPresented: $pickingLibrary, selection: $picking, maxSelectionCount: 10, matching: .images)
        .fullScreenCover(isPresented: $takingPhoto) {
            CameraPicker { image in addPhoto(image) }.ignoresSafeArea()
        }
        .alert("Photo from a Link", isPresented: $addingLink) {
            TextField("https://", text: $photoLink)
                .keyboardType(.URL)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
            Button("Cancel", role: .cancel) {}
            Button("Add") { addLink() }
        } message: {
            Text("The link of an image on the web. It is downloaded when you save.")
        }
        .alert(photoNote ?? "", isPresented: Binding(get: { photoNote != nil }, set: { if !$0 { photoNote = nil } })) {
            Button("OK", role: .cancel) {}
        }
    }

    private var searchingWikipedia: Bool {
        if let state = state?.wikipediaImageState, case .loading = onEnum(of: state) { return true }
        return false
    }

    private func addLink() {
        let link = photoLink.trimmingCharacters(in: .whitespaces)
        guard let url = URL(string: link), ["http", "https"].contains(url.scheme?.lowercased() ?? ""), url.host() != nil else {
            photoNote = "That is not a web link. It should start with https://"
            return
        }
        guard !photos.contains(where: { $0.uri == link }) else { return }
        photos.append(IosForms.shared.webPhoto(url: link, wikipedia: false))
    }

    /// Wikipedia's answer for the place's name: its photo, or why there is none.
    private func handleWikipedia(_ result: WikipediaImageResult) {
        switch onEnum(of: result) {
        case .success(let found):
            if !photos.contains(where: { $0.uri == found.imageUrl }) {
                photos.append(IosForms.shared.webPhoto(url: found.imageUrl, wikipedia: true))
            }
            vm.resetWikipediaImageState()
        case .notFound:
            photoNote = "Wikipedia has no photo for \(name)."
            vm.resetWikipediaImageState()
        case .error(let e):
            photoNote = e.message
            vm.resetWikipediaImageState()
        case .idle, .loading:
            break
        }
    }

    private func addPhoto(_ image: UIImage) {
        guard let jpeg = image.jpegData(compressionQuality: 0.85) else { return }
        let file = FileManager.default.temporaryDirectory.appendingPathComponent("\(UUID().uuidString).jpg")
        guard (try? jpeg.write(to: file)) != nil else { return }
        photos.append(IosForms.shared.localPhoto(fileUrl: file.absoluteString))
    }

    /// Writes each picked photo to a JPEG in the temporary folder, which the upload reads on save.
    private func add(_ items: [PhotosPickerItem]) async {
        readingPhotos = true
        defer { readingPhotos = false }
        for item in items {
            guard let data = try? await item.loadTransferable(type: Data.self),
                  let jpeg = UIImage(data: data)?.jpegData(compressionQuality: 0.85) else { continue }
            let file = FileManager.default.temporaryDirectory.appendingPathComponent("\(UUID().uuidString).jpg")
            guard (try? jpeg.write(to: file)) != nil else { continue }
            photos.append(IosForms.shared.localPhoto(fileUrl: file.absoluteString))
        }
    }

    // MARK: Trails

    /// Each trail as a name and a link, removed with its minus, and a row to add one.
    private var trailSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionLabel(text: "Trails", trailing: trails.isEmpty ? nil : "\(trails.count)")
            FormCard {
                ForEach($trails) { $trail in
                    HStack(alignment: .top, spacing: 10) {
                        VStack(spacing: 0) {
                            TextField("Trail name", text: $trail.name)
                                .frame(minHeight: 44)
                            Rectangle().fill(Theme.separator).frame(height: 0.5)
                            TextField("Link (AllTrails, Wanderer...)", text: $trail.link)
                                .keyboardType(.URL)
                                .textInputAutocapitalization(.never)
                                .autocorrectionDisabled()
                                .frame(minHeight: 44)
                        }
                        Button { trails.removeAll { $0.id == trail.id } } label: {
                            Image(systemName: "minus.circle.fill").font(.title3).foregroundStyle(Theme.Tone.coral.ink)
                        }
                        .buttonStyle(.plain)
                        .padding(.top, 10)
                        .accessibilityLabel("Remove \(trail.name.isEmpty ? "trail" : trail.name)")
                    }
                    .padding(.horizontal, 16)
                    if trail.isHalf {
                        Text("A trail needs both a name and a link.")
                            .font(.footnote).foregroundStyle(Theme.Tone.coral.ink)
                            .padding(.horizontal, 16).padding(.bottom, 8)
                    }
                    FormDivider()
                }
                Button { trails.append(TrailRow(serverId: nil, name: "", link: "")) } label: {
                    Label("Add trail", systemImage: "plus")
                        .font(.subheadline.weight(.semibold))
                        .frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                        .padding(.horizontal, 16)
                }
                .buttonStyle(.plain)
                .foregroundStyle(Theme.accent)
                .accessibilityIdentifier("addTrail")
            }
        }
    }

    private var coordinate: CLLocationCoordinate2D? {
        guard let lat = latitude.flatMap(Double.init), let lon = longitude.flatMap(Double.init) else { return nil }
        return CLLocationCoordinate2D(latitude: lat, longitude: lon)
    }

    private func pick(_ result: GeocodeSearchResult) {
        searching = false
        whereFocused = false
        picked = result.displayName
        whereText = result.displayName
        latitude = result.latitude
        longitude = result.longitude
        vm.clearLocationSearch()
        if name.isEmpty { name = result.name }
    }

    private func seed(_ f: LocationFormData) {
        base = f
        name = f.name; about = f.description_; category = f.category; rating = Int(f.rating)
        whereText = f.location; latitude = f.latitude; longitude = f.longitude; isPublic = f.isPublic
        visited = !f.visits.isEmpty
        lockedVisited = f.visits.contains { $0.id != nil }
        photos = f.images
        link = f.link; price = f.price; currency = f.priceCurrency; tags = f.tags
        trails = f.trails.map { TrailRow(serverId: $0.id, name: $0.name, link: $0.link) }
        initial = form
    }
}

/// A trail while the form edits it; [serverId] is nil for one added here.
private struct TrailRow: Identifiable {
    let id = UUID()
    let serverId: String?
    var name: String
    var link: String

    /// The server keeps a trail only with both; one without the other would vanish on save.
    var isHalf: Bool {
        name.trimmingCharacters(in: .whitespaces).isEmpty != link.trimmingCharacters(in: .whitespaces).isEmpty
    }

    /// The link as typed, with https:// when it was left out ("alltrails.com/...").
    var url: String {
        let typed = link.trimmingCharacters(in: .whitespaces)
        return typed.isEmpty || typed.contains("://") ? typed : "https://" + typed
    }
}

/// A photo of the form: the server's through the signed-in image client, a picked one from its file.
private struct PhotoThumb: View {
    let photo: ImageFormData
    let onRemove: () -> Void

    var body: some View {
        Group {
            if photo.serverId != nil {
                RemoteImage(url: photo.uri) { Rectangle().fill(Theme.Tone.teal.fill) }
            } else if photo.type != .localFile, let url = URL(string: photo.uri) {
                AsyncImage(url: url) { image in image.resizable().scaledToFill() } placeholder: {
                    Rectangle().fill(Theme.Tone.teal.fill).overlay { ProgressView() }
                }
            } else if let url = URL(string: photo.uri), let image = UIImage(contentsOfFile: url.path) {
                Image(uiImage: image).resizable().scaledToFill()
            } else {
                Rectangle().fill(Theme.Tone.teal.fill)
            }
        }
        .frame(width: 92, height: 92)
        .clipShape(.rect(cornerRadius: 16, style: .continuous))
        .overlay(alignment: .topTrailing) {
            Button(action: onRemove) {
                Image(systemName: "xmark").font(.caption.weight(.bold)).frame(width: 26, height: 26)
            }
            .glassEffect(.regular.interactive(), in: .circle)
            .tint(.primary)
            .padding(5)
            .accessibilityLabel("Remove photo")
        }
        .overlay(alignment: .bottomLeading) {
            if photo.isPrimary {
                Text("Cover").font(.caption2.weight(.bold)).padding(.horizontal, 7).padding(.vertical, 3)
                    .glassEffect(.regular, in: .capsule).padding(5)
            }
        }
    }
}

/// Five stars to tap; tapping the one already set clears the rating.
struct Stars: View {
    @Binding var value: Int
    var body: some View {
        HStack(spacing: 4) {
            ForEach(1...5, id: \.self) { n in
                Button { value = value == n ? 0 : n } label: {
                    Image(systemName: n <= value ? "star.fill" : "star")
                        .foregroundStyle(n <= value ? Theme.Tone.amber.ink : Theme.glyph)
                }
                .buttonStyle(.plain)
            }
        }
        .font(.title3)
        .accessibilityElement()
        .accessibilityLabel("Rating")
        .accessibilityValue("\(value) of 5")
        .accessibilityAdjustableAction { direction in
            switch direction {
            case .increment: value = min(5, value + 1)
            case .decrement: value = max(0, value - 1)
            @unknown default: break
            }
        }
    }
}

/// The camera, for a photo taken now; the simulator has none, so the menu offers it only on a device.
private struct CameraPicker: UIViewControllerRepresentable {
    let onPhoto: (UIImage) -> Void
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ picker: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    final class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraPicker
        init(_ parent: CameraPicker) { self.parent = parent }

        func imagePickerController(_ picker: UIImagePickerController,
                                   didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let image = info[.originalImage] as? UIImage { parent.onPhoto(image) }
            parent.dismiss()
        }

        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) { parent.dismiss() }
    }
}
