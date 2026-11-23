import SwiftUI

/// The canvas's voice on iOS: the brand teal, New York for display titles, SF for everything else,
/// and the corner radii the artboards use. Bars, tab bar, sheets and search take the system's
/// Liquid Glass by themselves; nothing here re-draws them.
enum Theme {
    /// `#007A85`, the accent of every iOS artboard.
    static let accent = Color(red: 0, green: 0x7A / 255, blue: 0x85 / 255)

    /// Large serif titles, as the canvas sets screen and card headings.
    static func display(_ size: CGFloat, weight: Font.Weight = .bold) -> Font {
        .system(size: size, weight: weight, design: .serif)
    }

    enum Radius {
        static let card: CGFloat = 22
        static let row: CGFloat = 16
        static let chip: CGFloat = 12
    }
}
