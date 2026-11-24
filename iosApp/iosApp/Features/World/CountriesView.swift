import SwiftUI
import ComposeApp

/// World › Countries, as the Mist-World artboard draws it: how many countries out of all of them
/// as the teal card, the visited ones by continent, then each visited country with how many of
/// its regions, as a bar. Search opens the whole list, visited or not.
struct CountriesView: View {
    @StateObject private var host = ViewModelHost()
    @State private var state = WorldUiState(isLoading: true, countries: [], filteredCountries: [], visitedCountriesCount: 0,
                                            completeCountriesCount: 0, partiallyVisitedCount: 0, totalCountriesCount: 0,
                                            selectedRegion: .all, searchQuery: "", filterMode: .all, error: nil)
    @State private var query = ""
    @State private var searching = false

    private var vm: WorldViewModel { host.owner.worldViewModel() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if state.countries.isEmpty, state.isLoading {
                    ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 120)
                } else if state.countries.isEmpty, let error = state.error {
                    ContentUnavailableView {
                        Label("Couldn't load countries", systemImage: "wifi.exclamationmark")
                    } description: {
                        Text(error)
                    } actions: {
                        Button("Try again") { vm.onRefresh() }.buttonStyle(.glassProminent)
                    }
                    .padding(.top, 60)
                } else {
                    // Under the large title rather than as the bar's subtitle, which the switch in
                    // the bar would show a second time.
                    Text(subtitle).font(.subheadline).foregroundStyle(Theme.muted).padding(.horizontal, 4)
                    hero.padding(.top, 14)
                    continents
                    visitedList
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 120)
        }
        .refreshable { vm.onRefresh() }
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { searching = true } label: { Image(systemName: "magnifyingglass") }
                    .accessibilityLabel("Search countries")
            }
        }
        .navigationDestination(isPresented: $searching) { AllCountriesView(vm: vm, state: state) }
        .navigationDestination(for: Country.self) { CountryView(country: $0) }
        .task { for await s in vm.uiState { state = s } }
    }

    private var visited: [Country] {
        state.countries.filter { $0.numVisits > 0 }.sorted { $0.numVisits > $1.numVisits }
    }

    /// "6 countries · 13 regions", with a placeholder of the same height until loaded.
    private var subtitle: String {
        guard !state.countries.isEmpty else { return "Countries and regions" }
        let regions = state.countries.reduce(0) { $0 + Int($1.numVisits) }
        return "\(plural(visited.count, "country", "countries")) · \(plural(regions, "region"))"
    }

    private var hero: some View {
        let total = max(state.countries.count, 1)
        let share = Double(visited.count) / Double(total)
        return VStack(alignment: .leading, spacing: 8) {
            Label("COUNTRIES VISITED", systemImage: "globe")
                .font(.caption.weight(.bold)).tracking(0.9)
                .foregroundStyle(.white.opacity(0.9))
                .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text("\(visited.count)").font(.system(size: 44, weight: .heavy)).monospacedDigit()
                Text("of \(state.countries.count)").font(.headline)
            }
            Text("\(share.formatted(.percent.precision(.fractionLength(1)))) of the world")
                .font(.footnote.weight(.medium)).foregroundStyle(.white.opacity(0.9))
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(alignment: .trailing) {
            Image(systemName: "globe")
                .font(.system(size: 150, weight: .ultraLight))
                .foregroundStyle(.white.opacity(0.18))
                .offset(x: 30)
                .accessibilityHidden(true)
        }
        .background { HeroGlow().clipShape(.rect(cornerRadius: Theme.Radius.hero)) }
        .clipShape(.rect(cornerRadius: Theme.Radius.hero))
        .heroCard()
        .accessibilityElement(children: .combine)
    }

    // MARK: By continent

    @ViewBuilder
    private var continents: some View {
        let groups = Continent.allCases.compactMap { c -> (Continent, Int)? in
            let n = visited.filter { Continent(subregion: $0.subregion) == c }.count
            return n > 0 ? (c, n) : nil
        }
        if !groups.isEmpty {
            SectionLabel(text: "By continent").padding(.top, 20).padding(.bottom, 8)
            LazyVGrid(columns: [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)], spacing: 10) {
                ForEach(groups, id: \.0) { continent, count in
                    HStack(spacing: 10) {
                        CodeBadge(code: continent.code, tone: continent.tone)
                        Text(continent.name).font(.subheadline.weight(.semibold)).foregroundStyle(Theme.ink)
                            .lineLimit(1).minimumScaleFactor(0.8)
                        Spacer(minLength: 4)
                        Text("\(count)").font(.footnote).foregroundStyle(Theme.faint).monospacedDigit()
                    }
                    .padding(.horizontal, 12)
                    .frame(minHeight: 52)
                    .mistCard(radius: 18)
                    .accessibilityElement(children: .combine)
                }
            }
        }
    }

    // MARK: Visited

    @ViewBuilder
    private var visitedList: some View {
        if visited.isEmpty {
            ContentUnavailableView("No countries yet", systemImage: "globe",
                                   description: Text("Countries show up here as you mark their regions visited."))
                .padding(.top, 20)
        } else {
            SectionLabel(text: "Visited", trailing: "\(visited.count)").padding(.top, 20).padding(.bottom, 8)
            VStack(spacing: 0) {
                ForEach(Array(visited.enumerated()), id: \.element.id) { index, country in
                    NavigationLink(value: country) {
                        CountryRow(country: country, last: index == visited.count - 1)
                    }
                    .buttonStyle(.plain)
                }
            }
            .mistCard()
        }
    }
}

