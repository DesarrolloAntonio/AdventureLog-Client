import SwiftUI
import MapKit
import ComposeApp

/// One place, as the Mist-Place artboard draws it: its name as the title and where it is under
/// it, tags for its stars, category and visit state, its photos in a card you swipe, Directions
/// in teal beside Add visit, the details (last visit, collection, coordinates) and what it's about.
struct PlaceView: View {
    let placeId: String
    /// Shown as the title while the place loads.
    let name: String
    /// Where it is, shown under the title from the first frame. A subtitle that appears only once
    /// the place has loaded grows the large title after the scroll view has laid out, and the
    /// title stays folded into the bar.
    var subtitle: String = ""

    @StateObject private var host = ViewModelHost()
    @State private var state: LocationState = LocationState.Loading.shared
    @State private var collections: [UltraSlimCollection] = []
    @State private var message: String?
    @Environment(\.openURL) private var openURL
    @Environment(\.dynamicTypeSize) private var typeSize

    private var vm: AdventureDetailViewModel { host.owner.adventureDetailViewModel() }

    var body: some View {
        // One scroll view from the first frame: the large title only stays large when the screen
        // starts with the scroll view under it, not a spinner swapped for one.
        ScrollView {
            switch onEnum(of: state) {
            case .loading:
                ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 120)
            case .error(let e):
                ContentUnavailableView {
                    Label("Couldn't load \(name)", systemImage: "wifi.exclamationmark")
                } description: {
                    Text(e.message)
                } actions: {
                    Button("Try again") { vm.loadLocation(locationId: placeId) }.buttonStyle(.glassProminent)
                }
                .padding(.top, 60)
            case .success(let s):
                content(s.location)
            }
        }
        .navigationTitle(loaded?.name ?? name)
        .navigationSubtitle(loaded.map(whereabouts) ?? subtitle)
        .toolbar { if let place = loaded { toolbar(place) } }
        .refreshable { vm.loadLocation(locationId: placeId) }
        .mistBackground()
        .navigationBarTitleDisplayMode(.large)
        .task { vm.loadLocation(locationId: placeId) }
        .task { for await s in vm.locationState { state = s } }
        .task { for await c in vm.collections { collections = c } }
        .task { for await m in vm.attachmentMessage { message = m } }
        .alert(message ?? "", isPresented: Binding(get: { message != nil }, set: { if !$0 { vm.clearAttachmentMessage() } })) {
            Button("OK", role: .cancel) {}
        }
    }

    private var loaded: Location? {
        if case .success(let s) = onEnum(of: state) { return s.location }
        return nil
    }

    private func content(_ place: Location) -> some View {
            VStack(alignment: .leading, spacing: 0) {
                tags(place).padding(.horizontal, 4)

                if !place.images.isEmpty {
                    PhotoCarousel(urls: place.images.map(\.image))
                        .padding(.top, 14)
                }

                actions(place).padding(.top, 16)

                details(place)

                if let about = place.description_, !about.isEmpty {
                    SectionLabel(text: "About").padding(.top, 22).padding(.bottom, 8)
                    Text(about)
                        .font(.body)
                        .foregroundStyle(Theme.ink)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .mistCard()
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 2)
            .padding(.bottom, 120)
    }

    @ToolbarContentBuilder
    private func toolbar(_ place: Location) -> some ToolbarContent {
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button { vm.shareLocation(location: place) } label: { Image(systemName: "square.and.arrow.up") }
                    .accessibilityLabel("Share")
                Menu {
                    NavigationLink { ComingSoon(title: "Edit \(place.name)") } label: { Label("Edit", systemImage: "pencil") }
                    NavigationLink { ComingSoon(title: "New visit") } label: { Label("Add visit", systemImage: "plus") }
                    if let link = place.link.flatMap(URL.init(string:)) {
                        Button("Open website", systemImage: "safari") { openURL(link) }
                    }
                } label: {
                    Image(systemName: "ellipsis")
                }
                .accessibilityLabel("More")
            }
    }

    // MARK: Tags

    private func tags(_ place: Location) -> some View {
        FlowTags {
            if let rating = place.rating?.doubleValue, rating > 0 {
                ToneTag(text: rating.formatted(.number.precision(.fractionLength(1))), tone: .amber, systemImage: "star.fill")
            }
            if let category = place.category {
                ToneTag(text: category.displayName, tone: categoryTone(category.name))
            }
            visitTag(place)
        }
    }

    // MARK: Actions

    /// Directions in teal beside Add visit; at the accessibility sizes each takes a row.
    private func actions(_ place: Location) -> some View {
        let layout = typeSize.isAccessibilitySize
            ? AnyLayout(VStackLayout(spacing: 10))
            : AnyLayout(HStackLayout(spacing: 10))
        return layout {
            if let coordinate = coordinate(place) {
                Button { directions(to: coordinate, name: place.name) } label: {
                    Label("Directions", systemImage: "location.fill")
                        .font(.headline)
                        .frame(maxWidth: .infinity, minHeight: 52)
                        .background { HeroGlow().clipShape(.rect(cornerRadius: 18)) }
                        .heroCard(radius: 18)
                }
                .buttonStyle(.plain)
            }
            NavigationLink { ComingSoon(title: "New visit") } label: {
                HStack(spacing: 10) {
                    ToneBadge(systemImage: "plus", tone: .coral, size: 28)
                    Text("Add visit").font(.headline).foregroundStyle(Theme.ink)
                }
                .frame(maxWidth: .infinity, minHeight: 52)
                .mistCard(radius: 18)
            }
            .buttonStyle(.plain)
        }
    }

    private func directions(to coordinate: CLLocationCoordinate2D, name: String) {
        let item = MKMapItem(location: CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude), address: nil)
        item.name = name
        item.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDefault])
    }

    // MARK: Details

    @ViewBuilder
    private func details(_ place: Location) -> some View {
        let lastVisit = place.visits.compactMap { $0.startDate.flatMap(DayRange.day) }.max()
        let coordinate = coordinate(place)
        if lastVisit != nil || !collections.isEmpty || coordinate != nil {
            SectionLabel(text: "Details").padding(.top, 22).padding(.bottom, 8)
            VStack(spacing: 0) {
                if let lastVisit {
                    DetailRow(systemImage: "calendar", tone: .amber, title: "Last visit") {
                        Text(lastVisit.formatted(.dateTime.day().month(.abbreviated).year()))
                    }
                }
                if !collections.isEmpty {
                    if lastVisit != nil { RowSeparator() }
                    NavigationLink {
                        ComingSoon(title: collections.count == 1 ? collections[0].name : "Collections")
                    } label: {
                        DetailRow(systemImage: "rectangle.stack", tone: .mint, title: collections.count == 1 ? "Collection" : "Collections") {
                            HStack(spacing: 6) {
                                Text(collections.count == 1 ? collections[0].name : "\(collections.count)")
                                Image(systemName: "chevron.right").font(.footnote.weight(.semibold)).foregroundStyle(Theme.glyph)
                            }
                        }
                    }
                    .buttonStyle(.plain)
                }
                if let coordinate {
                    if lastVisit != nil || !collections.isEmpty { RowSeparator() }
                    DetailRow(systemImage: "mappin", tone: .blue, title: "Coordinates") {
                        Text(Self.format(coordinate)).monospacedDigit()
                    }
                    .textSelection(.enabled)
                }
            }
            .mistCard()
        }
    }

    /// "60.124° N, 6.740° E"
    private static func format(_ c: CLLocationCoordinate2D) -> String {
        let lat = abs(c.latitude).formatted(.number.precision(.fractionLength(3)))
        let lon = abs(c.longitude).formatted(.number.precision(.fractionLength(3)))
        return "\(lat)° \(c.latitude >= 0 ? "N" : "S"), \(lon)° \(c.longitude >= 0 ? "E" : "W")"
    }

    private func coordinate(_ place: Location) -> CLLocationCoordinate2D? {
        guard let lat = place.latitude.flatMap(Double.init), let lon = place.longitude.flatMap(Double.init) else { return nil }
        return CLLocationCoordinate2D(latitude: lat, longitude: lon)
    }
}

