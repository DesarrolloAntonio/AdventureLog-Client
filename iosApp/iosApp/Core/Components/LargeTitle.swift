import SwiftUI
import UIKit

extension View {
    /// Keeps the bar's large title open while this screen is pushed. When the content finished
    /// loading during the push (a spinner replaced by a long list), UIKit folded the title into
    /// the bar with the content at its top, and only a pull-down opened it again.
    func opensLargeTitle() -> some View {
        background(LargeTitleOpener().frame(width: 0, height: 0))
    }
}

private struct LargeTitleOpener: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> Controller { Controller() }
    func updateUIViewController(_ controller: Controller, context: Context) {}

    final class Controller: UIViewController {
        private var watch: NSKeyValueObservation?

        override func viewWillAppear(_ animated: Bool) {
            super.viewWillAppear(animated)
            guard let scroll = scrollView else { return }
            // Until shortly after the push: by then a fold is the reader's own scrolling.
            watch = scroll.observe(\.contentSize) { [weak self] _, _ in
                DispatchQueue.main.async { self?.open() }
            }
        }

        override func viewDidAppear(_ animated: Bool) {
            super.viewDidAppear(animated)
            open()
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) { [weak self] in
                self?.open()
                self?.watch = nil
            }
        }

        override func viewWillDisappear(_ animated: Bool) {
            super.viewWillDisappear(animated)
            watch = nil
        }

        /// What a pull-down does: scroll past the top until the bar opens, then rest at the new top.
        /// Only when the title is folded with the content at its top, which scrolling never leaves.
        private func open() {
            guard let nav = navigationController, let scroll = scrollView,
                  nav.topViewController?.navigationItem.largeTitleDisplayMode == .always,
                  nav.navigationBar.frame.height < 80,
                  scroll.contentSize.height > 0,
                  scroll.contentOffset.y <= -scroll.adjustedContentInset.top + 1 else { return }
            scroll.setContentOffset(CGPoint(x: 0, y: -scroll.adjustedContentInset.top - 200), animated: false)
            nav.view.layoutIfNeeded()
            scroll.setContentOffset(CGPoint(x: 0, y: -scroll.adjustedContentInset.top), animated: false)
        }

        private var scrollView: UIScrollView? {
            navigationController?.topViewController?.view.flatMap(Self.firstScrollView)
        }

        private static func firstScrollView(in view: UIView) -> UIScrollView? {
            if let scroll = view as? UIScrollView { return scroll }
            for sub in view.subviews { if let scroll = firstScrollView(in: sub) { return scroll } }
            return nil
        }
    }
}
