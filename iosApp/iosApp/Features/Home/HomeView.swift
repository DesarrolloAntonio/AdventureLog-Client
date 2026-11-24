import SwiftUI
import ComposeApp

/// Home, as the Mist-Home artboard draws it: the library in one line under the app's name, a
/// search field, the next trip as a teal card counting the days down, four quick ways in, and the
/// places visited most recently.
struct HomeView: View {
    let open: (AppTab) -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state: HomeUiState = HomeUiStateLoading.shared
    @State private var user: UserDetails?
    @State private var showAccount = false
    @State private var creating = false
    @Environment(\.dynamicTypeSize) private var typeSize

    private var vm: HomeViewModel { host.owner.homeViewModel() }

    var body: some View {
        Group {
            switch onEnum(of: state) {
            case .loading:
                ProgressView().controlSize(.large).frame(maxWidth: .infinity, maxHeight: .infinity)
            case .error(let e):
                ContentUnavailableView {
                    Label("Couldn't load home", systemImage: "wifi.exclamationmark")
                } description: {
                    Text(e.message)
                } actions: {
                    Button("Try again") { vm.loadDashboard() }.buttonStyle(.glassProminent)
                }
            case .success(let s):
                content(s.dashboard)
            }
        }
        .mistBackground()
        .toolbar {
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button { creating = true } label: { Image(systemName: "plus") }
                    .accessibilityLabel("New place")
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
        .navigationDestination(isPresented: $creating) { ComingSoon(title: "New Place") }
        .task { for await s in vm.uiState { state = s } }
        .task { for await u in vm.userDetails { user = u } }
        .sheet(isPresented: $showAccount) {
            AccountSheet(user: user, onSignOut: { vm.logout() })
        }
    }

    private func content(_ dashboard: Dashboard) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                header(dashboard.stats)

                Button { open(.search) } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "magnifyingglass").font(.subheadline.weight(.semibold))
                        Text("Search places and collections")
                        Spacer()
                    }
                    .foregroundStyle(Theme.muted)
                    .padding(.horizontal, 12)
                    .frame(minHeight: 40)
                    .mistCard(radius: Theme.Radius.field)
                }
                .buttonStyle(.plain)
                .padding(.top, 16)

                TripCard(trip: dashboard.activeTrip ?? dashboard.upcomingTrips.first, isActive: dashboard.activeTrip != nil) {
                    open(.collections)
                }
                .padding(.top, 16)

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
        .refreshable { vm.refreshDashboard() }
    }

    private func header(_ stats: UserStats) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("YOUR TRAVELS")
                .font(.caption.weight(.bold))
                .tracking(1)
                .foregroundStyle(Theme.muted)
                // A small label over the title: past this it would outgrow the title itself.
                .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
            Text("AdventureLog")
                .font(Theme.title())
                .tracking(-0.6)
                // One word: shrunk to fit rather than broken across two lines.
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .foregroundStyle(Theme.ink)
            Text("\(plural(stats.locationCount, "place")) · \(plural(stats.visitedCountryCount, "country", "countries")) · \(plural(stats.tripsCount, "collection"))")
                .font(.subheadline)
                .foregroundStyle(Theme.muted)
        }
        .padding(.horizontal, 4)
        .accessibilityElement(children: .combine)
    }

    private func quickAccess(_ stats: UserStats) -> some View {
        let columns = typeSize.isAccessibilitySize
            ? [GridItem(.flexible())]
            : [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)]
        return LazyVGrid(columns: columns, spacing: 10) {
            QuickTile(title: "Visited", systemImage: "checkmark", tone: .green, count: stats.visitedLocationCount) { open(.places) }
            QuickTile(title: "Planned", systemImage: "mappin", tone: .coral, count: stats.locationCount - stats.visitedLocationCount) { open(.places) }
            QuickTile(title: "Countries", systemImage: "globe", tone: .blue, count: stats.visitedCountryCount) { open(.world) }
            NavigationLink { ComingSoon(title: "Calendar") } label: {
                QuickTileLabel(title: "Calendar", systemImage: "calendar", tone: .amber, count: nil)
            }
            .buttonStyle(.plain)
        }
    }

    private func recent(_ places: [Location]) -> some View {
        VStack(spacing: 0) {
            ForEach(Array(places.prefix(5).enumerated()), id: \.element.id) { index, place in
                NavigationLink { PlaceView(placeId: place.id, name: place.name, subtitle: whereabouts(place)) } label: {
                    RecentRow(place: place, last: index == min(places.count, 5) - 1)
                }
                .buttonStyle(.plain)
            }
        }
        .mistCard()
    }

    private var initials: String {
        let first = user?.firstName.first.map(String.init) ?? ""
        let last = user?.lastName.first.map(String.init) ?? ""
        let both = (first + last).uppercased()
        return both.isEmpty ? String((user?.username ?? "?").prefix(1)).uppercased() : both
    }
}

