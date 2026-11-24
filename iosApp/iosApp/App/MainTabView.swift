import SwiftUI

/// The tabs of the Coast Mist bar: Home, Places, Collections and World, with the system search tab
/// beside them. The map is not a tab of its own; it is half of World. Each tab keeps its own
/// navigation stack; switching tabs keeps where each one was.
enum AppTab: Hashable { case home, places, collections, world, search }

struct MainTabView: View {
    @State private var selection: AppTab = .home

    var body: some View {
        TabView(selection: $selection) {
            Tab("Home", systemImage: "house", value: .home) {
                NavigationStack { HomeView(open: { selection = $0 }) }
            }
            Tab("Places", systemImage: "mappin.and.ellipse", value: .places) {
                NavigationStack { PlacesView() }
            }
            Tab("Collections", systemImage: "rectangle.stack", value: .collections) {
                NavigationStack { CollectionsView() }
            }
            Tab("World", systemImage: "globe", value: .world) {
                NavigationStack { WorldView() }
            }
            Tab(value: .search, role: .search) {
                NavigationStack { SearchView() }
            }
        }
        .tint(Theme.accent)
    }
}
