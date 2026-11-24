import SwiftUI
import MapKit
import ComposeApp

/// Places, as the iOS-Places artboard draws it: the places on a map, and a panel over its lower
/// half with the serif title, the All / Visited / Planned switch and the list.
///
/// The artboard's list is headed "In view" and follows the map. The list here is paged, so the
/// map only knows the places loaded so far; filtering by what is in view would silently drop the
/// rest, and the heading says how many places the switch selects instead.
struct PlacesView: View {
    @StateObject private var host = ViewModelHost()
    @State private var places: [Location] = []
    @State private var status = PagingStatus(isRefreshing: true, isLoadingMore: false, endReached: false, error: nil)
    @State private var counts: UserStats?
    @State private var filters: LocationFilters?
    @State private var selected: Location?
    @State private var creating = false
    @State private var showFilters = false
    @State private var expanded = false
    @State private var satellite = false
    @State private var camera: MapCameraPosition = .automatic
    /// Where the pins can be seen: the map's whole size, and how much the controls and the
    /// panel cover at its top and bottom.
    @State private var viewport = MapViewport()

    private var vm: LocationsViewModel { host.owner.locationsViewModel() }
    private var presenter: PagingListPresenter<Location> { host.owner.locationsPresenter() }

    var body: some View {
        GeometryReader { geo in
            let panelHeight = expanded ? geo.size.height - 8 : max(geo.size.height * 0.48, 320)
            ZStack(alignment: .bottom) {
                PlacesMap(places: places, satellite: satellite, camera: $camera) { selected = $0 }
                    .ignoresSafeArea()
                    .overlay(alignment: .top) { mapControls.padding(.horizontal, 16).padding(.top, 8) }

                panel
                    .frame(height: panelHeight)
                    .animation(.snappy, value: expanded)
            }
            .onChange(of: geo.size, initial: true) {
                let collapsed = max(geo.size.height * 0.48, 320)
                viewport = MapViewport(
                    size: CGSize(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom),
                    // The controls, plus a pin's height: a pin stands above its point.
                    top: geo.safeAreaInsets.top + 64 + 52,
                    bottom: collapsed + geo.safeAreaInsets.bottom
                )
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .navigationDestination(for: Location.self) { PlaceView(placeId: $0.id, name: $0.name) }
        .navigationDestination(item: $selected) { PlaceView(placeId: $0.id, name: $0.name) }
        .navigationDestination(isPresented: $creating) { ComingSoon(title: "New Place") }
        .sheet(isPresented: $showFilters) {
            if let filters {
                PlacesFilterSheet(vm: vm, initial: filters)
            }
        }
        .task { for await p in presenter.items { places = p } }
        .task { for await s in presenter.status { status = s } }
        .task { for await c in vm.libraryCounts { counts = c } }
        .task { for await f in vm.filters { filters = f } }
        // Set once the map is on screen: a camera given before it has laid out is dropped.
        .task(id: places.map(\.id)) {
            try? await Task.sleep(for: .milliseconds(400))
            withAnimation { camera = PlacesMap.fit(places, in: viewport) }
        }
    }

    // MARK: Map controls

    private var mapControls: some View {
        HStack(alignment: .top) {
            Button { showFilters = true } label: {
                Image(systemName: "line.3.horizontal.decrease")
                    .font(.system(size: 17, weight: .semibold))
                    .frame(width: 48, height: 48)
            }
            .glassEffect(.regular.interactive(), in: .circle)
            .overlay(alignment: .topTrailing) {
                if activeFilters > 0 {
                    Text("\(activeFilters)")
                        .font(.caption2.weight(.bold))
                        .foregroundStyle(.white)
                        .frame(minWidth: 18, minHeight: 18)
                        .background(Theme.accent, in: .circle)
                        .offset(x: 2, y: -2)
                }
            }
            .accessibilityLabel(activeFilters > 0 ? "Filters, \(activeFilters) on" : "Filters")

            Spacer()

            Button { satellite.toggle() } label: {
                Image(systemName: satellite ? "map" : "globe.europe.africa")
                    .font(.system(size: 17, weight: .semibold))
                    .frame(width: 48, height: 48)
            }
            .glassEffect(.regular.interactive(), in: .circle)
            .accessibilityLabel(satellite ? "Standard map" : "Satellite map")
        }
        .tint(.primary)
        .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
    }

    /// What the filter sheet has changed. The visit switch is on screen already, so unlike the
    /// ViewModel's count, it doesn't add to the badge.
    private var activeFilters: Int {
        guard let f = filters else { return 0 }
        var n = f.categoryNames.count
        if f.sortField != .updatedAt || f.sortDirection != .descending { n += 1 }
        if !f.includeCollections { n += 1 }
        return n
    }

    // MARK: Panel

    private var panel: some View {
        VStack(alignment: .leading, spacing: 0) {
            Capsule()
                .fill(.secondary.opacity(0.5))
                .frame(width: 36, height: 5)
                .frame(maxWidth: .infinity)
                .padding(.top, 8)
                .padding(.bottom, 10)
                .contentShape(.rect)
                .onTapGesture { expanded.toggle() }
                .accessibilityLabel(expanded ? "Show the map" : "Show the whole list")
                .accessibilityAddTraits(.isButton)

            HStack {
                Text("Places").font(Theme.display(34))
                Spacer()
                Button { creating = true } label: {
                    Image(systemName: "plus")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(width: 44, height: 44)
                        .background(Theme.accent, in: .circle)
                }
                .accessibilityLabel("New place")
            }
            .padding(.horizontal, 20)

            visitPicker
                .padding(.horizontal, 16)
                .padding(.top, 12)

            list
        }
        .background(.regularMaterial, in: UnevenRoundedRectangle(topLeadingRadius: 34, topTrailingRadius: 34))
        .shadow(color: .black.opacity(0.12), radius: 16, y: -2)
        .gesture(
            DragGesture(minimumDistance: 20).onEnded { g in
                if g.translation.height < -40 { expanded = true }
                if g.translation.height > 40 { expanded = false }
            }
        )
    }

    private var visitPicker: some View {
        Picker("Show", selection: Binding(
            get: { filters?.visitedFilter ?? .all },
            set: { v in
                guard let f = filters else { return }
                vm.onFiltersChanged(filters: f.doCopy(
                    categoryNames: f.categoryNames, sortField: f.sortField, sortDirection: f.sortDirection,
                    visitedFilter: v, includeCollections: f.includeCollections
                ))
            }
        )) {
            Text(label("All", counts.map { Int($0.locationCount) })).tag(VisitedFilter.all)
            Text(label("Visited", counts.map { Int($0.visitedLocationCount) })).tag(VisitedFilter.visited)
            Text(label("Planned", counts.map { Int($0.locationCount - $0.visitedLocationCount) })).tag(VisitedFilter.notVisited)
        }
        .pickerStyle(.segmented)
    }

    private func label(_ name: String, _ count: Int?) -> String {
        count.map { "\(name) · \($0)" } ?? name
    }

    private var isFiltered: Bool { activeFilters > 0 || (filters.map { $0.visitedFilter != .all } ?? false) }

    /// How many places the switch selects, from the library totals (the loaded page can't say).
    private var heading: String? {
        guard let counts, let filters else { return nil }
        let n: Int32 = switch filters.visitedFilter {
        case .visited: counts.visitedLocationCount
        case .notVisited: counts.locationCount - counts.visitedLocationCount
        default: counts.locationCount
        }
        // Categories narrow the list beyond what the totals know about.
        guard filters.categoryNames.isEmpty else { return nil }
        return n == 1 ? "1 PLACE" : "\(n) PLACES"
    }

    @ViewBuilder
    private var list: some View {
        if places.isEmpty, status.isRefreshing {
            ProgressView().controlSize(.large).frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if places.isEmpty, let error = status.error {
            ContentUnavailableView {
                Label("Couldn't load places", systemImage: "wifi.exclamationmark")
            } description: {
                Text(error)
            } actions: {
                Button("Try again") { presenter.retry() }.buttonStyle(.glassProminent)
            }
        } else if places.isEmpty {
            ContentUnavailableView {
                Label(isFiltered ? "No places match" : "No places yet", systemImage: "mappin.slash")
            } description: {
                Text(isFiltered ? "Try other filters." : "Places you add show up here and on the map.")
            } actions: {
                if isFiltered {
                    Button("Clear Filters") { vm.clearFilters() }.buttonStyle(.glass)
                }
            }
        } else {
            List {
                Section {
                    ForEach(Array(places.enumerated()), id: \.element.id) { index, place in
                        NavigationLink(value: place) { PlaceRow(place: place) }
                            .onAppear { presenter.itemAccessed(index: Int32(index)) }
                    }
                    if status.isLoadingMore {
                        ProgressView().frame(maxWidth: .infinity)
                    } else if let error = status.error {
                        Button("Couldn't load more. Try again") { presenter.retry() }
                            .accessibilityHint(error)
                    }
                } header: {
                    if let heading { Text(heading) }
                }
            }
            .listStyle(.insetGrouped)
            .scrollContentBackground(.hidden)
            .refreshable {
                presenter.refresh()
                vm.loadLibraryCounts()
            }
        }
    }
}

/// One place: its photo, name, "Category · Region", and its stars when it has any.
private struct PlaceRow: View {
    let place: Location
    @Environment(\.dynamicTypeSize) private var typeSize

    var body: some View {
        Group {
            if typeSize.isAccessibilitySize {
                // The text gets the whole width; the photo and stars go above it.
                VStack(alignment: .leading, spacing: 8) {
                    HStack { photo; Spacer(); stars }
                    texts
                }
            } else {
                HStack(spacing: 12) {
                    photo
                    texts
                    Spacer(minLength: 8)
                    stars
                }
            }
        }
        .padding(.vertical, 2)
        .accessibilityElement(children: .combine)
    }

    private var photo: some View {
        RemoteImage(url: primaryPhoto(place)) {
            Rectangle().fill(Color(.tertiarySystemFill))
                .overlay(Text(place.category?.icon ?? "📍").font(.title3))
        }
        .frame(width: 48, height: 48)
        .clipShape(.rect(cornerRadius: 12))
    }

    private var texts: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(place.name).font(.body.weight(.semibold)).foregroundStyle(.primary)
            if !subtitle.isEmpty {
                Text(subtitle).font(.subheadline).foregroundStyle(.secondary)
            }
        }
    }

    @ViewBuilder
    private var stars: some View {
        if let rating = place.rating?.doubleValue, rating > 0 {
            Label(rating.formatted(.number.precision(.fractionLength(0...1))), systemImage: "star.fill")
                .labelStyle(StarLabel())
                .font(.subheadline.weight(.semibold))
                .accessibilityLabel("\(rating.formatted()) stars")
        }
    }

    private var subtitle: String {
        let where_ = place.region?.name ?? place.city?.name ?? place.country?.name ?? place.location
        return [place.category?.displayName, where_].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " · ")
    }
}