private func plural(_ n: Int32, _ one: String, _ many: String? = nil) -> String {
    n == 1 ? "1 \(one)" : "\(n) \(many ?? one + "s")"
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
        let range = DayRange(start: trip.startDate, end: trip.endDate)?.text
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

private struct QuickTileLabel: View {
    let title: String
    let systemImage: String
    let tone: Theme.Tone
    let count: Int32?

    var body: some View {
        HStack(spacing: 10) {
            ToneBadge(systemImage: systemImage, tone: tone)
            Text(title).font(.subheadline.weight(.semibold)).foregroundStyle(Theme.ink)
            Spacer(minLength: 4)
            if let count {
                Text("\(count)").font(.footnote).foregroundStyle(Theme.faint).monospacedDigit()
            }
        }
        .padding(.horizontal, 12)
        .frame(maxWidth: .infinity, minHeight: 52)
        .mistCard(radius: 18)
        .accessibilityElement(children: .combine)
    }
}

/// A recently visited place: thumbnail, name, "Region, Country · 6 Jul", chevron.
private struct RecentRow: View {
    let place: Location
    let last: Bool

    var body: some View {
        HStack(spacing: 12) {
            RemoteImage(url: primaryPhoto(place)) {
                Rectangle().fill(Theme.Tone.teal.fill).overlay(Text(place.category?.icon ?? "📍"))
            }
            .frame(width: 40, height: 40)
            .clipShape(.rect(cornerRadius: 10, style: .continuous))

            HStack {
                VStack(alignment: .leading, spacing: 1) {
                    Text(place.name).font(.body.weight(.medium)).foregroundStyle(Theme.ink)
                    Text(caption).font(.footnote).foregroundStyle(Theme.muted)
                }
                Spacer(minLength: 8)
                Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
            }
            .padding(.vertical, 10)
            .frame(minHeight: 60)
            .overlay(alignment: .bottom) {
                if !last { Rectangle().fill(Theme.separator).frame(height: 0.5) }
            }
        }
        .padding(.leading, 12)
        .padding(.trailing, 14)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }

    private var caption: String {
        let latest = place.visits.compactMap { $0.startDate.flatMap(DayRange.day) }.max()
        let when = latest.map { $0.formatted(.dateTime.day().month(.abbreviated)) }
        return [whereabouts(place), when].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " · ")
    }
}

/// The account behind the avatar: who is signed in, and signing out (asked first).
private struct AccountSheet: View {
    let user: UserDetails?
    let onSignOut: () -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var confirming = false

    var body: some View {
        NavigationStack {
            List {
                if let user {
                    Section {
                        VStack(alignment: .leading, spacing: 2) {
                            Text([user.firstName, user.lastName].filter { !$0.isEmpty }.joined(separator: " "))
                                .font(.title2.weight(.heavy))
                            Text("@\(user.username)").foregroundStyle(.secondary)
                        }
                    }
                }
                Section {
                    Button("Sign Out", role: .destructive) { confirming = true }
                }
            }
            .navigationTitle("Account")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button("Done") { dismiss() } } }
            .confirmationDialog("Sign out?", isPresented: $confirming, titleVisibility: .visible) {
                Button("Sign Out", role: .destructive) { onSignOut(); dismiss() }
            } message: {
                Text("You will need your password to sign back in.")
            }
        }
        .presentationDetents([.medium])
    }
}
