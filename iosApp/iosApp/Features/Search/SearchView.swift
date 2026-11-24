import SwiftUI
import ComposeApp

/// The search tab: the system search field over everything the server finds, places and
/// collections first, each row opening what it names.
struct SearchView: View {
    @StateObject private var host = ViewModelHost()
    @State private var query = ""
    @State private var state: GlobalSearchState?

    private var vm: GlobalSearchViewModel { host.owner.globalSearchViewModel() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                content
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 120)
        }
        .mistBackground()
        .navigationTitle("Search")
        .navigationBarTitleDisplayMode(.large)
        // Always showing: the default hides the field until the list is pulled down.
        .searchable(text: $query, placement: .navigationBarDrawer(displayMode: .always), prompt: "Places, collections, notes")
        .onChange(of: query) { _, q in vm.onQueryChange(query: q) }
        .navigationDestination(for: SearchHit.self) { hit in
            if hit.type == SearchHit.companion.LOCATION {
                PlaceView(placeId: hit.id, name: hit.title, subtitle: hit.subtitle)
            } else {
                TripView(collectionId: hit.id, name: hit.title, subtitle: hit.subtitle)
            }
        }
        .task { for await s in vm.state { state = s } }
    }

    @ViewBuilder
    private var content: some View {
        if let state, state.hasSearched {
            if state.isSearching && state.hits.isEmpty {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 80)
            } else if let error = state.error {
                ContentUnavailableView("Couldn't search", systemImage: "wifi.exclamationmark", description: Text(error))
                    .padding(.top, 40)
            } else if state.hits.isEmpty {
                ContentUnavailableView.search(text: state.query).padding(.top, 40)
            } else {
                results(state.hits)
            }
        } else if let state, state.tooShort {
            hint("Keep typing", "Type at least two letters to search.")
        } else {
            hint("Search your travels", "Find places, collections, stays, journeys and notes by name.")
        }
    }

    private func hint(_ title: String, _ text: String) -> some View {
        ContentUnavailableView(title, systemImage: "magnifyingglass", description: Text(text))
            .padding(.top, 60)
    }

    /// Hits grouped by kind, in the order the server ranks the kinds.
    private func results(_ hits: [SearchHit]) -> some View {
        let kinds = hits.map(\.type).reduce(into: [String]()) { if !$0.contains($1) { $0.append($1) } }
        return VStack(alignment: .leading, spacing: 20) {
            ForEach(kinds, id: \.self) { kind in
                let group = hits.filter { $0.type == kind }
                VStack(alignment: .leading, spacing: 8) {
                    SectionLabel(text: SearchHitKt.searchTypeLabel(type: kind), trailing: "\(group.count)")
                    VStack(spacing: 0) {
                        ForEach(Array(group.enumerated()), id: \.element.id) { index, hit in
                            row(hit, last: index == group.count - 1)
                        }
                    }
                    .mistCard()
                }
            }
        }
    }

    @ViewBuilder
    private func row(_ hit: SearchHit, last: Bool) -> some View {
        let opens = hit.type == SearchHit.companion.LOCATION || hit.type == SearchHit.companion.COLLECTION
        let label = HStack(spacing: 12) {
            ToneBadge(systemImage: icon(hit.type), tone: tone(hit.type), size: 34)
            VStack(alignment: .leading, spacing: 1) {
                Text(hit.title).foregroundStyle(Theme.ink).lineLimit(2)
                if !hit.subtitle.isEmpty {
                    Text(hit.subtitle).font(.footnote).foregroundStyle(Theme.muted).lineLimit(2)
                }
            }
            Spacer(minLength: 8)
            if opens {
                Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .frame(minHeight: 58)
        .overlay(alignment: .bottom) {
            if !last { Rectangle().fill(Theme.separator).frame(height: 0.5).padding(.leading, 60) }
        }
        .contentShape(.rect)

        if opens {
            NavigationLink(value: hit) { label }.buttonStyle(.plain)
        } else {
            label.accessibilityElement(children: .combine)
        }
    }

    private func icon(_ type: String) -> String {
        switch type {
        case "location": "mappin"
        case "collection": "rectangle.stack"
        case "lodging": "bed.double"
        case "transportation": "airplane"
        case "note": "note.text"
        case "checklist": "checklist"
        case "region", "city": "map"
        case "country": "globe"
        default: "sparkles"
        }
    }

    private func tone(_ type: String) -> Theme.Tone {
        switch type {
        case "location": .teal
        case "collection": .blue
        case "lodging": .coral
        case "transportation": .blue
        case "note": .amber
        case "checklist": .mint
        default: .green
        }
    }
}
