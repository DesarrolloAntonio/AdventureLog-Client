import SwiftUI
import UIKit
import ComposeApp

@main
struct iOSApp: App {
    init() {
        IosKoinKt.doInitKoin()
        // Coast Mist sets screen titles heavy and pulled in; the system's large title is bold.
        let large = UIFont.systemFont(ofSize: 34, weight: .heavy)
        UINavigationBar.appearance().largeTitleTextAttributes = [
            .font: UIFontMetrics(forTextStyle: .largeTitle).scaledFont(for: large),
            .kern: -0.6,
        ]
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
