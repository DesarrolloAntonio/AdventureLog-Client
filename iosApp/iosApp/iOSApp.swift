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
            // Capped as the system caps its own large titles: uncapped, the biggest text sizes drew
            // the title over the bar's buttons.
            .font: UIFontMetrics(forTextStyle: .largeTitle).scaledFont(for: large, maximumPointSize: 44),
            .kern: -0.6,
        ]
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
