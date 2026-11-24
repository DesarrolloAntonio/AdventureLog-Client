import SwiftUI
import ComposeApp

/// The app's gate: Login until there is a session, then the tabs. It watches the session itself,
/// so a session lost anywhere (signed out, expired on the server) brings Login back.
struct RootView: View {
    /// `-showLogin` on a debug build shows Login over a live session, so the screen can be looked
    /// at without signing out.
    #if DEBUG
    private static let showLoginForReview = ProcessInfo.processInfo.arguments.contains("-showLogin")
    #else
    private static let showLoginForReview = false
    #endif

    @StateObject private var host = ViewModelHost()
    @State private var user: UserDetails?
    @State private var checked = false
    @State private var theme: CommonThemeMode = .auto

    var body: some View {
        Group {
            if !checked {
                ProgressView()
            } else if user == nil || Self.showLoginForReview {
                LoginView()
            } else {
                MainTabView()
            }
        }
        .tint(Theme.accent)
        // The theme chosen in Settings; System leaves it to the phone.
        .preferredColorScheme(theme == .light ? .light : theme == .dark ? .dark : nil)
        .task { for await t in host.owner.settingsViewModel().themeMode { theme = t } }
        .task {
            for await session in host.owner.sessionFlow() {
                user = session
                checked = true
            }
        }
    }
}
