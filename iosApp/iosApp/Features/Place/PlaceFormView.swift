import SwiftUI
import MapKit
import ComposeApp

/// New or edit place, as the Mist-PlaceForm artboard draws it: the name, its category as chips, a
/// rating, whether it was visited, where it is (searched, with a small map), and the description.
/// Photos, trails and visits beyond the first stay as they were; the web edits those.
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
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditAdventureViewModel { host.owner.addEditAdventureViewModel(adventureId: placeId) }

    private var form: LocationFormData? {
        guard let base else { return nil }
        return IosForms.shared.place(base: base, name: name, description: about, category: category, rating: Int32(rating),
                                     location: whereText, latitude: latitude, longitude: longitude, isPublic: isPublic,
                                     visited: visited, today: ISODates.format(.now, withTime: false),
                                     timezone: TimeZone.current.identifier)
    }

    var body: some View {
        FormScaffold(
            title: placeId == nil ? "New Place" : "Edit Place",
            canSave: base != nil && !name.trimmingCharacters(in: .whitespaces).isEmpty,
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
                FormCard { ToggleRow(title: "Make public", isOn: $isPublic) }
            }
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
        initial = form
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
