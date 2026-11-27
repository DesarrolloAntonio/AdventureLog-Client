import SwiftUI
import MapKit

/// Where a place is, picked by tapping the map: for somewhere the search does not know (a
/// viewpoint, a trailhead). It opens on the spot already chosen, or on the whole world.
struct MapPicker: View {
    let title: String
    let initial: CLLocationCoordinate2D?
    let onPick: (CLLocationCoordinate2D) -> Void

    @State private var pin: CLLocationCoordinate2D?
    @State private var position: MapCameraPosition
    /// Where the map is looking: a pin dropped on an automatic camera made it zoom to the pin.
    @State private var camera: MapCamera?
    @Environment(\.dismiss) private var dismiss

    init(title: String, initial: CLLocationCoordinate2D?, onPick: @escaping (CLLocationCoordinate2D) -> Void) {
        self.title = title
        self.initial = initial
        self.onPick = onPick
        _pin = State(initialValue: initial)
        _position = State(initialValue: initial.map {
            .region(MKCoordinateRegion(center: $0, latitudinalMeters: 20_000, longitudinalMeters: 20_000))
        } ?? .automatic)
    }

    var body: some View {
        NavigationStack {
            MapReader { proxy in
                Map(position: $position) {
                    if let pin { Marker(title, coordinate: pin).tint(Theme.accent) }
                }
                .mapControls { MapUserLocationButton(); MapCompass() }
                .onMapCameraChange { context in camera = context.camera }
                .onTapGesture { point in
                    guard let coordinate = proxy.convert(point, from: .local) else { return }
                    if let camera { position = .camera(camera) }
                    pin = coordinate
                }
            }
            .ignoresSafeArea(edges: .bottom)
            .safeAreaInset(edge: .bottom) {
                Text(pin == nil ? "Tap the map where it is." : "Tap again to move the pin.")
                    .font(.subheadline.weight(.medium))
                    .padding(.horizontal, 18).padding(.vertical, 12)
                    .glassEffect(.regular, in: .capsule)
                    .padding(.bottom, 12)
            }
            .navigationTitle("Pick on the Map")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button { dismiss() } label: { Image(systemName: "xmark") }.accessibilityLabel("Close")
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button { if let pin { onPick(pin) }; dismiss() } label: { Image(systemName: "checkmark") }
                        .buttonStyle(.glassProminent)
                        .tint(Theme.accent)
                        .disabled(pin == nil)
                        .accessibilityLabel("Use this spot")
                }
            }
        }
    }
}
