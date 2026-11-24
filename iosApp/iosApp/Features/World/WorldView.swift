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
            case .map: WorldMap()
            case .countries: CountriesView().mistBackground()
            }
        }
        .toolbar {
            ToolbarItem(placement: .principal) {
                Picker("View", selection: $page) {
                    Text("Map").tag(Page.map)
                    Text("Countries").tag(Page.countries)
                }
                .pickerStyle(.segmented)
                .frame(width: 220)
            }
        }
        // Large on both pages: a mode switched at runtime never opened the title out, and
        // screens pushed from an inline bar open with their own large titles folded.
        // The map runs under the bar with no title over it.
        .navigationTitle(page == .countries ? "World" : "")
        .navigationBarTitleDisplayMode(.large)
    }
}

/// Every place on the map, framed on the biggest group; a pin opens its place.
private struct WorldMap: View {
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
                .overlay(alignment: .topTrailing) {
                    Button { satellite.toggle() } label: {
                        Image(systemName: satellite ? "map" : "globe.europe.africa")
                            .font(.system(size: 17, weight: .semibold))
                            .frame(width: 48, height: 48)
                    }
                    .glassEffect(.regular.interactive(), in: .circle)
                    .tint(.primary)
                    .padding(16)
                    .accessibilityLabel(satellite ? "Standard map" : "Satellite map")
                }
                .onChange(of: geo.size, initial: true) {
                    viewport = MapViewport(
                        size: CGSize(width: geo.size.width, height: geo.size.height + geo.safeAreaInsets.top + geo.safeAreaInsets.bottom),
                        // The bar, plus a pin's height: a pin stands above its point.
                        top: geo.safeAreaInsets.top + 52,
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
