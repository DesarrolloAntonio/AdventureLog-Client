import SwiftUI
import MapKit
import ComposeApp

/// The places on a map: one photo pin per loaded place that has coordinates.
struct PlacesMap: View {
    let places: [Location]
    let satellite: Bool
    @Binding var camera: MapCameraPosition
    let onSelect: (Location) -> Void

    var body: some View {
        Map(position: $camera) {
            ForEach(pinned, id: \.0.id) { place, coordinate in
                Annotation(place.name, coordinate: coordinate, anchor: .bottom) {
                    Button { onSelect(place) } label: { PhotoPin(place: place) }
                        .buttonStyle(.plain)
                }
                .annotationTitles(.hidden)
            }
        }
        .mapStyle(satellite ? .hybrid(pointsOfInterest: .excludingAll) : .standard(pointsOfInterest: .excludingAll))
        .mapControls { MapCompass() }
    }

    /// A camera over the pins. `.automatic` is worked out once, from the first page.
    ///
    /// Places on several continents (Peru to Japan) span more than a phone's map can show, and
    /// MapKit drops a camera it can't honour. Then the camera frames the biggest group instead,
    /// the places within about a continent of each other, and the rest are a pan away.
    static func fit(_ places: [Location], in viewport: MapViewport) -> MapCameraPosition {
        let all = coordinates(places).map(\.1)
        guard !all.isEmpty else { return .automatic }
        if let rect = bounds(all), rect.width <= MKMapRect.world.width * 0.3 { return .rect(viewport.frame(padded(rect))) }
        let near: (CLLocationCoordinate2D, CLLocationCoordinate2D) -> Bool = {
            abs($0.latitude - $1.latitude) < 30 && abs($0.longitude - $1.longitude) < 40
        }
        let busiest = all.max { a, b in all.filter { near(a, $0) }.count < all.filter { near(b, $0) }.count }!
        return bounds(all.filter { near(busiest, $0) }).map { .rect(viewport.frame(padded($0))) } ?? .automatic
    }

    private static func bounds(_ coordinates: [CLLocationCoordinate2D]) -> MKMapRect? {
        let rects = coordinates.map { MKMapRect(origin: MKMapPoint($0), size: MKMapSize(width: 0, height: 0)) }
        guard let first = rects.first else { return nil }
        return rects.dropFirst().reduce(first) { $0.union($1) }
    }

    /// A margin of a fifth each side, and never closer than a few streets for a single place.
    private static func padded(_ rect: MKMapRect) -> MKMapRect {
        let pad = max(max(rect.width, rect.height) * 0.2, 4000)
        return rect.insetBy(dx: -pad, dy: -pad)
    }

    private var pinned: [(Location, CLLocationCoordinate2D)] { Self.coordinates(places) }

    private static func coordinates(_ places: [Location]) -> [(Location, CLLocationCoordinate2D)] {
        places.compactMap { place in
            guard let lat = place.latitude.flatMap(Double.init), let lon = place.longitude.flatMap(Double.init) else { return nil }
            return (place, CLLocationCoordinate2D(latitude: lat, longitude: lon))
        }
    }
}

/// A round photo with a white ring and a small point under it.
struct PhotoPin: View {
    let place: Location

    var body: some View {
        VStack(spacing: -3) {
            RemoteImage(url: primaryPhoto(place)) {
                Circle().fill(Theme.accent).overlay(Text(place.category?.icon ?? "📍").font(.callout))
            }
            .frame(width: 44, height: 44)
            .clipShape(.circle)
            .overlay(Circle().stroke(.white, lineWidth: 3))
            Image(systemName: "triangle.fill")
                .font(.system(size: 9))
                .foregroundStyle(.white)
                .rotationEffect(.degrees(180))
        }
        .shadow(color: .black.opacity(0.25), radius: 4, y: 2)
    }
}

/// The map's size and the bands its overlays cover. MapKit fits a camera rect to the whole map,
/// ignoring safe-area padding once the map ignores the safe area, so the rect is grown here until
/// the pins land in the uncovered middle.
struct MapViewport {
    var size = CGSize(width: 402, height: 874)
    var top: CGFloat = 126
    var bottom: CGFloat = 520

    /// A rect of the map's own shape that puts [content] in the band between top and bottom.
    func frame(_ content: MKMapRect) -> MKMapRect {
        let visible = max(size.height - top - bottom, 1)
        let scale = max(content.width / size.width, content.height / visible)
        let width = size.width * scale, height = size.height * scale
        return MKMapRect(
            x: content.midX - width / 2,
            y: content.midY - (top + visible / 2) * scale,
            width: width,
            height: height
        )
    }
}
