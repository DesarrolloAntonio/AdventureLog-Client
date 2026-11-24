import SwiftUI
import ComposeApp

/// One country, as the Mist-Country artboard draws it: its continent over the name, how many of
/// its regions are visited as the teal card with a bar, a search field, then the visited regions
/// and the rest, each with a tick to mark it.
struct CountryView: View {
    let country: Country

    @StateObject private var host = ViewModelHost()
    @State private var state = CountryDetailUiState(isLoading: true, error: nil, country: nil, regions: [], searchQuery: "", message: nil)
    @State private var query = ""

    private var vm: CountryDetailViewModel { host.owner.countryDetailViewModel() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if state.regions.isEmpty, state.isLoading {
                    ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 120)
                } else if state.regions.isEmpty, let error = state.error {
                    ContentUnavailableView {
                        Label("Couldn't load regions", systemImage: "wifi.exclamationmark")
                    } description: {
                        Text(error)
                    } actions: {
                        Button("Try again") { vm.retry() }.buttonStyle(.glassProminent)
                    }
                    .padding(.top, 60)
                } else {
                    hero
                    SearchField(text: $query, prompt: "Search regions").padding(.top, 16)
                    section("Visited", rows: state.filtered.filter(\.visited))
                    section("Not visited yet", rows: state.filtered.filter { !$0.visited })
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 120)
        }
        .mistBackground()
        .navigationBarTitleDisplayMode(.large)
        .navigationTitle(country.name)
        .navigationSubtitle(subtitle)
        .onChange(of: query) { _, v in vm.onSearchQueryChanged(query: v) }
        .alert(state.message ?? "", isPresented: Binding(get: { state.message != nil }, set: { if !$0 { vm.clearMessage() } })) {
            Button("OK", role: .cancel) {}
        }
        .task { vm.load(countryCode: country.countryCode) }
        .task { for await s in vm.uiState { state = s } }
    }

    /// "Europe · 3 of 15 regions" — the continent stands where the canvas puts it over the title.
    private var subtitle: String {
        let regions = state.regions.isEmpty ? "\(country.numVisits) of \(country.numRegions) regions" : "\(state.visitedCount) of \(state.regions.count) regions"
        return [Continent(subregion: country.subregion)?.name, regions].compactMap { $0 }.joined(separator: " · ")
    }

    private var hero: some View {
        let total = max(state.regions.count, 1)
        let done = Int(state.visitedCount)
        let share = Double(done) / Double(total)
        return VStack(alignment: .leading, spacing: 8) {
            Label("REGIONS VISITED", systemImage: "map")
                .font(.caption.weight(.bold)).tracking(0.9)
                .foregroundStyle(.white.opacity(0.9))
                .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text("\(done)").font(.system(size: 40, weight: .heavy)).monospacedDigit()
                Text("of \(state.regions.count) · \(share.formatted(.percent.precision(.fractionLength(0...1)))) of \(country.name)")
                    .font(.subheadline.weight(.semibold))
            }
            ProgressView(value: share)
                .tint(.white)
                .background(.white.opacity(0.25), in: .capsule)
                .accessibilityHidden(true)
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background { HeroGlow().clipShape(.rect(cornerRadius: Theme.Radius.hero)) }
        .heroCard()
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private func section(_ title: String, rows: [RegionRow]) -> some View {
        if !rows.isEmpty {
            SectionLabel(text: title, trailing: "\(rows.count)").padding(.top, 20).padding(.bottom, 8)
            LazyVStack(spacing: 0) {
                ForEach(Array(rows.enumerated()), id: \.element.region.id) { index, row in
                    RegionLine(row: row, last: index == rows.count - 1) { vm.onRegionToggled(regionId: row.region.id) }
                }
            }
            .mistCard()
        }
    }
}

/// A region: its tick, its name, and "Mark visited" on the ones not ticked yet.
private struct RegionLine: View {
    let row: RegionRow
    let last: Bool
    let toggle: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: toggle) {
                Image(systemName: row.visited ? "checkmark.circle.fill" : "circle")
                    .font(.title2)
                    .foregroundStyle(row.visited ? Theme.accent : Theme.glyph)
                    .frame(width: 32, height: 44)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(row.region.name)
            .accessibilityValue(row.visited ? "Visited" : "Not visited")
            .accessibilityHint(row.visited ? "Marks it not visited" : "Marks it visited")

            HStack {
                Text(row.region.name).foregroundStyle(Theme.ink)
                Spacer(minLength: 8)
                if !row.visited {
                    Button("Mark visited", action: toggle)
                        .font(.footnote.weight(.semibold))
                        .foregroundStyle(Theme.Tone.teal.ink)
                        .padding(.horizontal, 10)
                        .frame(minHeight: 28)
                        .background(Theme.Tone.teal.fill, in: .rect(cornerRadius: 8, style: .continuous))
                        .buttonStyle(.plain)
                        .accessibilityHidden(true)
                }
            }
            .frame(minHeight: 52)
            .overlay(alignment: .bottom) {
                if !last { Rectangle().fill(Theme.separator).frame(height: 0.5) }
            }
        }
        .padding(.horizontal, 14)
    }
}
