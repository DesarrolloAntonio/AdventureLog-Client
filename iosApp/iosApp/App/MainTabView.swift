import SwiftUI

/// The tabs of the iOS-Home artboard's bar, with the system search tab beside them. The artboard
/// also has World in the bar, but an iPhone shows five tabs at most, search included, and folds
/// the rest into "More"; World opens from Home's Countries tile instead. Each tab keeps its own
/// navigation stack; switching tabs keeps where each one was.
enum AppTab: Hashable { case home, places, collections, map, search }

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
                NavigationStack { ComingSoon(title: "Collections") }
            }
            Tab("Map", systemImage: "map", value: .map) {
                NavigationStack { ComingSoon(title: "Map") }
            }
            Tab(value: .search, role: .search) {
                NavigationStack { ComingSoon(title: "Search") }
            }
        }
        .tint(Theme.accent)
    }
}

/// A screen still to be built in this phase.
struct ComingSoon: View {
    let title: String
    var body: some View {
        ContentUnavailableView(title, systemImage: "hammer", description: Text("Coming in the next steps of the iOS app."))
            .navigationTitle(title)
    }
}