/// The photos in a rounded card, swiped through, with the system's page dots on a capsule.
private struct PhotoCarousel: View {
    let urls: [String]

    var body: some View {
        TabView {
            ForEach(urls, id: \.self) { url in
                RemoteImage(url: url) { Rectangle().fill(Theme.Tone.teal.fill) }
            }
        }
        .tabViewStyle(.page(indexDisplayMode: urls.count > 1 ? .always : .never))
        .indexViewStyle(.page(backgroundDisplayMode: .always))
        .frame(height: 210)
        .clipShape(.rect(cornerRadius: 22, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 22, style: .continuous).strokeBorder(.white.opacity(0.6)))
        .shadow(color: Color(light: 0x14465A, dark: 0x000000).opacity(0.12), radius: 11, y: 6)
        .accessibilityLabel(urls.count == 1 ? "Photo" : "\(urls.count) photos")
    }
}

/// Tags in a row that wraps when the text is large.
private struct FlowTags<Content: View>: View {
    @ViewBuilder let content: Content
    var body: some View {
        ViewThatFits(in: .horizontal) {
            HStack(spacing: 6) { content }
            VStack(alignment: .leading, spacing: 6) { content }
        }
    }
}

private struct RowSeparator: View {
    var body: some View {
        Rectangle().fill(Theme.separator).frame(height: 0.5).padding(.leading, 56)
    }
}

/// A row of the details card: a soft badge, its name, and its value on the trailing side.
private struct DetailRow<Value: View>: View {
    let systemImage: String
    let tone: Theme.Tone
    let title: String
    @ViewBuilder let value: Value

    var body: some View {
        ViewThatFits(in: .horizontal) {
            HStack(spacing: 12) {
                ToneBadge(systemImage: systemImage, tone: tone)
                Text(title).foregroundStyle(Theme.ink)
                Spacer(minLength: 8)
                value.foregroundStyle(Theme.muted).lineLimit(1)
            }
            HStack(alignment: .top, spacing: 12) {
                ToneBadge(systemImage: systemImage, tone: tone)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).foregroundStyle(Theme.ink)
                    value.foregroundStyle(Theme.muted)
                }
                Spacer(minLength: 0)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .frame(minHeight: 52)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}