/// An orange star before the number, as the artboard draws the rating.
private struct StarLabel: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 3) {
            configuration.icon.foregroundStyle(.orange).font(.caption)
            configuration.title.foregroundStyle(.secondary)
        }
    }
}

/// The map behind the panel: one photo pin per loaded place that has coordinates.
private struct PlacesMap: View {
    let places: [Location]
    let satellite: Bool
    @Binding var camera: MapCameraPosition
    let onSelect: (Location) -> Void

    var body: some View {
        Map(position: $camera) {
            ForEach(pinned, id: \.0.id) { place, coordinate in
                Annotation(place.name, coordinate: coordinate, anchor: .bottom) {
                    Button { onSelect(place) } label: { PhotoPin(place: place) }
                        .buttonStyle(.plain)
                }
                .annotationTitles(.hidden)
            }
        }
        .mapStyle(satellite ? .hybrid(pointsOfInterest: .excludingAll) : .standard(pointsOfInterest: .excludingAll))
        .mapControls { MapCompass() }
    }

    /// A camera over the pins. `.automatic` is worked out once, from the first page.
    ///
    /// Places on several continents (Peru to Japan) span more than a phone's map can show, and
    /// MapKit drops a camera it can't honour. Then the camera frames the biggest group instead,
    /// the places within about a continent of each other, and the rest are a pan away.
    static func fit(_ places: [Location], in viewport: MapViewport) -> MapCameraPosition {
        let all = coordinates(places).map(\.1)
        guard !all.isEmpty else { return .automatic }
        if let rect = bounds(all), rect.width <= MKMapRect.world.width * 0.3 { return .rect(viewport.frame(padded(rect))) }
        let near: (CLLocationCoordinate2D, CLLocationCoordinate2D) -> Bool = {
            abs($0.latitude - $1.latitude) < 30 && abs($0.longitude - $1.longitude) < 40
        }
        let busiest = all.max { a, b in all.filter { near(a, $0) }.count < all.filter { near(b, $0) }.count }!
        return bounds(all.filter { near(busiest, $0) }).map { .rect(viewport.frame(padded($0))) } ?? .automatic
    }

