import SwiftUI
import UIKit
import ComposeApp

/// A server photo, loaded through the app's authenticated image client (`IosImages`), cached in
/// memory, and drawn to fill its frame. While loading, or if it never loads, [placeholder] shows.
struct RemoteImage<Placeholder: View>: View {
    let url: String?
    @ViewBuilder var placeholder: Placeholder
    @State private var image: UIImage?

    private static var cache: NSCache<NSString, UIImage> { ImageCache.shared }

    var body: some View {
        // The size comes from the frame the caller gives (Color.clear takes any), never from the
        // photo: a filled image in a ZStack reports its own overflowing width and widened Home.
        Color.clear
            .overlay { placeholder }
            .overlay {
                if let image {
                    Image(uiImage: image).resizable().scaledToFill()
                }
            }
            .clipped()
            .task(id: url) {
                guard let url, !url.isEmpty else { image = nil; return }
                if let cached = Self.cache.object(forKey: url as NSString) { image = cached; return }
                if let data = try? await IosImages.shared.data(url: url), let loaded = UIImage(data: data) {
                    Self.cache.setObject(loaded, forKey: url as NSString)
                    image = loaded
                }
            }
    }
}

private enum ImageCache {
    static let shared: NSCache<NSString, UIImage> = {
        let cache = NSCache<NSString, UIImage>()
        cache.countLimit = 200
        return cache
    }()
}
