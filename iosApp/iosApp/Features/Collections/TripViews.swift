import SwiftUI
import MapKit
import ComposeApp

// The trip's other views, as Android and the web have them: everything on a map, the trip in
// figures, and what is nearby. None needs a request of its own except Nearby: a collection
// arrives with its places, stays and journeys inside it.

// MARK: - Map

/// The trip's places, stays and the ends of its journeys on one map, each kind in its colour.
struct TripMap: View {
    let collection: ComposeApp.Collection

    private struct Pin: Identifiable {
        let id: String
        let title: String
        let coordinate: CLLocationCoordinate2D
        let systemImage: String
        let tint: Color
    }

    private var pins: [Pin] {
        func spot(_ lat: String?, _ lon: String?) -> CLLocationCoordinate2D? {
            guard let lat = lat.flatMap(Double.init), let lon = lon.flatMap(Double.init) else { return nil }
            return CLLocationCoordinate2D(latitude: lat, longitude: lon)
        }
        var pins: [Pin] = []
        for place in collection.locations {
            if let c = spot(place.latitude, place.longitude) {
                pins.append(Pin(id: "p\(place.id)", title: place.name, coordinate: c,
                                systemImage: place.isVisited ? "checkmark" : "mappin", tint: Theme.accent))
            }
        }
        for stay in collection.lodging {
            if let c = spot(stay.latitude, stay.longitude) {
                pins.append(Pin(id: "l\(stay.id)", title: stay.name, coordinate: c, systemImage: "bed.double.fill",
                                tint: Theme.Tone.coral.ink))
            }
        }
        for leg in collection.transportations {
            if let c = spot(leg.originLatitude, leg.originLongitude) {
                pins.append(Pin(id: "o\(leg.id)", title: leg.name, coordinate: c, systemImage: "arrow.up.right",
                                tint: Theme.Tone.blue.ink))
            }
            if let c = spot(leg.destinationLatitude, leg.destinationLongitude) {
                pins.append(Pin(id: "d\(leg.id)", title: leg.name, coordinate: c, systemImage: "flag.fill",
                                tint: Theme.Tone.blue.ink))
            }
        }
        return pins
    }

    var body: some View {
        let pins = pins
        if pins.isEmpty {
            ContentUnavailableView("Nothing on the map yet", systemImage: "map",
                                   description: Text("Places, stays and journeys with a location show up here."))
                .padding(.top, 40)
        } else {
            VStack(alignment: .leading, spacing: 10) {
                Map(initialPosition: .automatic) {
                    ForEach(pins) { pin in
                        Marker(pin.title, systemImage: pin.systemImage, coordinate: pin.coordinate).tint(pin.tint)
                    }
                }
                .mapControls { MapCompass() }
                .frame(height: 460)
                .clipShape(.rect(cornerRadius: Theme.Radius.card, style: .continuous))
                HStack(spacing: 14) {
                    legend("Places", Theme.accent)
                    legend("Stays", Theme.Tone.coral.ink)
                    legend("Journeys", Theme.Tone.blue.ink)
                }
                .padding(.horizontal, 6)
            }
        }
    }

    private func legend(_ text: String, _ tint: Color) -> some View {
        Label { Text(text) } icon: { Circle().fill(tint).frame(width: 8, height: 8) }
            .font(.footnote).foregroundStyle(Theme.muted)
    }
}

// MARK: - Stats

/// The trip in figures, from the shared arithmetic Android uses, so both say the same.
struct TripStats: View {
    let collection: ComposeApp.Collection