    private static func bounds(_ coordinates: [CLLocationCoordinate2D]) -> MKMapRect? {
        let rects = coordinates.map { MKMapRect(origin: MKMapPoint($0), size: MKMapSize(width: 0, height: 0)) }
        guard let first = rects.first else { return nil }
        return rects.dropFirst().reduce(first) { $0.union($1) }
    }

    /// A margin of a fifth each side, and never closer than a few streets for a single place.
    private static func padded(_ rect: MKMapRect) -> MKMapRect {
        let pad = max(max(rect.width, rect.height) * 0.2, 4000)
        return rect.insetBy(dx: -pad, dy: -pad)
    }

    private var pinned: [(Location, CLLocationCoordinate2D)] { Self.coordinates(places) }

    private static func coordinates(_ places: [Location]) -> [(Location, CLLocationCoordinate2D)] {
        places.compactMap { place in
            guard let lat = place.latitude.flatMap(Double.init), let lon = place.longitude.flatMap(Double.init) else { return nil }
            return (place, CLLocationCoordinate2D(latitude: lat, longitude: lon))
        }
    }
}

/// A round photo with a white ring and a small point under it.
private struct PhotoPin: View {
    let place: Location

    var body: some View {
        VStack(spacing: -3) {
            RemoteImage(url: primaryPhoto(place)) {
                Circle().fill(Theme.accent).overlay(Text(place.category?.icon ?? "📍").font(.callout))
            }
            .frame(width: 44, height: 44)
            .clipShape(.circle)
            .overlay(Circle().stroke(.white, lineWidth: 3))
            Image(systemName: "triangle.fill")
                .font(.system(size: 9))
                .foregroundStyle(.white)
                .rotationEffect(.degrees(180))
        }
        .shadow(color: .black.opacity(0.25), radius: 4, y: 2)
    }
}

/// The map's size and the bands its overlays cover. MapKit fits a camera rect to the whole map,
/// ignoring safe-area padding once the map ignores the safe area, so the rect is grown here until
/// the pins land in the uncovered middle.
struct MapViewport {
    var size = CGSize(width: 402, height: 874)
    var top: CGFloat = 126
    var bottom: CGFloat = 520

    /// A rect of the map's own shape that puts [content] in the band between top and bottom.
    func frame(_ content: MKMapRect) -> MKMapRect {
        let visible = max(size.height - top - bottom, 1)
        let scale = max(content.width / size.width, content.height / visible)
        let width = size.width * scale, height = size.height * scale
        return MKMapRect(
            x: content.midX - width / 2,
            y: content.midY - (top + visible / 2) * scale,
            width: width,
            height: height
        )
    }
}
