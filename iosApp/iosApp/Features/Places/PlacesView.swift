import SwiftUI
import ComposeApp

/// Places, as the Mist-Places artboard draws it: the title with the library's totals, a search
/// field, filter chips (visit state, then categories), and the places in one frosted card, each
/// with its photo, where it is, its stars and its tags. The map of them lives in World.
struct PlacesView: View {
    @StateObject private var host = ViewModelHost()
    @State private var places: [Location] = []
    @State private var status = PagingStatus(isRefreshing: true, isLoadingMore: false, endReached: false, error: nil)
    @State private var counts: UserStats?
    @State private var filters: LocationFilters?
    @State private var categories: [ComposeApp.Category] = []
    @State private var query = ""
    @State private var showFilters = false
    @State private var creating = false
    @State private var message: String?
    @State private var deleting: Location?

    private var vm: LocationsViewModel { host.owner.locationsViewModel() }
    private var presenter: PagingListPresenter<Location> { host.owner.locationsPresenter() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                SearchField(text: $query, prompt: "Search places") { submit() }
                chips
                content
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 120)
        }
        .mistBackground()
        .navigationTitle("Places")
        .navigationSubtitle(subtitle)
        .toolbar {
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button { showFilters = true } label: { Image(systemName: "arrow.up.arrow.down") }
                    .accessibilityLabel(activeFilters > 0 ? "Sort and filter, \(activeFilters) on" : "Sort and filter")
                    .badge(activeFilters)
                Button { creating = true } label: { Image(systemName: "plus") }
                    .accessibilityLabel("New place")
            }
        }
        .navigationDestination(for: Location.self) { PlaceView(placeId: $0.id, name: $0.name, subtitle: whereabouts($0)) }
        .sheet(isPresented: $creating) { PlaceFormView {} }
        .sheet(isPresented: $showFilters) {
            if let filters { PlacesFilterSheet(vm: vm, initial: filters) }
        }
        .refreshable {
            presenter.refresh()
            vm.loadLibraryCounts()
        }
        .onChange(of: query) { _, v in
            vm.onSearchQueryChange(query: v)
            // Emptying the field brings every place back without a Return.
            if v.isEmpty { vm.executeSearch() }
        }
        .alert(message ?? "", isPresented: Binding(get: { message != nil }, set: { if !$0 { vm.clearActionMessage() } })) {
            Button("OK", role: .cancel) {}
        }
        .confirmationDialog(
            "Delete \(deleting?.name ?? "")?",
            isPresented: Binding(get: { deleting != nil }, set: { if !$0 { deleting = nil } }),
            titleVisibility: .visible,
            presenting: deleting
        ) { place in
            Button("Delete", role: .destructive) { vm.deleteAdventure(adventureId: place.id) }
        } message: { _ in
            Text("Its visits and photos go with it. This can't be undone.")
        }
        // A place saved anywhere, here or from its own page, reloads the list and its counts.
        .onReceive(NotificationCenter.default.publisher(for: .placeSaved)) { _ in
            presenter.refresh()
            vm.loadLibraryCounts()
        }
        .task { for await p in presenter.items { places = p } }
        .task { for await s in presenter.status { status = s } }
        .task { for await c in vm.libraryCounts { counts = c } }
        .task { for await f in vm.filters { filters = f } }
        .task { for await c in vm.categories { categories = c } }
        .task { for await m in vm.actionMessage { message = m } }
        .task {
            // A deleted place leaves the list at once; the totals follow, here and on Home.
            for await d in vm.deleteState {
                if case .success = onEnum(of: d) {
                    NotificationCenter.default.post(name: .placeSaved, object: nil)
                    vm.clearDeleteState()
                }
            }
        }
    }

    private func submit() {
        vm.onSearchQueryChange(query: query)
        vm.executeSearch()
    }

    private var subtitle: String {
        guard let c = counts else { return "" }
        let planned = c.locationCount - c.visitedLocationCount
        return "\(c.locationCount) places · \(c.visitedLocationCount) visited · \(planned) planned"
    }

    // MARK: Chips

    private var chips: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                Chip(title: "All", selected: visited == .all) { setVisited(.all) }
                Chip(title: "Visited", selected: visited == .visited) { setVisited(.visited) }
                Chip(title: "Planned", selected: visited == .notVisited) { setVisited(.notVisited) }
                ForEach(categories, id: \.id) { category in
                    Chip(title: category.displayName, selected: filters?.categoryNames.contains(category.name) == true) {
                        toggle(category.name)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 4)
        }
        .padding(.horizontal, -16)
    }

    private var visited: VisitedFilter { filters?.visitedFilter ?? .all }

    private func setVisited(_ v: VisitedFilter) {
        guard let f = filters else { return }
        vm.onFiltersChanged(filters: f.doCopy(
            categoryNames: f.categoryNames, sortField: f.sortField, sortDirection: f.sortDirection,
            visitedFilter: v, includeCollections: f.includeCollections
        ))
    }

    private func toggle(_ category: String) {
        guard let f = filters else { return }
        var names = f.categoryNames
        if let i = names.firstIndex(of: category) { names.remove(at: i) } else { names.append(category) }
        vm.onFiltersChanged(filters: f.doCopy(
            categoryNames: names, sortField: f.sortField, sortDirection: f.sortDirection,
            visitedFilter: f.visitedFilter, includeCollections: f.includeCollections
        ))
    }

    /// What the sort sheet has changed: sort and collections. Visit state and categories show as
    /// chips on screen already.
    private var activeFilters: Int {
        guard let f = filters else { return 0 }
        var n = 0
        if f.sortField != .updatedAt || f.sortDirection != .descending { n += 1 }
        if !f.includeCollections { n += 1 }
        return n
    }

    private var isFiltered: Bool {
        guard let f = filters else { return false }
        return f.visitedFilter != .all || !f.categoryNames.isEmpty || !f.includeCollections || !query.isEmpty
    }

    // MARK: List

    /// The label over the card says what the order is; the count is the library's, and only
    /// while categories or a search don't narrow it past what the totals know.
    private var heading: (String, String?) {
        let label: String = switch filters?.sortField {
        case .visitDate: "By visit date"
        case .name: "A–Z"
        case .rating: "By rating"
        default: "Recently updated"
        }
        guard let c = counts, let f = filters, f.categoryNames.isEmpty, query.isEmpty else { return (label, nil) }
        let n: Int32 = switch f.visitedFilter {
        case .visited: c.visitedLocationCount
        case .notVisited: c.locationCount - c.visitedLocationCount
        default: c.locationCount
        }
        return (label, "\(n)")
    }

    @ViewBuilder
    private var content: some View {
        if places.isEmpty, status.isRefreshing {
            ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 80)
        } else if places.isEmpty, let error = status.error {
            ContentUnavailableView {
                Label("Couldn't load places", systemImage: "wifi.exclamationmark")
            } description: {
                Text(error)
            } actions: {
                Button("Try again") { presenter.retry() }.buttonStyle(.glassProminent)
            }
        } else if places.isEmpty {
            ContentUnavailableView {
                Label(isFiltered ? "No places match" : "No places yet", systemImage: "mappin.slash")
            } description: {
                Text(isFiltered ? "Try other filters or another search." : "Places you add show up here.")
            } actions: {
                if isFiltered {
                    Button("Show All") { query = ""; vm.clearFilters() }.buttonStyle(.glass)
                }
            }
        } else {
            VStack(alignment: .leading, spacing: 8) {
                SectionLabel(text: heading.0, trailing: heading.1)
                LazyVStack(spacing: 0) {
                    ForEach(Array(places.enumerated()), id: \.element.id) { index, place in
                        PlaceRow(
                            place: place,
                            last: index == places.count - 1 && !status.isLoadingMore,
                            onShare: { vm.shareLocation(location: place) },
                            onDuplicate: { vm.duplicateLocation(location: place) },
                            onDelete: { deleting = place }
                        )
                        .onAppear { presenter.itemAccessed(index: Int32(index)) }
                    }
                    if status.isLoadingMore {
                        ProgressView().frame(maxWidth: .infinity, minHeight: 56)
                    } else if status.error != nil {
                        Button("Couldn't load more. Try again") { presenter.retry() }
                            .frame(maxWidth: .infinity, minHeight: 56)
                    }
                }
                .mistCard()
            }
        }
    }
}

