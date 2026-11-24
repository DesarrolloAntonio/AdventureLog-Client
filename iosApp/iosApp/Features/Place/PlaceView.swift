import SwiftUI
import MapKit
import ComposeApp

/// One place, as the iOS-Place artboard draws it: its photo full-bleed with the rating, category
/// and visit state over it and the name in serif; then Directions, a new visit and its website;
/// the last visit and its collection; and what it's about.
struct PlaceView: View {
    let placeId: String
    /// Shown as the title while the place loads.
    let name: String

    @StateObject private var host = ViewModelHost()
    @State private var state: LocationState = LocationState.Loading.shared
    @State private var collections: [UltraSlimCollection] = []
    @State private var message: String?
    @Environment(\.openURL) private var openURL
    @Environment(\.dynamicTypeSize) private var typeSize

    private var vm: AdventureDetailViewModel { host.owner.adventureDetailViewModel() }

    var body: some View {
        Group {
            switch onEnum(of: state) {
            case .loading:
                ProgressView().controlSize(.large).frame(maxWidth: .infinity, maxHeight: .infinity)
                    .navigationTitle(name)
            case .error(let e):
                ContentUnavailableView {
                    Label("Couldn't load \(name)", systemImage: "wifi.exclamationmark")
                } description: {
                    Text(e.message)
                } actions: {
                    Button("Try again") { vm.loadLocation(locationId: placeId) }.buttonStyle(.glassProminent)
                }
                .navigationTitle(name)
            case .success(let s):
                content(s.location)
            }
        }
        .background(Color(.systemGroupedBackground))
        .navigationBarTitleDisplayMode(.inline)
        .task { vm.loadLocation(locationId: placeId) }
        .task { for await s in vm.locationState { state = s } }
        .task { for await c in vm.collections { collections = c } }
        .task { for await m in vm.attachmentMessage { message = m } }
        .alert(message ?? "", isPresented: Binding(get: { message != nil }, set: { if !$0 { vm.clearAttachmentMessage() } })) {
            Button("OK", role: .cancel) {}
        }
    }

    private func content(_ place: Location) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                PlaceHero(place: place)

                VStack(alignment: .leading, spacing: 16) {
                    actions(place)
                    facts(place)
                    if let about = place.description_, !about.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("About").font(Theme.display(24))
                            Text(about).font(.body).foregroundStyle(.secondary)
                        }
                        .padding(.horizontal, 4)
                    }
                    if place.images.count > 1 {
                        photos(place)
                    }
                }
                .padding(EdgeInsets(top: 20, leading: 16, bottom: 120, trailing: 16))
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.systemGroupedBackground), in: UnevenRoundedRectangle(topLeadingRadius: 34, topTrailingRadius: 34))
                .padding(.top, -30)
            }
        }
        .ignoresSafeArea(edges: .top)
        .toolbarBackgroundVisibility(.hidden, for: .navigationBar)
        .toolbar {
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button { vm.shareLocation(location: place) } label: { Image(systemName: "square.and.arrow.up") }
                    .accessibilityLabel("Share")
                NavigationLink { ComingSoon(title: "Edit \(place.name)") } label: { Image(systemName: "pencil") }
                    .accessibilityLabel("Edit")
            }
        }
        .refreshable { vm.loadLocation(locationId: placeId) }
    }

    // MARK: Actions

    /// Directions, then the round buttons; at the accessibility sizes Directions takes a row of
    /// its own so its label isn't broken.
    private func actions(_ place: Location) -> some View {
        let layout = typeSize.isAccessibilitySize
            ? AnyLayout(VStackLayout(alignment: .leading, spacing: 10))
            : AnyLayout(HStackLayout(spacing: 10))
        return layout {
            if let coordinate = coordinate(place) {
                Button { directions(to: coordinate, name: place.name) } label: {
                    Label("Directions", systemImage: "location.fill")
                        .font(.headline)
                        .frame(maxWidth: .infinity, minHeight: 50)
                }
                .buttonStyle(.glassProminent)
                .tint(Theme.accent)
            }
            HStack(spacing: 10) { roundButtons(place) }
        }
        .tint(Theme.accent)
        .dynamicTypeSize(...DynamicTypeSize.accessibility2)
    }

    @ViewBuilder
    private func roundButtons(_ place: Location) -> some View {
            NavigationLink { ComingSoon(title: "New visit") } label: {
                Image(systemName: "plus").font(.title3.weight(.semibold)).frame(width: 50, height: 50)
            }
            .buttonStyle(.glass)
            .accessibilityLabel("Add a visit")
            if let link = place.link.flatMap(URL.init(string:)) {
                Button { openURL(link) } label: {
                    Image(systemName: "globe").font(.title3.weight(.semibold)).frame(width: 50, height: 50)
                }
                .buttonStyle(.glass)
                .accessibilityLabel("Website")
            }
    }

    private func directions(to coordinate: CLLocationCoordinate2D, name: String) {
        let item = MKMapItem(location: CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude), address: nil)
        item.name = name
        item.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDefault])
    }

    // MARK: Facts

    @ViewBuilder
    private func facts(_ place: Location) -> some View {
        let lastVisit = place.visits.compactMap { $0.startDate }.max().flatMap(DayRange.day)
        if lastVisit != nil || !collections.isEmpty {
            VStack(spacing: 0) {
                if let lastVisit {
                    FactRow(systemImage: "calendar", tint: .orange, title: "Last visit") {
                        Text(lastVisit.formatted(.dateTime.day().month(.abbreviated).year()))
                    }
                }
                if lastVisit != nil, !collections.isEmpty {
                    Divider().padding(.leading, 58)
                }
                if !collections.isEmpty {
                    NavigationLink {
                        ComingSoon(title: collections.count == 1 ? collections[0].name : "Collections")
                    } label: {
                        FactRow(systemImage: "rectangle.stack", tint: .indigo, title: collections.count == 1 ? "Collection" : "Collections") {
                            HStack(spacing: 6) {
                                Text(collections.count == 1 ? collections[0].name : "\(collections.count)")
                                Image(systemName: "chevron.right").font(.footnote.weight(.semibold)).foregroundStyle(.tertiary)
                            }
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
            .background(Color(.secondarySystemGroupedBackground), in: .rect(cornerRadius: Theme.Radius.card))
        }
    }

    // MARK: Photos

    private func photos(_ place: Location) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Photos").font(Theme.display(24)).padding(.horizontal, 4)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    ForEach(place.images, id: \.id) { image in
                        RemoteImage(url: image.image) { Rectangle().fill(Color(.tertiarySystemFill)) }
                            .frame(width: 150, height: 112)
                            .clipShape(.rect(cornerRadius: 18))
                            .accessibilityHidden(true)
                    }
                }
                .padding(.horizontal, 16)
            }
            .padding(.horizontal, -16)
        }
    }

    private func coordinate(_ place: Location) -> CLLocationCoordinate2D? {
        guard let lat = place.latitude.flatMap(Double.init), let lon = place.longitude.flatMap(Double.init) else { return nil }
        return CLLocationCoordinate2D(latitude: lat, longitude: lon)
    }
}

