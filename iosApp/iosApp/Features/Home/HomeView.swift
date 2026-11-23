import SwiftUI
import ComposeApp

/// Home, as the iOS-Home artboard draws it: the next trip's photo filling the top with its name
/// in serif over it, then a sheet rising over the photo with the library's three counts and the
/// places visited most recently.
struct HomeView: View {
    let open: (AppTab) -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state: HomeUiState = HomeUiStateLoading.shared
    @State private var user: UserDetails?
    @State private var showAccount = false
    @Environment(\.dynamicTypeSize) private var typeSize

    /// At the accessibility sizes rows become columns, so labels wrap instead of truncating.
    private var stacked: AnyLayout {
        typeSize.isAccessibilitySize ? AnyLayout(VStackLayout(alignment: .leading, spacing: 10)) : AnyLayout(HStackLayout(spacing: 10))
    }

    private var vm: HomeViewModel { host.owner.homeViewModel() }

    var body: some View {
        Group {
            switch onEnum(of: state) {
            case .loading:
                ProgressView().controlSize(.large)
            case .error(let e):
                ContentUnavailableView {
                    Label("Couldn't load home", systemImage: "wifi.exclamationmark")
                } description: {
                    Text(e.message)
                } actions: {
                    Button("Try again") { vm.loadDashboard() }.buttonStyle(.glassProminent)
                }
            case .success(let s):
                content(s)
            }
        }
        .toolbar(.hidden, for: .navigationBar)
        .background(Color(.systemGroupedBackground))
        .task { for await s in vm.uiState { state = s } }
        .task { for await u in vm.userDetails { user = u } }
        .sheet(isPresented: $showAccount) {
            AccountSheet(user: user, onSignOut: { vm.logout() })
        }
    }

    private func content(_ s: HomeUiStateSuccess) -> some View {
        let dashboard = s.dashboard
        let trip = dashboard.activeTrip ?? dashboard.upcomingTrips.first
        return ScrollView {
            VStack(spacing: 0) {
                Hero(
                    trip: trip,
                    isActive: dashboard.activeTrip != nil,
                    greetingName: s.userName,
                    // With no trip ahead, the latest place's photo carries the top instead.
                    fallbackPhoto: dashboard.recentLocations.first.flatMap(primaryPhoto)
                )
                .overlay(alignment: .topTrailing) { heroButtons.padding(.horizontal, 16).padding(.top, 58) }
                    .onTapGesture { if trip != nil { open(.collections) } }

                VStack(alignment: .leading, spacing: 0) {
                    stacked {
                        StatTile(value: dashboard.stats.locationCount, label: "Places", systemImage: "mappin", tint: Theme.accent) { open(.places) }
                        NavigationLink { ComingSoon(title: "World") } label: {
                            StatTileLabel(value: dashboard.stats.visitedCountryCount, label: "Countries", systemImage: "globe", tint: .orange)
                        }
                        .buttonStyle(.plain)
                        StatTile(value: dashboard.stats.tripsCount, label: "Collections", systemImage: "rectangle.stack", tint: .indigo) { open(.collections) }
                    }

                    (typeSize.isAccessibilitySize ? AnyLayout(VStackLayout(alignment: .leading, spacing: 4)) : AnyLayout(HStackLayout(alignment: .firstTextBaseline))) {
                        Text("Recently Visited").font(Theme.display(24))
                        Spacer(minLength: 0)
                        Button("See All") { open(.places) }
                    }
                    .padding(.horizontal, 4)
                    .padding(.top, 24)
                    .padding(.bottom, 12)

                    if dashboard.recentLocations.isEmpty {
                        Text("Places you visit show up here.")
                            .foregroundStyle(.secondary)
                            .padding(.horizontal, 4)
                    } else {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 12) {
                                ForEach(dashboard.recentLocations, id: \.id) { place in
                                    RecentPlaceCard(place: place)
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                        .padding(.horizontal, -16)
                    }
                }
                .padding(EdgeInsets(top: 22, leading: 16, bottom: 120, trailing: 16))
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(.systemGroupedBackground), in: UnevenRoundedRectangle(topLeadingRadius: 34, topTrailingRadius: 34))
                .padding(.top, -28)
            }
        }
        .ignoresSafeArea(edges: .top)
        .refreshable { vm.refreshDashboard() }
    }

