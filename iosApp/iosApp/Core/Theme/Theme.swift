import SwiftUI
import UIKit

/// Coast Mist, the iOS look from the canvas (`design/native-ui/Mist-*`): a misty pastel wash
/// behind everything, frosted white cards, one teal gradient card per screen for what matters
/// most, heavy sans titles and SF for the rest. Bars, the tab bar, search and sheets are the
/// system's own; nothing here re-draws them.
///
/// The canvas draws light mode only. Dark mode keeps the same structure with the wash turned
/// down to deep sea tones and the cards to dark frosted glass.
enum Theme {
    /// `#0B7A84`, the accent of every Mist artboard.
    static let accent = Color(light: 0x0B7A84, dark: 0x3FB8C2)

    /// Body text, `#102027`.
    static let ink = Color(light: 0x102027, dark: 0xEEF3F5)
    /// Secondary text, `#56656C`.
    static let muted = Color(light: 0x56656C, dark: 0xA3B1B7)
    /// Counts and trailing values, `#6E7B82`.
    static let faint = Color(light: 0x6E7B82, dark: 0x8C9AA1)
    /// Chevrons and other quiet glyphs, `#8A979D`.
    static let glyph = Color(light: 0x8A979D, dark: 0x6F7D84)
    /// Row separators.
    static let separator = Color(light: 0x102027, dark: 0xFFFFFF).opacity(0.1)

    /// A screen title: the large-title style (34 pt at the default size, and growing with the
    /// reader's text size), heavy, as the canvas sets it.
    static func title() -> Font { .largeTitle.weight(.heavy) }

    enum Radius {
        static let hero: CGFloat = 24
        static let card: CGFloat = 20
        static let field: CGFloat = 12
        static let thumb: CGFloat = 12
    }

    /// The soft colour pairs the canvas uses for icon badges and tags.
    enum Tone {
        case green, coral, blue, amber, mint, teal

        var fill: Color {
            switch self {
            case .green: Color(light: 0xDCF3E5, dark: 0x1D3A2A)
            case .coral: Color(light: 0xFCE3D9, dark: 0x452820)
            case .blue: Color(light: 0xDCE9FB, dark: 0x1E2F4A)
            case .amber: Color(light: 0xFBEFD3, dark: 0x3F3219)
            case .mint: Color(light: 0xD7F0EE, dark: 0x173B3B)
            case .teal: Color(light: 0xD7F0EE, dark: 0x173B3B)
            }
        }

        var ink: Color {
            switch self {
            case .green: Color(light: 0x1F7A48, dark: 0x7FD6A2)
            case .coral: Color(light: 0xB23F22, dark: 0xF2A38D)
            case .blue: Color(light: 0x2B63B8, dark: 0x93B8F0)
            case .amber: Color(light: 0x9A6A12, dark: 0xE8C277)
            case .mint: Color(light: 0x1E7A73, dark: 0x8ADBD2)
            case .teal: Color(light: 0x0B7A84, dark: 0x6FD0D8)
            }
        }
    }
}

extension Color {
    /// One colour for each appearance, given as `0xRRGGBB`.
    init(light: UInt32, dark: UInt32) {
        self.init(uiColor: UIColor { traits in
            let hex = traits.userInterfaceStyle == .dark ? dark : light
            return UIColor(
                red: CGFloat((hex >> 16) & 0xFF) / 255,
                green: CGFloat((hex >> 8) & 0xFF) / 255,
                blue: CGFloat(hex & 0xFF) / 255,
                alpha: 1
            )
        })
    }
}

// MARK: - The wash

/// The mist behind every screen: peach from the top left, sky from the top right, sea glass from
/// the bottom, over a pale grey. Each is the canvas's CSS `radial-gradient(W H at x y, colour 0,
/// transparent S)`: an ellipse of radii W × H centred at (x, y), fading out at S of its radius.
struct MistBackground: View {
    @Environment(\.colorScheme) private var scheme

    private struct Blob { let light: UInt32, dark: UInt32, x: CGFloat, y: CGFloat, rx: CGFloat, ry: CGFloat, fade: CGFloat }

    private static let blobs = [
        Blob(light: 0xFCE2D2, dark: 0x2E2320, x: 0, y: 0, rx: 1.2, ry: 0.55, fade: 0.55),
        Blob(light: 0xD6E6FA, dark: 0x17263A, x: 1, y: 0.06, rx: 0.95, ry: 0.55, fade: 0.6),
        Blob(light: 0xCFEFEA, dark: 0x12302C, x: 0.25, y: 1, rx: 1.2, ry: 0.6, fade: 0.6),
    ]

