import SwiftUI
import MapKit
import PhotosUI
import ComposeApp

/// New or edit place, as the Mist-PlaceForm artboard draws it: the name, its category as chips, a
/// rating, whether it was visited, where it is (searched, with a small map), the description, its
/// photos (added from the library, removed with their x) and its trails. Each visit is changed from
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
    @State private var trails: [TrailRow] = []
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
        return IosForms.shared.withPhotos(base: withTrails, photos: photos)
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
                        if state?.isSearchingLocation == true { ProgressView() }
                    }
                    .padding(.horizontal, 16).frame(minHeight: 52)
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
                }
                photoSection
                trailSection
                FormCard { ToggleRow(title: "Make public", isOn: $isPublic) }
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
                if base == nil, s.loadError == nil, placeId == nil || s.existingLocation != nil {
                    seed(IosForms.shared.place(location: s.existingLocation, defaultCurrency: vm.defaultCurrency))
                }
                if s.isSaved { onSaved(); dismiss() }
            }
        }
    }

    // MARK: Photos

    /// The place's photos and the ones added here, each with an x, and a tile to add more.
    private var photoSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionLabel(text: "Photos", trailing: photos.isEmpty ? nil : "\(photos.count)")
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    PhotosPicker(selection: $picking, maxSelectionCount: 10, matching: .images) {
                        VStack(spacing: 6) {
                            if readingPhotos { ProgressView() } else { Image(systemName: "photo.badge.plus").font(.title2) }
                            Text("Add photos").font(.caption.weight(.semibold))
                        }
                        .foregroundStyle(Theme.accent)
                        .frame(width: 92, height: 92)
                        .mistCard(radius: 16)
                    }
                    .disabled(readingPhotos)
                    .accessibilityIdentifier("addPhotos")
                    ForEach(photos, id: \.uri) { photo in
                        PhotoThumb(photo: photo) { photos.removeAll { $0.uri == photo.uri } }
                    }
                }
                .padding(.vertical, 2)
            }
            .scrollClipDisabled()
        }
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