    private var heroButtons: some View {
        HStack(spacing: 10) {
            Spacer()
            Button { showAccount = true } label: {
                Text(initials)
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(.white)
                    .frame(width: 44, height: 44)
            }
            // A fixed 44-point circle: two initials can't grow with the text.
            .dynamicTypeSize(...DynamicTypeSize.xLarge)
            // Tinted so white initials read over a pale sky as well as a dark sea.
            .glassEffect(.regular.tint(.black.opacity(0.25)).interactive(), in: .circle)
            .accessibilityLabel("Account")
        }
    }

    private var initials: String {
        let first = user?.firstName.first.map(String.init) ?? ""
        let last = user?.lastName.first.map(String.init) ?? ""
        let both = (first + last).uppercased()
        return both.isEmpty ? String((user?.username ?? "?").prefix(1)).uppercased() : both
    }
}

/// The top of Home: the trip's photo full-bleed, a glass pill saying when, its name in serif.
private struct Hero: View {
    let trip: UltraSlimCollection?
    let isActive: Bool
    let greetingName: String
    let fallbackPhoto: String?

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            RemoteImage(url: trip?.featuredImage ?? fallbackPhoto) {
                LinearGradient(colors: [Color(red: 0.50, green: 0.74, blue: 0.88), Color(red: 0.06, green: 0.29, blue: 0.37)], startPoint: .top, endPoint: .bottom)
            }
            .frame(height: 440)
            .frame(maxWidth: .infinity)
            .clipped()
            .overlay {
                LinearGradient(colors: [.clear, .black.opacity(0.45)], startPoint: .center, endPoint: .bottom)
            }

            VStack(alignment: .leading, spacing: 6) {
                if let trip {
                    Label(when(trip), systemImage: isActive ? "location.fill" : "airplane")
                        .font(.footnote.weight(.semibold))
                        .padding(.horizontal, 12)
                        .frame(height: 28)
                        .glassEffect(.regular.tint(.black.opacity(0.2)), in: .capsule)
                    Text(trip.name)
                        .font(Theme.display(40))
                        .lineLimit(2)
                    Text(subtitle(trip))
                        .font(.callout.weight(.medium))
                        .opacity(0.92)
                } else {
                    Text("Hello, \(greetingName.isEmpty ? "traveller" : greetingName)")
                        .font(Theme.display(40))
                    Text("No trip planned yet")
                        .font(.callout.weight(.medium))
                        .opacity(0.92)
                }
            }
            .foregroundStyle(.white)
            .padding(.horizontal, 20)
            .padding(.bottom, 52)
        }
        .accessibilityElement(children: .combine)
    }

    private func when(_ trip: UltraSlimCollection) -> String {
        if isActive { return "On this trip now" }
        guard let days = trip.daysUntilStart?.intValue else { return "Next trip" }
        switch days {
        case ...0: return "Next trip · today"
        case 1: return "Next trip · tomorrow"
        default: return "Next trip · in \(days) days"
        }
    }

    private func subtitle(_ trip: UltraSlimCollection) -> String {
        let places = trip.adventureCount == 1 ? "1 place" : "\(trip.adventureCount) places"
        guard let range = DayRange(start: trip.startDate, end: trip.endDate) else { return places }
        return "\(range.text) · \(places)"
    }
}

/// One of the three counts: a coloured icon square, the number in rounded type, its label.
private struct StatTile: View {
    let value: Int32
    let label: String
    let systemImage: String
    let tint: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            StatTileLabel(value: value, label: label, systemImage: systemImage, tint: tint)
        }
        .buttonStyle(.plain)
    }
}

private struct StatTileLabel: View {
    let value: Int32
    let label: String
    let systemImage: String
    let tint: Color
    @Environment(\.dynamicTypeSize) private var typeSize