/// A filter chip: teal when on, frosted white when off.
private struct Chip: View {
    let title: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(selected ? Theme.onAccent : Theme.ink)
                .padding(.horizontal, 14)
                .frame(minHeight: 34)
                .background {
                    if selected { Capsule().fill(Theme.accent) }
                }
        }
        .buttonStyle(.plain)
        .background { if !selected { Color.clear.mistCard(radius: 17) } }
        .accessibilityAddTraits(selected ? .isSelected : [])
    }
}

/// One place in the card: photo, name, "Region, Country · ★ 5.0", its tags, and a menu.
private struct PlaceRow: View {
    let place: Location
    let last: Bool
    let onShare: () -> Void
    let onDuplicate: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            NavigationLink(value: place) {
                HStack(alignment: .top, spacing: 12) {
                    RemoteImage(url: primaryPhoto(place)) {
                        Rectangle().fill(Theme.Tone.teal.fill)
                            .overlay(Text(place.category?.icon ?? "📍").font(.title3))
                    }
                    .frame(width: 48, height: 48)
                    .clipShape(.rect(cornerRadius: Theme.Radius.thumb, style: .continuous))

                    VStack(alignment: .leading, spacing: 3) {
                        Text(place.name).font(.body.weight(.semibold)).foregroundStyle(Theme.ink)
                        Text(subtitle).font(.subheadline).foregroundStyle(Theme.muted)
                        tags.padding(.top, 3)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.bottom, 12)
                }
                .contentShape(.rect)
            }
            .buttonStyle(.plain)

