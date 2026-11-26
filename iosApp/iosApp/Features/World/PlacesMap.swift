import SwiftUI
import MapKit
import ComposeApp

/// The places on a map: one photo pin per place that has coordinates, and, when asked for, a dot
/// for each visited region and city under them.
struct PlacesMap: View {
    let places: [Location]
    let satellite: Bool
    @Binding var camera: MapCameraPosition
    var regions: [VisitedRegion] = []
    var cities: [VisitedCity] = []
    let onSelect: (Location) -> Void

    /// What the map shows now, for grouping the pins; nil until it first settles.
    @State private var region: MKCoordinateRegion?

    var body: some View {
        Map(position: $camera) {
            ForEach(cities, id: \.id) { city in
                if let lat = city.latitude?.doubleValue, let lon = city.longitude?.doubleValue {
                    Annotation(city.name, coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lon)) {
                        LayerDot(tone: .amber, size: 10)
                    }
                }
            }
            ForEach(regions, id: \.id) { region in
                if let lat = region.latitude?.doubleValue, let lon = region.longitude?.doubleValue {
                    Annotation(region.name, coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lon)) {
                        LayerDot(tone: .teal, size: 16)
                    }
                }
            }
            ForEach(clusters, id: \.id) { cluster in
                Annotation(cluster.places[0].name, coordinate: cluster.center, anchor: .bottom) {
                    if cluster.places.count == 1 {
                        Button { onSelect(cluster.places[0]) } label: { PhotoPin(place: cluster.places[0]) }
                            .buttonStyle(.plain)
                    } else {
                        Button { zoom(into: cluster) } label: { PhotoPin(place: cluster.places[0], count: cluster.places.count) }
                            .buttonStyle(.plain)
                            .accessibilityLabel("\(cluster.places.count) places")
                            .accessibilityHint("Zooms in")
                    }
                }
                .annotationTitles(.hidden)
            }
        }
        .onMapCameraChange(frequency: .onEnd) { region = $0.region }
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

    private struct Cluster { let id: String; let center: CLLocationCoordinate2D; let places: [Location] }

    /// Places close together on screen as one pin with their number on it, as Android and the web
    /// cluster: two hundred photos over one country were a blob. The screen is cut into a grid
    /// about a pin and a half wide; zooming in splits the groups.
    private var clusters: [Cluster] {
        let pinned = Self.coordinates(places)
        guard let region, region.span.longitudeDelta > 0 else {
            return pinned.map { Cluster(id: $0.0.id, center: $0.1, places: [$0.0]) }
        }
        let cellLon = region.span.longitudeDelta / 6, cellLat = region.span.latitudeDelta / 11
        var cells: [String: [(Location, CLLocationCoordinate2D)]] = [:]
        var order: [String] = []
        for item in pinned {
            let key = "\(Int(floor(item.1.longitude / cellLon))):\(Int(floor(item.1.latitude / cellLat)))"
            if cells[key] == nil { order.append(key) }
            cells[key, default: []].append(item)
        }
        return order.map { key in
            let items = cells[key]!
            let lat = items.map(\.1.latitude).reduce(0, +) / Double(items.count)
            let lon = items.map(\.1.longitude).reduce(0, +) / Double(items.count)
            // Keyed by its first place, so a pin keeps its identity while the map moves.
            return Cluster(id: items.count == 1 ? items[0].0.id : "c-" + items[0].0.id,
                           center: CLLocationCoordinate2D(latitude: lat, longitude: lon), places: items.map(\.0))
        }
    }

    /// Frames a group's places, close enough that they come apart.
    private func zoom(into cluster: Cluster) {
        guard let rect = Self.bounds(Self.coordinates(cluster.places).map(\.1)) else { return }
        withAnimation { camera = .rect(Self.padded(rect)) }
    }

    private static func coordinates(_ places: [Location]) -> [(Location, CLLocationCoordinate2D)] {
        places.compactMap { place in
            guard let lat = place.latitude.flatMap(Double.init), let lon = place.longitude.flatMap(Double.init) else { return nil }
            return (place, CLLocationCoordinate2D(latitude: lat, longitude: lon))
        }
    }
}

/// A visited region or city: a coloured dot in a white ring, quieter than a place's photo.
private struct LayerDot: View {
    let tone: Theme.Tone
    let size: CGFloat

    var body: some View {
        Circle().fill(tone.ink)
            .frame(width: size, height: size)
            .overlay(Circle().stroke(.white, lineWidth: 2))
            .shadow(color: .black.opacity(0.2), radius: 2, y: 1)
    }
}

/// A round photo with a white ring and a small point under it.
struct PhotoPin: View {
    let place: Location
    /// How many places the pin stands for, when it is a group of them.
    var count: Int = 1

    var body: some View {
        VStack(spacing: -3) {
            RemoteImage(url: primaryPhoto(place)) {
                Circle().fill(Theme.accent).overlay(Text(place.category?.icon ?? "📍").font(.callout))
            }
            .frame(width: 44, height: 44)
            .clipShape(.circle)
            .overlay(Circle().stroke(.white, lineWidth: 3))
            .overlay(alignment: .topTrailing) {
                if count > 1 {
                    Text("\(count)")
                        .font(.caption2.weight(.bold)).monospacedDigit()
                        .foregroundStyle(.white)
                        .padding(.horizontal, 5)
                        .frame(minWidth: 20, minHeight: 20)
                        .background(Capsule().fill(Theme.accent))
                        .overlay(Capsule().stroke(.white, lineWidth: 2))
                        .offset(x: 8, y: -6)
                }
            }
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
