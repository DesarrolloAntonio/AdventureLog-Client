import SwiftUI
import ComposeApp

/// Home, as the Mist-Home artboard draws it: the library in one line under the app's name, search
/// in the bar, the next trip as a teal card counting the days down, four quick ways in, and the
/// places visited most recently.
struct HomeView: View {
    let open: (AppTab) -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state: HomeUiState = HomeUiStateLoading.shared
    @State private var user: UserDetails?
    @State private var showAccount = false
    @State private var searching = false
    @State private var openTrip: UltraSlimCollection?
    @Environment(\.dynamicTypeSize) private var typeSize

    private var vm: HomeViewModel { host.owner.homeViewModel() }

    var body: some View {
        // The app's name is the bar's large title, with the library's totals under it. A bar
        // without a large title made every screen pushed from Home open with its own title
        // folded into the bar; the canvas's "Your travels" label over the name goes with it.
        ScrollView {
            switch onEnum(of: state) {
            case .loading:
                ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 120)
            case .error(let e):
                ContentUnavailableView {
                    Label("Couldn't load home", systemImage: "wifi.exclamationmark")
                } description: {
                    Text(e.message)
                } actions: {
                    Button("Try again") { vm.loadDashboard() }.buttonStyle(.glassProminent)
                }
                .padding(.top, 60)
            case .success(let s):
                content(s.dashboard)
            }
        }
        .refreshable { vm.refreshDashboard() }
        .mistBackground()
        .navigationTitle("AdventureLog")
        .navigationSubtitle(subtitle)
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItemGroup(placement: .topBarTrailing) {
                // Search lives here, not in the tab bar: the bar is for going places, and new
                // places are added from Places and Collections, where they end up.
                Button { searching = true } label: { Image(systemName: "magnifyingglass") }
                    .accessibilityLabel("Search")
                Button { showAccount = true } label: {
                    Text(initials)
                        .font(.footnote.weight(.bold))
                        .foregroundStyle(Theme.accent)
                        .frame(width: 32, height: 32)
                        .background(Theme.Tone.mint.fill, in: .circle)
                }
                .accessibilityLabel("Account and settings")
            }
            .sharedBackgroundVisibility(.hidden)
        }
        .navigationDestination(isPresented: $searching) { SearchView() }
        .navigationDestination(item: $openTrip) { TripView(collectionId: $0.id, name: $0.name, subtitle: dates($0)) }
        .onReceive(NotificationCenter.default.publisher(for: .placeSaved)) { _ in vm.refreshDashboard() }
        .task { for await s in vm.uiState { state = s } }
        .task { for await u in vm.userDetails { user = u } }
        .navigationDestination(isPresented: $showAccount) { SettingsView(onSignOut: { vm.logout() }) }
    }

    /// The library in one line; a placeholder of the same height until it has loaded, so the
    /// large title doesn't change size under the reader.
    private var subtitle: String {
        guard case .success(let s) = onEnum(of: state) else { return "Your travels" }
        let stats = s.dashboard.stats
        return "\(plural(stats.locationCount, "place")) · \(plural(stats.visitedCountryCount, "country", "countries")) · \(plural(stats.tripsCount, "collection"))"
    }

    private func content(_ dashboard: Dashboard) -> some View {
            VStack(alignment: .leading, spacing: 0) {
                let trip = dashboard.activeTrip ?? dashboard.upcomingTrips.first
                TripCard(trip: trip, isActive: dashboard.activeTrip != nil) {
                    // The trip itself when there is one; Collections to plan one when there isn't.
                    if let trip { openTrip = trip } else { open(.collections) }
                }
                .padding(.top, 4)

                // Someone shared a collection: on Android's Home too, often a new account's first
                // reason to open the app.
                if dashboard.inviteCount > 0 {
                    Button {
                        InvitesRequest.pending = true
                        open(.collections)
                    } label: {
                        HStack(spacing: 12) {
                            ToneBadge(systemImage: "envelope.fill", tone: .blue)
                            Text(dashboard.inviteCount == 1
                                 ? "You have an invitation to a collection"
                                 : "You have \(dashboard.inviteCount) invitations to collections")
                                .font(.subheadline.weight(.semibold))
                                .foregroundStyle(Theme.ink)
                                .multilineTextAlignment(.leading)
                            Spacer(minLength: 8)
                            Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
                        }
                        .padding(14)
                        .mistCard()
                    }
                    .buttonStyle(.plain)
                    .padding(.top, 12)
                }

                SectionLabel(text: "Quick access").padding(.top, 20).padding(.bottom, 8)
                quickAccess(dashboard.stats)

                if !dashboard.recentLocations.isEmpty {
                    HStack(alignment: .firstTextBaseline) {
                        SectionLabel(text: "Recently visited")
                        Button("See all") { open(.places) }
                            .font(.footnote.weight(.semibold))
                            .padding(.trailing, 6)
                    }
                    .padding(.top, 20)
                    .padding(.bottom, 8)
                    recent(dashboard.recentLocations)
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 120)
    }

    private func quickAccess(_ stats: UserStats) -> some View {
        let columns = typeSize.isAccessibilitySize
            ? [GridItem(.flexible())]
            : [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)]
        return LazyVGrid(columns: columns, spacing: 10) {
            QuickTile(title: "Visited", systemImage: "checkmark", tone: .green, count: stats.visitedLocationCount) { open(.places) }
            QuickTile(title: "Planned", systemImage: "mappin", tone: .coral, count: stats.locationCount - stats.visitedLocationCount) { open(.places) }
            QuickTile(title: "Countries", systemImage: "globe", tone: .blue, count: stats.visitedCountryCount) { open(.world) }
            NavigationLink { CalendarView() } label: {
                QuickTileLabel(title: "Calendar", systemImage: "calendar", tone: .amber, count: nil)
            }
            .buttonStyle(.plain)
        }
    }

    /// A strip that runs off the screen's edge, like Android's: the photographs stay photographs
    /// instead of shrinking into a column of thumbnails.
    private func recent(_ places: [Location]) -> some View {
        ScrollView(.horizontal) {
            LazyHStack(alignment: .top, spacing: 10) {
                ForEach(places, id: \.id) { place in
                    NavigationLink { PlaceView(placeId: place.id, name: place.name, subtitle: whereabouts(place)) } label: {
                        RecentCard(place: place)
                    }
                    .buttonStyle(.plain)
                }
            }
            .scrollTargetLayout()
        }
        .scrollIndicators(.hidden)
        .scrollTargetBehavior(.viewAligned)
        .contentMargins(.horizontal, 16, for: .scrollContent)
        // Out to the screen's edges; the margins above keep the first card in line with the rest.
        .padding(.horizontal, -16)
        .scrollClipDisabled()
    }

    private var initials: String {
        let first = user?.firstName.first.map(String.init) ?? ""
        let last = user?.lastName.first.map(String.init) ?? ""
        let both = (first + last).uppercased()
        return both.isEmpty ? String((user?.username ?? "?").prefix(1)).uppercased() : both
    }
}