            Menu {
                Button("Share", systemImage: "square.and.arrow.up", action: onShare)
                Button("Duplicate", systemImage: "plus.square.on.square", action: onDuplicate)
                Button("Delete", systemImage: "trash", role: .destructive, action: onDelete)
            } label: {
                Image(systemName: "ellipsis")
                    .font(.body.weight(.semibold))
                    .foregroundStyle(Theme.glyph)
                    .frame(width: 32, height: 32)
            }
            // A glyph in a fixed 32-point target: it can't grow with the text.
            .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
            .accessibilityLabel("More for \(place.name)")
        }
        .padding(.leading, 12)
        .padding(.trailing, 6)
        .padding(.top, 12)
        // Under the text and the menu both, to the card's edge, as on the other lists; under
        // the text alone it stopped short of the menu.
        .overlay(alignment: .bottom) {
            if !last { Rectangle().fill(Theme.separator).frame(height: 0.5).padding(.leading, 72) }
        }
    }

    private var subtitle: String {
        let whereabouts = whereabouts(place)
        guard let rating = place.rating?.doubleValue, rating > 0 else { return whereabouts }
        let stars = "★ " + rating.formatted(.number.precision(.fractionLength(0...1)))
        return whereabouts.isEmpty ? stars : "\(whereabouts) · \(stars)"
    }

    private var tags: some View {
        HStack(spacing: 6) {
            if let category = place.category {
                ToneTag(text: category.displayName, tone: categoryTone(category.name))
            }
            visitTag(place)
        }
    }
}

/// "Vestland, Norway": region and country, or what the place was saved with.
func whereabouts(_ place: Location) -> String {
    let parts = [place.region?.name, place.country?.name].compactMap { $0 }.filter { !$0.isEmpty }
    return parts.isEmpty ? (place.location ?? "") : parts.joined(separator: ", ")
}

/// The canvas colours categories by kind; a category's tone stays the same wherever it shows.
/// Green and coral are left out: they mean Visited and Planned.
func categoryTone(_ name: String) -> Theme.Tone {
    let tones: [Theme.Tone] = [.blue, .mint, .amber]
    let sum = name.unicodeScalars.reduce(0) { $0 + Int($1.value) }
    return tones[sum % tones.count]
}

/// "Visited" in green, or "Planned" in coral with the next visit's date when one is booked.
@ViewBuilder
func visitTag(_ place: Location) -> some View {
    let today = Calendar.current.startOfDay(for: .now)
    let next = place.visits.compactMap { $0.startDate.flatMap(DayRange.day) }.filter { $0 >= today }.min()
    if let next {
        ToneTag(text: "Planned · \(next.formatted(.dateTime.day().month(.abbreviated)))", tone: .coral)
    } else if place.isVisited {
        ToneTag(text: "Visited", tone: .green, systemImage: "checkmark")
    } else {
        ToneTag(text: "Planned", tone: .coral)
    }
}

/// The canvas's inline search field: a frosted rounded box with a magnifier.
struct SearchField: View {
    @Binding var text: String
    let prompt: String
    var onSubmit: () -> Void = {}

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass").font(.subheadline.weight(.semibold)).foregroundStyle(Theme.muted)
            TextField(prompt, text: $text)
                .submitLabel(.search)
                .onSubmit(onSubmit)
                .autocorrectionDisabled()
                .foregroundStyle(Theme.ink)
            if !text.isEmpty {
                Button { text = "" } label: { Image(systemName: "xmark.circle.fill").foregroundStyle(Theme.glyph) }
                    .accessibilityLabel("Clear search")
            }
        }
        .padding(.horizontal, 12)
        .frame(minHeight: 40)
        .mistCard(radius: Theme.Radius.field)
    }
}
