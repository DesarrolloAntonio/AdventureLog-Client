import SwiftUI
import ComposeApp

/// The app's gate: Login until there is a session, then the tabs. It watches the session itself,
/// so a session lost anywhere (signed out, expired on the server) brings Login back.
struct RootView: View {
    @StateObject private var host = ViewModelHost()
    @State private var user: UserDetails?
    @State private var checked = false

    var body: some View {
        Group {
            if !checked {
                ProgressView()
            } else if user == nil {
                ContentUnavailableView("Sign in", systemImage: "person.crop.circle", description: Text("Login comes next."))
            } else {
                ContentUnavailableView("AdventureLog", systemImage: "map", description: Text("Signed in."))
            }
        }
        .tint(Theme.accent)
        .task {
            for await session in host.owner.sessionFlow() {
                user = session
                checked = true
            }
        }
    }
}