private func plural(_ n: Int32, _ one: String, _ many: String? = nil) -> String {
    plural(Int(n), one, many)
}

/// The next trip in teal: when it is, and how many days are left; or, on a trip, which day of it.
private struct TripCard: View {
    let trip: UltraSlimCollection?
    let isActive: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 7) {
                    Image(systemName: "calendar")
                    Text(eyebrow).lineLimit(2)
                    Spacer(minLength: 4)
                    Image(systemName: "chevron.right").fontWeight(.bold)
                }
                .font(.caption.weight(.bold))
                .tracking(0.9)
                .foregroundStyle(.white.opacity(0.9))
                .dynamicTypeSize(...DynamicTypeSize.xxxLarge)

                if let trip, let big = big(trip) {
                    HStack(alignment: .firstTextBaseline, spacing: 10) {
                        Text(big)
                            .font(.system(size: 44, weight: .heavy))
                            .tracking(-1.5)
                            .monospacedDigit()
                        Text(detail(trip))
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(.white.opacity(0.92))
                    }
                } else {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(trip?.name ?? "Nothing planned yet").font(.title2.weight(.heavy))
                        Text(trip == nil ? "Plan your next trip in Collections." : detail(trip!))
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(.white.opacity(0.92))
                    }
                }
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background { HeroGlow().clipShape(.rect(cornerRadius: Theme.Radius.hero)) }
            .heroCard()
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isButton)
    }

    private var eyebrow: String {
        guard let trip else { return "YOUR NEXT TRIP" }
        return (isActive ? "ON THIS TRIP · " : "NEXT TRIP · ") + trip.name.uppercased()
    }

    /// The big number: days to go, or the day of the trip it is.
    private func big(_ trip: UltraSlimCollection) -> String? {
        if isActive {
            guard let start = trip.startDate.flatMap(DayRange.day) else { return nil }
            let day = (Calendar.current.dateComponents([.day], from: start, to: .now).day ?? 0) + 1
            return "\(max(day, 1))"
        }
        guard let days = trip.daysUntilStart?.intValue, days > 0 else { return nil }
        return "\(days)"
    }

    private func detail(_ trip: UltraSlimCollection) -> String {
        let range = DayRange(start: trip.startDate, end: trip.endDate)?.shortText
        if isActive {
            return ["day of the trip", range].compactMap { $0 }.joined(separator: " · ")
        }
        let days = trip.daysUntilStart?.intValue ?? 0
        let lead = days == 1 ? "day to go" : days > 1 ? "days to go" : "starts today"
        return [lead, range].compactMap { $0 }.joined(separator: " · ")
    }
}