/// Every country, searchable; visited ones show their share of regions.
private struct AllCountriesView: View {
    let vm: WorldViewModel
    @State var state: WorldUiState
    @State private var query = ""

    var body: some View {
        List {
            ForEach(state.filteredCountries, id: \.id) { country in
                NavigationLink(value: country) {
                    HStack(spacing: 12) {
                        Text(flag(country.countryCode)).font(.title2)
                        VStack(alignment: .leading, spacing: 1) {
                            Text(country.name).foregroundStyle(Theme.ink)
                            Text(country.numVisits > 0 ? "\(country.numVisits) of \(country.numRegions) regions" : (country.subregion ?? ""))
                                .font(.footnote).foregroundStyle(Theme.muted)
                        }
                    }
                }
                .listRowBackground(Color.clear)
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .mistBackground()
        .navigationTitle("Countries")
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: "Search countries")
        .onChange(of: query) { _, v in vm.onSearchQueryChanged(query: v) }
        .onDisappear { vm.onSearchQueryChanged(query: "") }
        .task { for await s in vm.uiState { state = s } }
    }
}

/// A visited country: its code, name, "3 of 15 regions" and a bar of how far through it is.
private struct CountryRow: View {
    let country: Country
    let last: Bool

    var body: some View {
        HStack(spacing: 12) {
            CodeBadge(code: country.countryCode, tone: Continent(subregion: country.subregion)?.tone ?? .teal, size: 34)
            VStack(alignment: .leading, spacing: 6) {
                HStack(alignment: .firstTextBaseline) {
                    Text(country.name).foregroundStyle(Theme.ink)
                    Spacer(minLength: 8)
                    Text("\(country.numVisits) of \(country.numRegions) regions")
                        .font(.footnote).foregroundStyle(Theme.muted).monospacedDigit()
                }
                ProgressView(value: Double(country.numVisits), total: Double(max(country.numRegions, 1)))
                    .tint(Theme.accent)
                    .accessibilityHidden(true)
            }
            .padding(.vertical, 12)
            .overlay(alignment: .bottom) {
                if !last { Rectangle().fill(Theme.separator).frame(height: 0.5) }
            }
        }
        .padding(.horizontal, 14)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

/// A two-letter code in a soft circle, as the canvas marks countries and continents.
struct CodeBadge: View {
    let code: String
    let tone: Theme.Tone
    var size: CGFloat = 30

    var body: some View {
        Text(code.uppercased())
            .font(.system(size: size * 0.36, weight: .bold))
            .foregroundStyle(tone.ink)
            .frame(width: size, height: size)
            .background(tone.fill, in: .circle)
            .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
            .accessibilityHidden(true)
    }
}

/// The continents the canvas groups by, read from a country's UN subregion.
enum Continent: CaseIterable {
    case europe, asia, africa, americas, oceania

    init?(subregion: String?) {
        guard let s = subregion?.lowercased() else { return nil }
        if s.contains("europe") { self = .europe }
        else if s.contains("asia") { self = .asia }
        else if s.contains("africa") { self = .africa }
        else if s.contains("america") || s.contains("caribbean") { self = .americas }
        else if ["australia", "zealand", "polynesia", "melanesia", "micronesia"].contains(where: s.contains) { self = .oceania }
        else { return nil }
    }

    var name: String {
        switch self {
        case .europe: "Europe"
        case .asia: "Asia"
        case .africa: "Africa"
        case .americas: "Americas"
        case .oceania: "Oceania"
        }
    }

    var code: String {
        switch self {
        case .europe: "EU"
        case .asia: "AS"
        case .africa: "AF"
        case .americas: "AM"
        case .oceania: "OC"
        }
    }

    var tone: Theme.Tone {
        switch self {
        case .europe: .mint
        case .asia: .blue
        case .africa: .amber
        case .americas: .coral
        case .oceania: .green
        }
    }
}

/// The flag emoji for an ISO country code: "NO" → 🇳🇴.
func flag(_ code: String) -> String {
    code.uppercased().unicodeScalars.compactMap { UnicodeScalar(127397 + $0.value) }.map(String.init).joined()
}

func plural(_ n: Int, _ one: String, _ many: String? = nil) -> String {
    n == 1 ? "1 \(one)" : "\(n) \(many ?? one + "s")"
}