    var body: some View {
        let s = collection.stats()
        VStack(alignment: .leading, spacing: 16) {
            VStack(alignment: .leading, spacing: 10) {
                HStack(alignment: .firstTextBaseline) {
                    Text("\(s.placesVisited) of \(s.placesTotal)").font(.title2.weight(.bold)).foregroundStyle(Theme.ink)
                    Text("places visited").foregroundStyle(Theme.muted)
                    Spacer()
                    if s.isComplete { ToneTag(text: "Complete", tone: .green, systemImage: "checkmark") }
                }
                ProgressView(value: Double(s.visitedFraction)).tint(Theme.accent)
            }
            .padding(16)
            .mistCard()

            LazyVGrid(columns: [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)], spacing: 10) {
                tile("Days", "\(s.totalDays)", "calendar", .amber, detail: s.activeDays > 0 ? "\(s.activeDays) with plans" : nil)
                tile("Visits", "\(s.visits)", "figure.walk", .green)
                tile("Nights", "\(s.nights)", "moon.stars", .coral, detail: "\(s.stays) \(s.stays == 1 ? "stay" : "stays")")
                tile("Journeys", "\(s.transportations)", "airplane", .blue)
                tile("Photos", "\(s.photos)", "photo", .mint)
                tile("Travellers", "\(s.travellers)", "person.2", .teal)
                tile("Notes", "\(s.notes)", "note.text", .amber)
                tile("Lists", "\(s.checklists)", "checklist", .green)
            }

            if !s.countries.isEmpty || !s.regions.isEmpty || !s.cities.isEmpty {
                SectionLabel(text: "Where you have been")
                VStack(alignment: .leading, spacing: 0) {
                    geography("Countries", s.countries)
                    geography("Regions", s.regions)
                    geography("Cities", s.cities)
                }
                .mistCard()
            }
            Text("Photos and places count only what you have visited, as the web does.")
                .font(.footnote).foregroundStyle(Theme.faint).padding(.horizontal, 6)
        }
    }

    private func tile(_ title: String, _ value: String, _ icon: String, _ tone: Theme.Tone, detail: String? = nil) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            ToneBadge(systemImage: icon, tone: tone, size: 30)
            Text(value).font(.title2.weight(.bold)).foregroundStyle(Theme.ink).monospacedDigit()
            Text(detail.map { "\(title) · \($0)" } ?? title).font(.footnote).foregroundStyle(Theme.muted).lineLimit(1)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .mistCard()
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private func geography(_ title: String, _ names: [String]) -> some View {
        if !names.isEmpty {
            VStack(alignment: .leading, spacing: 4) {
                Text("\(title) · \(names.count)").font(.footnote.weight(.semibold)).foregroundStyle(Theme.muted)
                Text(names.joined(separator: ", ")).foregroundStyle(Theme.ink)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 16).padding(.vertical, 12)
        }
    }
}

// MARK: - Nearby

/// What is around one of the trip's places, or around a place typed in: OpenStreetMap's points of
/// interest through the server. Each can become a place in the trip with one tap.
struct TripNearby: View {
    let collection: ComposeApp.Collection
    let onAdded: () -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state = RecommendationsUiState(anchorLocationId: nil, query: "", category: .tourism, radiusMetres: 5000,
                                                      results: [], isSearching: false, hasSearched: false, errorMessage: nil,
                                                      added: [], addingId: nil)
    @State private var query = ""

    private var vm: RecommendationsViewModel { host.owner.recommendationsViewModel() }

