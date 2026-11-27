import Foundation
import ComposeApp

/// One per SwiftUI screen, held in a `@StateObject`: it lives exactly as long as the screen's
/// identity, and when SwiftUI drops it, `deinit` clears the Kotlin owner, which runs the screen's
/// ViewModels' `onCleared()` and cancels their scopes.
///
/// Nothing that captures the view (a Binding, a closure, a stored Task) may outlive the view, or
/// this never deinits and the ViewModels leak — the bridge spike lost `onCleared` to one Binding.
final class ViewModelHost: ObservableObject {
    let owner = IosViewModelOwner()

    deinit {
        owner.clear()
    }
}