/// The photo, the chips over it and the name in serif.
private struct PlaceHero: View {
    let place: Location

    var body: some View {
        RemoteImage(url: primaryPhoto(place)) {
            LinearGradient(colors: [Color(red: 0.50, green: 0.74, blue: 0.88), Color(red: 0.06, green: 0.29, blue: 0.37)], startPoint: .top, endPoint: .bottom)
                .overlay(Text(place.category?.icon ?? "").font(.system(size: 64)))
        }
        .frame(height: 420)
        .frame(maxWidth: .infinity)
        .overlay {
            LinearGradient(colors: [.black.opacity(0.25), .clear, .clear, .black.opacity(0.55)], startPoint: .top, endPoint: .bottom)
        }
        .overlay(alignment: .bottomLeading) {
            VStack(alignment: .leading, spacing: 8) {
                chips
                Text(place.name)
                    .font(Theme.display(38))
                    .lineLimit(3)
                if !whereabouts.isEmpty {
                    Text(whereabouts).font(.callout.weight(.semibold)).opacity(0.92)
                }
            }
            .foregroundStyle(.white)
            .padding(.horizontal, 20)
            .padding(.bottom, 50)
        }
        .accessibilityElement(children: .combine)
    }

    private var chips: some View {
        HStack(spacing: 6) {
            if let rating = place.rating?.doubleValue, rating > 0 {
                Chip { Label(rating.formatted(.number.precision(.fractionLength(1))), systemImage: "star.fill").labelStyle(TintedIcon(tint: .yellow)) }
            }
            if let category = place.category {
                Chip { Text(category.displayName) }
            }
            Chip {
                Label(place.isVisited ? "Visited" : "Planned", systemImage: place.isVisited ? "checkmark" : "calendar")
            }
        }
        .font(.footnote.weight(.semibold))
        .dynamicTypeSize(...DynamicTypeSize.accessibility1)
    }

    /// "Vestland, Norway": region and country, or what the place was saved with.
    private var whereabouts: String {
        let parts = [place.region?.name, place.country?.name].compactMap { $0 }.filter { !$0.isEmpty }
        return parts.isEmpty ? (place.location ?? "") : parts.joined(separator: ", ")
    }
}

/// A glass capsule over the photo.
private struct Chip<Content: View>: View {
    @ViewBuilder let content: Content
    var body: some View {
        content
            .padding(.horizontal, 10)
            .frame(minHeight: 28)
            .glassEffect(.regular.tint(.black.opacity(0.2)), in: .capsule)
    }
}

private struct TintedIcon: LabelStyle {
    let tint: Color
    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 4) { configuration.icon.foregroundStyle(tint); configuration.title }
    }
}

/// A row of the facts card: a coloured icon square, its name, and its value on the trailing side.
private struct FactRow<Value: View>: View {
    let systemImage: String
    let tint: Color
    let title: String
    @ViewBuilder let value: Value

    var body: some View {
        ViewThatFits(in: .horizontal) {
            HStack(spacing: 12) {
                icon
                Text(title)
                Spacer(minLength: 8)
                value.foregroundStyle(.secondary).lineLimit(1)
            }
            HStack(alignment: .top, spacing: 12) {
                icon
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                    value.foregroundStyle(.secondary)
                }
                Spacer(minLength: 0)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .frame(minHeight: 56)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }

    private var icon: some View {
        Image(systemName: systemImage)
            .font(.system(size: 15, weight: .semibold))
            .foregroundStyle(.white)
            .frame(width: 30, height: 30)
            .background(tint, in: .rect(cornerRadius: 8))
    }
}