/// A quick way in: a soft badge, a name and its count.
private struct QuickTile: View {
    let title: String
    let systemImage: String
    let tone: Theme.Tone
    let count: Int32?
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            QuickTileLabel(title: title, systemImage: systemImage, tone: tone, count: count)
        }
        .buttonStyle(.plain)
    }
}

/// A tile the way Reminders draws its lists: the badge and the count on top, the word alone on
/// the line below with the whole tile's width, so it never shares room with the number.
private struct QuickTileLabel: View {
    let title: String
    let systemImage: String
    let tone: Theme.Tone
    let count: Int32?

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .center) {
                ToneBadge(systemImage: systemImage, tone: tone)
                Spacer(minLength: 4)
                if let count {
                    Text("\(count)").font(.title2.weight(.bold)).foregroundStyle(Theme.ink).monospacedDigit()
                } else {
                    Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
                }
            }
            Text(title)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(Theme.muted)
                .lineLimit(1)
                .minimumScaleFactor(0.85)
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .mistCard(radius: 18)
        .accessibilityElement(children: .combine)
    }
}

/// A recently visited place as a card: the photo at 4:3, then its name, where it is and when it
/// was last visited, and its rating and category. Every line is drawn, empty or not, so cards
/// side by side keep one height.
private struct RecentCard: View {
    let place: Location
    @Environment(\.colorScheme) private var scheme
    @ScaledMetric(relativeTo: .body) private var width: CGFloat = 176

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            RemoteImage(url: primaryPhoto(place)) {
                Rectangle().fill(Theme.Tone.teal.fill)
                    .overlay(Text(place.category?.icon ?? "📍").font(.largeTitle))
            }
            .aspectRatio(4 / 3, contentMode: .fit)

            VStack(alignment: .leading, spacing: 2) {
                Text(place.name).font(.subheadline.weight(.semibold)).foregroundStyle(Theme.ink)
                Text(caption.isEmpty ? " " : caption).font(.caption).foregroundStyle(Theme.muted)
                HStack(spacing: 6) {
                    let rating = Int(place.rating?.doubleValue ?? 0)
                    if rating > 0 {
                        Text(String(repeating: "★", count: min(rating, 5))).foregroundStyle(Theme.Tone.amber.ink)
                    }
                    Text(category).foregroundStyle(Theme.muted)
                }
                .font(.caption2)
                .padding(.top, 3)
            }
            .lineLimit(1)
            .padding(.horizontal, 10)
            .padding(.top, 8)
            .padding(.bottom, 10)
        }
        .frame(width: min(width, 260))
        // Clipped first, so the photo takes the corners; the card and its shadow go on outside
        // the clip, which would otherwise cut the shadow off.
        .clipShape(.rect(cornerRadius: 16, style: .continuous))
        .mistCard(radius: 16)
        // A deeper shadow than a plain card's, so the photos lift off the mist. It hangs off a
        // solid shape underneath: on the translucent card itself it would fall under the text too.
        .background {
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(scheme == .dark ? Color(white: 0.12) : .white)
                .shadow(color: Color(light: 0x0A4E66, dark: 0x000000).opacity(scheme == .dark ? 0.5 : 0.18), radius: 12, y: 7)
        }
        .contentShape(.rect(cornerRadius: 16))
        .accessibilityElement(children: .combine)
    }

    private var category: String {
        guard let c = place.category else { return " " }
        return "\(c.icon) \(c.displayName)".trimmingCharacters(in: .whitespaces)
    }

    private var caption: String {
        let latest = place.visits.compactMap { $0.startDate.flatMap(DayRange.day) }.max()
        // The year only when it isn't this one: "9 Nov" for a visit a year ago read as last fortnight.
        let when = latest.map {
            Calendar.current.isDate($0, equalTo: .now, toGranularity: .year)
                ? $0.formatted(.dateTime.day().month(.abbreviated))
                : $0.formatted(.dateTime.day().month(.abbreviated).year())
        }
        return [whereabouts(place), when].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " · ")
    }
}

