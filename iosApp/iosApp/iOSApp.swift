import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        IosKoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
