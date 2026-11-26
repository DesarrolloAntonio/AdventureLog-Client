import SwiftUI
import MapKit
import ComposeApp

/// World, as the Mist canvas splits it: a Map of every place and the Countries visited, under one
/// switch at the top.
struct WorldView: View {
    enum Page: Hashable { case map, countries }

    @State private var page: Page = .map

    var body: some View {
        Group {
            switch page {
            case .map: WorldMap(switcher: switcher)
            case .countries: CountriesView().mistBackground()
            }
        }
        .toolbar {
            if page == .countries {
                ToolbarItem(placement: .principal) { switcher }
            }
        }
        // Large on both pages: a mode switched at runtime never opened the title out, and
        // screens pushed from an inline bar open with their own large titles folded.
        .navigationTitle(page == .countries ? "World" : "")
        .navigationBarTitleDisplayMode(.large)
        // The map runs to the top with no bar over it: the bar laid a blurred band with a hard
        // edge over the map. Its switch floats in the bar's place instead.
        .toolbarVisibility(page == .map ? .hidden : .automatic, for: .navigationBar)
    }

    private var switcher: some View {
        Picker("View", selection: $page) {
            Text("Map").tag(Page.map)
            Text("Countries").tag(Page.countries)
        }
        .pickerStyle(.segmented)
        .frame(width: 220)
    }
}

/// Every place on the map, framed on the biggest group; a pin opens its place.
private struct WorldMap<Switcher: View>: View {
    /// The Map/Countries switch, floating where the bar holds it on the Countries page.
    let switcher: Switcher

    @StateObject private var host = ViewModelHost()
    /// Nil until the shared map state first arrives (Swift can't build its defaults).
    @State private var state: MapUiState?
    @State private var selected: Location?
    @State private var showFilters = false
    @State private var satellite = false
    @State private var camera: MapCameraPosition = .automatic
    @State private var viewport = MapViewport()

    /// Every place with coordinates, not a page of them, with the shared filters' choice of pins.
    private var vm: MapViewModel { host.owner.mapViewModel() }
    private var places: [Location] { state.map { MapUiStateKt.mapMarkers($0.locations, filters: $0.filters) } ?? [] }

    /// Filters away from "everything", for the button's look: layers only add, so they don't count.
    private var narrowed: Bool {
        guard let f = state?.filters else { return false }
        return !f.showVisited || !f.showPlanned || !f.selectedCategories.isEmpty
    }

    var body: some View {
        GeometryReader { geo in
            PlacesMap(places: places, satellite: satellite, camera: $camera,
                      regions: state?.filters.showRegions == true ? state?.visitedRegions ?? [] : [],
                      cities: state?.filters.showCities == true ? state?.visitedCities ?? [] : []) { selected = $0 }
                .ignoresSafeArea()
                .overlay(alignment: .top) {
                    // The bar's row: the switch in the middle, the map style where Countries has search.
                    ZStack {
                        switcher.padding(4).glassEffect(.regular.interactive(), in: .capsule)
                        HStack {
                            Button { showFilters = true } label: {
                                Image(systemName: narrowed ? "line.3.horizontal.decrease.circle.fill" : "line.3.horizontal.decrease")
                                    .font(.system(size: 17, weight: .semibold))
                                    .frame(width: 44, height: 44)
                            }
                            .glassEffect(.regular.interactive(), in: .circle)
                            .tint(narrowed ? Theme.accent : .primary)
                            .accessibilityLabel(narrowed ? "Map filters, on" : "Map filters")
                            Spacer()
                            Button { satellite.toggle() } label: {
                                Image(systemName: satellite ? "map" : "globe.europe.africa")
                                    .font(.system(size: 17, weight: .semibold))
                                    .frame(width: 44, height: 44)
                            }
                            .glassEffect(.regular.interactive(), in: .circle)
                            .tint(.primary)
                            .accessibilityLabel(satellite ? "Standard map" : "Satellite map")
                        }
                    }
                    .frame(height: 44)
                    .padding(.horizontal, 16)
                }
                .onChange(of: geo.size, initial: true) {
                    viewport = MapViewport(
                        size: CGSize(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom),
                        // The floating row, plus a pin's height: a pin stands above its point.
                        top: geo.safeAreaInsets.top + 44 + 52,
                        bottom: geo.safeAreaInsets.bottom + 8
                    )
                }
        }
        .navigationDestination(item: $selected) { PlaceView(placeId: $0.id, name: $0.name, subtitle: whereabouts($0)) }
        .sheet(isPresented: $showFilters) { if let state { MapFiltersSheet(vm: vm, state: state) } }
        .task { for await s in vm.uiState { state = s } }
        // A place saved or deleted anywhere moves, adds or takes away a pin.
        .onReceive(NotificationCenter.default.publisher(for: .placeSaved)) { _ in vm.refresh() }
        // Framed on every place, not the filtered ones: a filter shouldn't swing the map around.
        .task(id: state?.locations.map(\.id) ?? []) {
            guard let all = state?.locations, !all.isEmpty else { return }
            // Set once the map is on screen: a camera given before it has laid out is dropped.
            try? await Task.sleep(for: .milliseconds(400))
            withAnimation { camera = PlacesMap.fit(all, in: viewport) }
        }
    }
}