    var body: some View {
        // Drawn on a canvas, each blob a circular gradient stretched into its ellipse and filling
        // the whole canvas: gradient views in frames left hard edges where a frame ended.
        Canvas { context, size in
            context.fill(Path(CGRect(origin: .zero, size: size)), with: .color(Color(light: 0xF1F4F6, dark: 0x0C1418)))
            for b in Self.blobs {
                let colour = Color(light: b.light, dark: b.dark)
                let rx = size.width * b.rx, ry = size.height * b.ry
                var layer = context
                layer.translateBy(x: size.width * b.x, y: size.height * b.y)
                layer.scaleBy(x: rx / ry, y: 1)
                let far = CGRect(x: -size.width * 4, y: -size.height * 4, width: size.width * 8, height: size.height * 8)
                layer.fill(Path(far), with: .radialGradient(
                    Gradient(colors: [colour, colour.opacity(0)]),
                    center: .zero, startRadius: 0, endRadius: ry * b.fade
                ))
            }
        }
        .id(scheme)
        .ignoresSafeArea()
        .accessibilityHidden(true)
    }
}

// MARK: - Cards

extension View {
    /// The canvas's frosted card: white at three quarters over a blur, a bright hairline edge and
    /// a soft sea-tinted shadow.
    func mistCard(radius: CGFloat = Theme.Radius.card) -> some View {
        modifier(MistCard(radius: radius))
    }

    /// The teal gradient card: the next trip, the main action, the packing list.
    func heroCard(radius: CGFloat = Theme.Radius.hero) -> some View {
        background {
            RoundedRectangle(cornerRadius: radius, style: .continuous)
                .fill(HeroFill())
                .shadow(color: Color(light: 0x0A4E66, dark: 0x000000).opacity(0.3), radius: 15, y: 10)
        }
        .foregroundStyle(.white)
    }

    /// Paints the mist behind a screen.
    func mistBackground() -> some View {
        background { MistBackground() }
    }
}

private struct MistCard: ViewModifier {
    let radius: CGFloat
    @Environment(\.colorScheme) private var scheme

    func body(content: Content) -> some View {
        let shape = RoundedRectangle(cornerRadius: radius, style: .continuous)
        content
            // The shadow belongs to the card's shape alone: on the whole view it would also fall
            // under every tag and line of text inside.
            .background {
                shape.fill(.ultraThinMaterial)
                    .overlay(shape.fill(scheme == .dark ? Color(white: 0.16).opacity(0.6) : Color.white.opacity(0.6)))
                    .shadow(color: Color(light: 0x14465A, dark: 0x000000).opacity(scheme == .dark ? 0.3 : 0.08), radius: 11, y: 6)
            }
            .overlay(shape.strokeBorder(scheme == .dark ? Color.white.opacity(0.08) : Color.white.opacity(0.92), lineWidth: 1))
    }
}

/// `linear-gradient(135deg, #12838D, #0A4E66)` with a white glow in the top right corner.
struct HeroFill: ShapeStyle {
    func resolve(in environment: EnvironmentValues) -> some ShapeStyle {
        LinearGradient(
            colors: [Color(light: 0x12838D, dark: 0x137E88), Color(light: 0x0A4E66, dark: 0x093C4F)],
            startPoint: .topLeading, endPoint: .bottomTrailing
        )
    }
}

/// The hero card's glow, laid over [HeroFill] where a view wants it.
struct HeroGlow: View {
    var body: some View {
        RadialGradient(colors: [.white.opacity(0.22), .white.opacity(0)], center: .topTrailing, startRadius: 0, endRadius: 220)
            .allowsHitTesting(false)
    }
}

// MARK: - Small pieces

/// A soft coloured circle with a glyph in it, as the canvas draws quick access and detail rows.
struct ToneBadge: View {
    let systemImage: String
    let tone: Theme.Tone
    var size: CGFloat = 30

    var body: some View {
        Image(systemName: systemImage)
            .font(.system(size: size * 0.5, weight: .semibold))
            .foregroundStyle(tone.ink)
            .frame(width: size, height: size)
            .background(tone.fill, in: .circle)
            .accessibilityHidden(true)
    }
}

/// A small rounded tag: a category, "Visited", "Planned · 5 Sep".
struct ToneTag: View {
    let text: String
    let tone: Theme.Tone
    var systemImage: String?

    var body: some View {
        HStack(spacing: 3) {
            if let systemImage { Image(systemName: systemImage).font(.caption2.weight(.bold)) }
            Text(text)
        }
        .font(.caption.weight(.semibold))
        .foregroundStyle(tone.ink)
        .padding(.horizontal, 8)
        .frame(minHeight: 22)
        .background(tone.fill, in: .rect(cornerRadius: 7, style: .continuous))
    }
}

/// A grey section label above a card, 13 pt semibold.
struct SectionLabel: View {
    let text: String
    var trailing: String?

    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(text).font(.footnote.weight(.semibold)).foregroundStyle(Theme.muted)
            Spacer()
            if let trailing { Text(trailing).font(.footnote).foregroundStyle(Theme.faint).monospacedDigit() }
        }
        .padding(.horizontal, 6)
        .accessibilityAddTraits(.isHeader)
    }
}