    var body: some View {
            VStack(alignment: .leading, spacing: 8) {
                Image(systemName: systemImage)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(.white)
                    .frame(width: 30, height: 30)
                    .background(tint, in: .rect(cornerRadius: 9))
                VStack(alignment: .leading, spacing: 0) {
                    Text("\(value)").font(.system(size: 26, weight: .bold, design: .rounded))
                    Text(label).font(.footnote).foregroundStyle(.secondary).lineLimit(typeSize.isAccessibilitySize ? nil : 1).minimumScaleFactor(0.8)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
            .background(Color(.secondarySystemGroupedBackground), in: .rect(cornerRadius: Theme.Radius.card))
            .accessibilityElement(children: .combine)
    }
}

/// A recently visited place: its photo, and a glass caption with where and when.
private struct RecentPlaceCard: View {
    let place: Location
    @Environment(\.dynamicTypeSize) private var typeSize

    var body: some View {
        // Bigger text gets a bigger card, so the caption still leaves some of the photo.
        let large = typeSize.isAccessibilitySize
        RemoteImage(url: primaryPhoto(place)) {
            Rectangle().fill(Color(.tertiarySystemFill))
                .overlay(Image(systemName: "photo").foregroundStyle(.secondary))
        }
        .frame(width: large ? 300 : 190, height: large ? 240 : 142)
        .clipShape(.rect(cornerRadius: 24))
        .overlay(alignment: .bottom) {
            VStack(alignment: .leading, spacing: 1) {
                Text(place.name).font(.subheadline.weight(.semibold)).lineLimit(1)
                if !caption.isEmpty {
                    Text(caption).font(.caption).opacity(0.9).lineLimit(1)
                }
            }
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .frame(minHeight: 46)
            .glassEffect(.regular.tint(.black.opacity(0.25)), in: .rect(cornerRadius: 16))
            .padding(8)
            // Past this the caption would cover the whole photo.
            .dynamicTypeSize(...DynamicTypeSize.accessibility1)
        }
        .accessibilityElement(children: .combine)
    }

    /// "Norway · Jul 2026": the country, then the month of the latest visit.
    private var caption: String {
        let country = place.country?.name
        let latest = place.visits.compactMap { $0.startDate }.max()
        let month = latest.flatMap(DayRange.monthYear)
        return [country, month].compactMap { $0 }.joined(separator: " · ")
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
                                .font(Theme.display(24))
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

/// A place's cover: the photo marked primary, or its first.
func primaryPhoto(_ place: Location) -> String? {
    place.images.first(where: { $0.isPrimary })?.image ?? place.images.first?.image
}

/// Dates as the canvas writes them: "5–9 September", "28 August – 3 September", "Jul 2026".
enum DayRange {
    case range(Date, Date)

    init?(start: String?, end: String?) {
        guard let s = start.flatMap(DayRange.day) else { return nil }
        self = .range(s, end.flatMap(DayRange.day) ?? s)
    }

    var text: String {
        guard case let .range(a, b) = self else { return "" }
        let cal = Calendar.current
        let day = Date.FormatStyle().day()
        let dayMonth = Date.FormatStyle().day().month(.wide)
        if cal.isDate(a, inSameDayAs: b) { return a.formatted(dayMonth) }
        if cal.component(.month, from: a) == cal.component(.month, from: b) {
            return "\(a.formatted(day))–\(b.formatted(dayMonth))"
        }
        return "\(a.formatted(dayMonth)) – \(b.formatted(dayMonth))"
    }

    static func day(_ iso: String) -> Date? {
        let f = DateFormatter()
        f.calendar = Calendar(identifier: .gregorian)
        f.locale = Locale(identifier: "en_US_POSIX")
        f.dateFormat = "yyyy-MM-dd"
        return f.date(from: String(iso.prefix(10)))
    }

    static func monthYear(_ iso: String) -> String? {
        day(iso).map { $0.formatted(.dateTime.month(.abbreviated).year()) }
    }
}
