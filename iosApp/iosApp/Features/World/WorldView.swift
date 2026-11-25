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
    @State private var places: [Location] = []
    @State private var selected: Location?
    @State private var satellite = false
    @State private var camera: MapCameraPosition = .automatic
    @State private var viewport = MapViewport()

    private var presenter: PagingListPresenter<Location> { host.owner.locationsPresenter() }

    var body: some View {
        GeometryReader { geo in
            PlacesMap(places: places, satellite: satellite, camera: $camera) { selected = $0 }
                .ignoresSafeArea()
                .overlay(alignment: .top) {
                    // The bar's row: the switch in the middle, the map style where Countries has search.
                    ZStack {
                        switcher.padding(4).glassEffect(.regular.interactive(), in: .capsule)
                        HStack {
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
        .task { for await p in presenter.items { places = p } }
        .task(id: places.map(\.id)) {
            // Set once the map is on screen: a camera given before it has laid out is dropped.
            try? await Task.sleep(for: .milliseconds(400))
            withAnimation { camera = PlacesMap.fit(places, in: viewport) }
        }
    }
}