    private var anchors: [Location] { collection.locations.filter { $0.latitude != nil && $0.longitude != nil } }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            FormCard {
                Menu {
                    Button("A place I type") { vm.onAnchorSelected(locationId: nil) }
                    ForEach(anchors, id: \.id) { place in
                        Button(place.name) { vm.onAnchorSelected(locationId: place.id) }
                    }
                } label: {
                    HStack {
                        Text("Around").foregroundStyle(Theme.ink)
                        Spacer()
                        Text(anchorName).foregroundStyle(Theme.accent).lineLimit(1)
                        Image(systemName: "chevron.up.chevron.down").font(.footnote).foregroundStyle(Theme.accent)
                    }
                    .padding(.horizontal, 16).frame(minHeight: 50)
                }
                if state.anchorLocationId == nil {
                    FormDivider()
                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass").foregroundStyle(Theme.accent)
                        TextField("A town, a street, a landmark", text: $query)
                            .submitLabel(.search)
                            .onSubmit(search)
                    }
                    .padding(.horizontal, 16).frame(minHeight: 50)
                    .onChange(of: query) { _, text in vm.onQueryChanged(query: text) }
                }
            }
            ChoiceChips(
                options: RecommendationCategory.allCases.map { ($0.wireName, $0.label) },
                selection: Binding(get: { state.category.wireName },
                                   set: { wire in
                                       if let c = RecommendationCategory.allCases.first(where: { $0.wireName == wire }) {
                                           vm.onCategorySelected(category: c)
                                       }
                                   })
            )
            Picker("Radius", selection: Binding(get: { Int(state.radiusMetres) }, set: { vm.onRadiusSelected(radiusMetres: Int32($0)) })) {
                Text("1 km").tag(1000)
                Text("5 km").tag(5000)
                Text("10 km").tag(10000)
                Text("25 km").tag(25000)
            }
            .pickerStyle(.segmented)
            Button(action: search) {
                HStack {
                    if state.isSearching { ProgressView().tint(.white) }
                    Text("Search").font(.headline)
                }
                .frame(maxWidth: .infinity, minHeight: 48)
            }
            .buttonStyle(.glassProminent)
            .tint(Theme.accent)
            .disabled(state.isSearching || (state.anchorLocationId == nil && query.trimmingCharacters(in: .whitespaces).isEmpty))

            if let error = state.errorMessage {
                Label(error, systemImage: "exclamationmark.circle.fill")
                    .font(.footnote).foregroundStyle(Theme.Tone.coral.ink)
            }
            if state.hasSearched, state.results.isEmpty, state.errorMessage == nil, !state.isSearching {
                ContentUnavailableView("Nothing found", systemImage: "binoculars",
                                       description: Text("Try a wider radius or another kind."))
            }
            if !state.results.isEmpty {
                SectionLabel(text: "Found", trailing: "\(state.results.count)")
                VStack(spacing: 0) {
                    ForEach(Array(state.results.enumerated()), id: \.element.id) { index, result in
                        if index > 0 { FormDivider(inset: 16) }
                        row(result)
                    }
                }
                .mistCard()
            }
        }
        .task { for await s in vm.uiState { state = s } }
    }

    private var anchorName: String {
        guard let id = state.anchorLocationId else { return "A place I type" }
        return anchors.first { $0.id == id }?.name ?? "A place I type"
    }

    private func search() {
        let anchor = state.anchorLocationId.flatMap { id in anchors.first { $0.id == id } }
        if let anchor, let lat = anchor.latitude.flatMap(Double.init), let lon = anchor.longitude.flatMap(Double.init) {
            vm.search(anchor: KotlinPair(first: KotlinDouble(value: lat), second: KotlinDouble(value: lon)))
        } else {
            vm.search(anchor: nil)
        }
    }

    private func row(_ r: Recommendation) -> some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 3) {
                Text(r.name).font(.body.weight(.medium)).foregroundStyle(Theme.ink)
                if let address = r.address, !address.isEmpty {
                    Text(address).font(.footnote).foregroundStyle(Theme.muted).lineLimit(2)
                }
                HStack(spacing: 8) {
                    if let km = r.distanceKm?.doubleValue {
                        Text(km < 1 ? "\(Int(km * 1000)) m" : km.formatted(.number.precision(.fractionLength(1))) + " km")
                    }
                    if let type = r.primaryType, !type.isEmpty { Text(type.replacingOccurrences(of: "_", with: " ").capitalized) }
                }
                .font(.caption).foregroundStyle(Theme.faint)
            }
            Spacer(minLength: 8)
            if state.added.contains(r.id) {
                Image(systemName: "checkmark.circle.fill").font(.title2).foregroundStyle(Theme.Tone.green.ink)
                    .accessibilityLabel("Added")
            } else if state.addingId == r.id {
                ProgressView()
            } else {
                Button { vm.addAsPlace(recommendation: r, collectionId: collection.id) { _ in onAdded() } } label: {
                    Image(systemName: "plus.circle.fill").font(.title2)
                }
                .buttonStyle(.plain)
                .foregroundStyle(Theme.accent)
                .disabled(state.addingId != nil)
                .accessibilityLabel("Add \(r.name) to the trip")
            }
        }
        .padding(.horizontal, 16).padding(.vertical, 12)
    }
}
