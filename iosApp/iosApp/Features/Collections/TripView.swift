import SwiftUI
import ComposeApp

/// One collection as a trip, as the Mist-Trip artboard draws it: its dates and how far off it is
/// under the title, Itinerary / Lists / Notes, the packing list as the teal card, and the days,
/// each a frosted card of what happens on it with a coloured dot per kind.
struct TripView: View {
    let collectionId: String
    let name: String
    /// Shown under the title from the first frame, so the large title opens out (see PlaceView).
    var subtitle: String = ""

    enum Page: String, Hashable, CaseIterable { case itinerary = "Itinerary", lists = "Lists", notes = "Notes", map = "Map", stats = "Stats", nearby = "Nearby" }

    /// A form over the trip: something new in it, or one of its things to edit.
    enum Form: Identifiable {
        case place, transport(String?), lodging(String?), note(String?), checklist(String?), trip
        var id: String {
            switch self {
            case .place: "place"
            case .transport(let id): "transport-\(id ?? "")"
            case .lodging(let id): "lodging-\(id ?? "")"
            case .note(let id): "note-\(id ?? "")"
            case .checklist(let id): "checklist-\(id ?? "")"
            case .trip: "trip"
            }
        }
    }

    @StateObject private var host = ViewModelHost()
    @State private var state = CollectionDetailUiState(collection: nil, isLoading: true, errorMessage: nil)
    @State private var page: Page = .itinerary
    @State private var working = false
    @State private var message: String?
    @State private var form: Form?
    @State private var userId: String?
    @State private var managing: Location?
    @State private var removing: Location?
    /// A place taken off the trip, until the reloaded trip no longer has it.
    @State private var removedPlaceId: String?
    @State private var collections: [UltraSlimCollection] = []
    @State private var collectionsLoading = false
    @State private var savingCollections = false
    @State private var collectionsError: String?

    private var vm: CollectionDetailViewModel { host.owner.collectionDetailViewModel() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if let collection = state.collection {
                    // Six views, as on Android and the web: more than a segmented control holds.
                    ChoiceChips(
                        options: Page.allCases.map { ($0.rawValue, $0.rawValue) },
                        selection: Binding(get: { page.rawValue }, set: { page = Page(rawValue: $0) ?? .itinerary })
                    )
                    .padding(.bottom, 16)

                    switch page {
                    case .itinerary: itinerary(collection)
                    case .lists: lists(collection)
                    case .notes: notes(collection)
                    case .map: TripMap(collection: collection)
                    case .stats: TripStats(collection: collection)
                    case .nearby: TripNearby(collection: collection) { vm.loadCollection(collectionId: collectionId) }
                    }
                } else if let error = state.errorMessage {
                    ContentUnavailableView {
                        Label("Couldn't load \(name)", systemImage: "wifi.exclamationmark")
                    } description: {
                        Text(error)
                    } actions: {
                        Button("Try again") { vm.loadCollection(collectionId: collectionId) }.buttonStyle(.glassProminent)
                    }
                    .padding(.top, 60)
                } else {
                    ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 120)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 120)
        }
        .mistBackground()
        .navigationBarTitleDisplayMode(.large)
        .opensLargeTitle()
        .navigationTitle(state.collection?.name ?? name)
        .navigationSubtitle(state.collection.map(Self.subtitle) ?? subtitle)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Menu {
                    Button { form = .place } label: { Label("Place", systemImage: "mappin") }
                    Button { form = .transport(nil) } label: { Label("Transport", systemImage: "airplane") }
                    Button { form = .lodging(nil) } label: { Label("Lodging", systemImage: "bed.double") }
                    Button { form = .note(nil) } label: { Label("Note", systemImage: "note.text") }
                    Button { form = .checklist(nil) } label: { Label("Checklist", systemImage: "checklist") }
                } label: {
                    Image(systemName: "plus")
                }
                .accessibilityLabel("Add to trip")
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button { form = .trip } label: { Image(systemName: "pencil") }
                    .accessibilityLabel("Edit trip")
                    .disabled(state.collection == nil)
            }
        }
        .sheet(item: $form) { form in
            let tripName = state.collection?.name ?? name
            let reload = { vm.loadCollection(collectionId: collectionId) }
            switch form {
            case .place: PlaceFormView(intoCollectionId: collectionId, onSaved: reload)
            case .transport(let id):
                TransportFormView(collectionId: collectionId, collectionName: tripName, transportationId: id, onSaved: reload,
                                  onDelete: id.map { id in { vm.deleteTransportation(transportationId: id) } })
            case .lodging(let id):
                LodgingFormView(collectionId: collectionId, collectionName: tripName, lodgingId: id, onSaved: reload,
                                onDelete: id.map { id in { vm.deleteLodging(lodgingId: id) } })
            case .note(let id):
                NoteFormView(collectionId: collectionId, collectionName: tripName, noteId: id, onSaved: reload,
                             onDelete: id.map { id in { vm.deleteNote(noteId: id) } })
            case .checklist(let id):
                ChecklistFormView(collectionId: collectionId, collectionName: tripName, checklistId: id, onSaved: reload,
                                  onDelete: id.map { id in { vm.deleteChecklist(checklistId: id) } })
            case .trip: CollectionFormView(collectionId: collectionId, onSaved: reload)
            }
        }
        .sheet(item: $managing) { place in
            CollectionsPickerView(
                place: place,
                collections: collections,
                loading: collectionsLoading,
                saving: savingCollections,
                error: collectionsError,
                clearError: { collectionsError = nil; vm.clearUpdateCollectionsState() },
                refresh: { vm.refreshCollections() },
                save: { vm.updateAdventureCollections(adventureId: place.id, collectionIds: $0) }
            )
        }
        .confirmationDialog(
            "Remove \(removing?.name ?? "") from this trip?",
            isPresented: Binding(get: { removing != nil }, set: { if !$0 { removing = nil } }),
            titleVisibility: .visible,
            presenting: removing
        ) { place in
            Button("Remove from Trip", role: .destructive) {
                removedPlaceId = place.id
                vm.removeFromCollection(location: place, collectionId: collectionId)
            }
        } message: { _ in
            Text("The place stays in Places and in its other collections.")
        }
        .navigationDestination(for: Location.self) { PlaceView(placeId: $0.id, name: $0.name, subtitle: whereabouts($0)) }
        .refreshable { vm.loadCollection(collectionId: collectionId) }
        .alert(message ?? "", isPresented: Binding(get: { message != nil }, set: { if !$0 { vm.clearActionMessage() } })) {
            Button("OK", role: .cancel) {}
        }
        .task { vm.loadCollection(collectionId: collectionId) }
        .task {
            for await s in vm.uiState {
                state = s
                // Lists elsewhere reload once the server has really taken the place off: told
                // sooner, Places kept the trip in the place's collections and could put it back.
                if let id = removedPlaceId, let trip = s.collection, !trip.locations.contains(where: { $0.id == id }) {
                    removedPlaceId = nil
                    NotificationCenter.default.post(name: .placeSaved, object: nil)
                }
            }
        }
        .task { for await w in vm.itineraryWorking { working = w.boolValue } }
        .task { for await m in vm.actionMessage { message = m } }
        .task { for await user in host.owner.sessionFlow() { userId = user?.uuid } }
        .task { for await c in vm.allCollections { collections = c } }
        .task { for await l in vm.collectionsLoading { collectionsLoading = l.boolValue } }
        .task {
            for await s in vm.updateCollectionsState {
                savingCollections = false
                switch onEnum(of: s) {
                case .loading: savingCollections = true
                case .success:
                    managing = nil
                    vm.clearUpdateCollectionsState()
                    NotificationCenter.default.post(name: .placeSaved, object: nil)
                case .error(let e): collectionsError = e.message
                case .idle: break
                }
            }
        }
        // A delete, or a change to the plan, the server refused.
        .task {
            for await d in vm.deleteState {
                if case .error(let e) = onEnum(of: d) { message = e.message; vm.clearDeleteState() }
            }
        }
    }

    /// "5–9 Sep · 4 nights · in 8 days"
    private static func subtitle(_ c: ComposeApp.Collection) -> String {
        guard let range = DayRange(start: c.startDate, end: c.endDate) else {
            return c.locations.count == 1 ? "1 place" : "\(c.locations.count) places"
        }
        var parts = [range.shortText]
        if range.nights > 0 { parts.append(range.nights == 1 ? "1 night" : "\(range.nights) nights") }
        if case let .range(start, end) = range {
            let today = Calendar.current.startOfDay(for: .now)
            let days = Calendar.current.dateComponents([.day], from: today, to: start).day ?? 0
            if days > 1 { parts.append("in \(days) days") }
            else if days == 1 { parts.append("tomorrow") }
            else if end >= today { parts.append("on now") }
        }
        return parts.joined(separator: " · ")
    }

    // MARK: Itinerary

    @ViewBuilder
    private func itinerary(_ collection: ComposeApp.Collection) -> some View {
        let hero = collection.checklists.first
        if let hero {
            Button { page = .lists } label: { ChecklistCard(list: hero) }
                .buttonStyle(.plain)
                .accessibilityHint("Opens the lists")
                .padding(.bottom, 20)
        }

        let plan = IosTrip.shared.plan(collection: collection)
        // The list on the teal card isn't drawn a second time as a row.
        let shown: (ItineraryItem) -> Bool = { item in
            item.kind != .checklist || collection.itinerary.first { $0.id == item.entryId }?.itemId != hero?.id
        }
        let context = plan.tripContext.filter(shown)
        let days = plan.days.map { ($0, $0.items.filter(shown)) }.filter { !$0.1.isEmpty }

        if !context.isEmpty {
            SectionLabel(text: "For the whole trip", trailing: "\(context.count)").padding(.bottom, 8)
            itemsCard(context, in: collection).padding(.bottom, 20)
        }

        // Server 0.14's Ideas: on the trip, on no day yet.
        let ideas = plan.ideas.filter(shown)
        if !ideas.isEmpty {
            SectionLabel(text: "Ideas", trailing: "\(ideas.count)").padding(.bottom, 8)
            itemsCard(ideas, in: collection).padding(.bottom, 20)
        }

        ForEach(days, id: \.0.dayNumber) { day, items in
            SectionLabel(text: dayLabel(day), trailing: "\(items.count)").padding(.bottom, 8)
            itemsCard(items, in: collection).padding(.bottom, 20)
        }

        // Places, journeys and stays added after the plan was laid out have no day yet; without
        // this they were nowhere on the trip.
        let planned = Set(collection.itinerary.map(\.itemId))
        // Shown before there is a plan too: the server may refuse to build one, and these can be
        // put on it by hand.
        let unplanned: [Unplanned] =
            collection.locations.filter { place in
                !planned.contains(place.id) && !place.visits.contains { planned.contains($0.id) }
            }.map(Unplanned.place)
            + collection.transportations.filter { !planned.contains($0.id) }.map(Unplanned.transport)
            + collection.lodging.filter { !planned.contains($0.id) }.map(Unplanned.stay)
        if !unplanned.isEmpty {
            SectionLabel(text: "Not in the plan", trailing: "\(unplanned.count)").padding(.bottom, 8)
            VStack(spacing: 0) {
                ForEach(Array(unplanned.enumerated()), id: \.element.id) { index, item in
                    let last = index == unplanned.count - 1
                    Group {
                    switch item {
                    case .place(let place):
                        NavigationLink(value: place) {
                            TripRow(kind: .location, title: place.name, subtitle: whereabouts(place), showsKind: false,
                                    last: last, chevron: true)
                        }
                        .buttonStyle(.plain)
                    case .transport(let journey):
                        Button { form = .transport(journey.id) } label: {
                            TripRow(kind: .transportation, title: journey.name, subtitle: route(journey), last: last, chevron: true)
                        }
                        .buttonStyle(.plain)
                    case .stay(let stay):
                        Button { form = .lodging(stay.id) } label: {
                            TripRow(kind: .lodging, title: stay.name,
                                    subtitle: DayRange(start: stay.checkIn, end: stay.checkOut)?.shortText, last: last, chevron: true)
                        }
                        .buttonStyle(.plain)
                    }
                    }
                    .contextMenu {
                        addToPlan(item, days: plan.days)
                        if case .place(let place) = item { placeActions(place) }
                    }
                }
            }
            .mistCard()
            .padding(.bottom, 20)
        }

        if plan.isEmpty {
            if plan.canAutoGenerate {
                VStack(alignment: .leading, spacing: 10) {
                    Text("No itinerary yet").font(.headline).foregroundStyle(Theme.ink)
                    Text("Lay the trip out day by day from the dates its places, stays and journeys already have.")
                        .font(.subheadline).foregroundStyle(Theme.muted)
                    Button {
                        vm.autoGenerateItinerary()
                    } label: {
                        ZStack {
                            Text("Build itinerary").opacity(working ? 0 : 1)
                            if working { ProgressView().tint(.white) }
                        }
                        .font(.headline)
                        .frame(maxWidth: .infinity, minHeight: 48)
                        .background { HeroGlow().clipShape(.rect(cornerRadius: 16)) }
                        .heroCard(radius: 16)
                    }
                    .buttonStyle(.plain)
                    .disabled(working)
                }
                .padding(16)
                .mistCard()
                .padding(.bottom, 20)
            }
            // Places, stays and journeys are listed above, under Not in the plan.
            if unplanned.isEmpty && !plan.canAutoGenerate {
                ContentUnavailableView("Nothing here yet", systemImage: "calendar",
                                       description: Text("Add places, stays and journeys to plan this trip."))
            }
        }
    }

    /// "Saturday 5 Sep · Day 1", or the day's own name where it has one.
    /// "Madrid → Lisbon", or the kind of journey when it has no places.
    private func route(_ journey: Transportation) -> String {
        let ends = [journey.fromLocation, journey.toLocation].compactMap { $0 }.filter { !$0.isEmpty }
        return ends.isEmpty ? journey.type.capitalized : ends.joined(separator: " → ")
    }

    /// Where a place is filed, as Android's place actions: only for its owner, like there.
    @ViewBuilder
    private func placeActions(_ place: Location) -> some View {
        if OwnershipKt.ownedBy(ownerId: place.user.uuid, currentUserId: userId) {
            Section {
                Button("Collections…", systemImage: "rectangle.stack") { managing = place }
                Button("Remove from Trip", systemImage: "folder.badge.minus", role: .destructive) { removing = place }
            }
        }
    }

    /// Puts something that has no day on the plan: for the whole trip, or on one of its days.
    @ViewBuilder
    private func addToPlan(_ item: Unplanned, days: [ItineraryDay]) -> some View {
        Menu("Add to the Plan", systemImage: "calendar.badge.plus") {
            Button("For the Whole Trip") { vm.addToItinerary(kind: item.kind, itemId: item.itemId, date: nil) }
            ForEach(days, id: \.dayNumber) { day in
                Button(dayLabel(day)) { vm.addToItinerary(kind: item.kind, itemId: item.itemId, date: String(describing: day.date)) }
            }
        }
    }

    private func dayLabel(_ day: ItineraryDay) -> String {
        var c = DateComponents()
        c.year = Int(day.date.year); c.month = Int(day.date.month.ordinal + 1); c.day = Int(day.date.day)
        let date = Calendar.current.date(from: c).map { $0.formatted(.dateTime.weekday(.wide).day().month(.abbreviated)) } ?? ""
        return day.name.isEmpty ? "\(date) · Day \(day.dayNumber)" : "\(date) · \(day.name)"
    }

    private func itemsCard(_ items: [ItineraryItem], in collection: ComposeApp.Collection) -> some View {
        VStack(spacing: 0) {
            ForEach(Array(items.enumerated()), id: \.element.entryId) { index, item in
                let last = index == items.count - 1
                Group {
                    if let place = place(for: item, in: collection) {
                        NavigationLink(value: place) {
                            TripRow(kind: item.kind, title: item.title, subtitle: item.subtitle ?? whereabouts(place), last: last, chevron: true)
                        }
                        .buttonStyle(.plain)
                    } else if let edit = edit(for: item, in: collection) {
                        Button { form = edit } label: {
                            TripRow(kind: item.kind, title: item.title, subtitle: item.subtitle, last: last, chevron: true)
                        }
                        .buttonStyle(.plain)
                    } else {
                        TripRow(kind: item.kind, title: item.title, subtitle: item.subtitle, last: last, chevron: false)
                    }
                }
                // Off the plan only: the place, journey or note itself stays on the trip.
                .contextMenu {
                    Button("Remove from the Plan", systemImage: "calendar.badge.minus") {
                        vm.removeFromItinerary(entryId: item.entryId)
                    }
                    if let place = place(for: item, in: collection) { placeActions(place) }
                }
                .accessibilityAction(named: "Remove from the plan") { vm.removeFromItinerary(entryId: item.entryId) }
            }
        }
        .mistCard()
    }

    /// The place an itinerary entry points at, for the kinds that are places.
    private func place(for item: ItineraryItem, in collection: ComposeApp.Collection) -> Location? {
        guard item.kind == .location || item.kind == .visit,
              let entry = collection.itinerary.first(where: { $0.id == item.entryId }) else { return nil }
        return collection.locations.first { $0.id == entry.itemId || $0.visits.contains { $0.id == entry.itemId } }
    }

    /// The form that edits what an itinerary entry points at, for the kinds that have one.
    private func edit(for item: ItineraryItem, in collection: ComposeApp.Collection) -> Form? {
        guard let id = collection.itinerary.first(where: { $0.id == item.entryId })?.itemId else { return nil }
        switch item.kind {
        case .transportation: return .transport(id)
        case .lodging: return .lodging(id)
        case .note: return .note(id)
        case .checklist: return .checklist(id)
        default: return nil
        }
    }

    // MARK: Lists

    @ViewBuilder
    private func lists(_ collection: ComposeApp.Collection) -> some View {
        if collection.checklists.isEmpty {
            ContentUnavailableView("No lists yet", systemImage: "checklist",
                                   description: Text("Packing lists and to-dos for this trip show up here."))
        } else {
            VStack(spacing: 14) {
                ForEach(collection.checklists, id: \.id) { list in
                    VStack(alignment: .leading, spacing: 0) {
                        Button { form = .checklist(list.id) } label: {
                            HStack {
                                Text(list.name).font(.headline).foregroundStyle(Theme.ink)
                                Spacer()
                                Text("\(list.items.filter(\.isChecked).count) of \(list.items.count)")
                                    .font(.footnote).foregroundStyle(Theme.faint).monospacedDigit()
                                Image(systemName: "pencil").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                            .contentShape(.rect)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Edit \(list.name)")
                        ForEach(list.items, id: \.id) { item in
                            HStack(spacing: 12) {
                                Image(systemName: item.isChecked ? "checkmark.circle.fill" : "circle")
                                    .foregroundStyle(item.isChecked ? Theme.accent : Theme.glyph)
                                    .font(.title3)
                                Text(item.name)
                                    .foregroundStyle(item.isChecked ? Theme.muted : Theme.ink)
                                    .strikethrough(item.isChecked, color: Theme.muted)
                                Spacer()
                            }
                            .padding(.horizontal, 16)
                            .frame(minHeight: 44)
                            .accessibilityElement(children: .combine)
                            .accessibilityValue(item.isChecked ? "Done" : "Not done")
                        }
                    }
                    .padding(.bottom, 6)
                    .mistCard()
                }
            }
        }
    }

    // MARK: Notes

    @ViewBuilder
    private func notes(_ collection: ComposeApp.Collection) -> some View {
        if collection.notes.isEmpty {
            ContentUnavailableView("No notes yet", systemImage: "note.text",
                                   description: Text("Notes you keep for this trip show up here."))
        } else {
            VStack(spacing: 14) {
                ForEach(collection.notes, id: \.id) { note in
                    Button { form = .note(note.id) } label: {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack(alignment: .firstTextBaseline) {
                            Text(note.name).font(.headline).foregroundStyle(Theme.ink)
                            Spacer()
                            if let date = note.date.flatMap(DayRange.day) {
                                Text(date.formatted(.dateTime.day().month(.abbreviated)))
                                    .font(.footnote).foregroundStyle(Theme.faint)
                            }
                        }
                        if let content = note.content, !content.isEmpty {
                            Text(content).font(.body).foregroundStyle(Theme.muted)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .mistCard()
                    .contentShape(.rect)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }
}

/// The packing list in teal: how much is packed and how much is left.
private struct ChecklistCard: View {
    let list: Checklist

    var body: some View {
        let done = list.items.filter(\.isChecked).count
        let left = list.items.count - done
        HStack(spacing: 12) {
            Image(systemName: "checklist")
                .font(.system(size: 18, weight: .semibold))
                .frame(width: 38, height: 38)
                .background(.white.opacity(0.2), in: .circle)
            VStack(alignment: .leading, spacing: 1) {
                Text(list.name).font(.body.weight(.bold))
                Text(list.items.isEmpty ? "Nothing on it yet" : "\(done) of \(list.items.count) done · \(left) to go")
                    .font(.footnote)
                    .foregroundStyle(.white.opacity(0.9))
            }
            Spacer()
        }
        .padding(.horizontal, 14)
        .frame(minHeight: 62)
        .background { HeroGlow().clipShape(.rect(cornerRadius: Theme.Radius.card)) }
        .heroCard(radius: Theme.Radius.card)
        .accessibilityElement(children: .combine)
    }
}

/// A row of a day: a dot coloured by kind, its title and detail.
/// Something on the trip that the itinerary has no day for.
private enum Unplanned: Identifiable {
    case place(Location), transport(Transportation), stay(Lodging)

    var id: String {
        switch self {
        case .place(let p): "place-\(p.id)"
        case .transport(let t): "transport-\(t.id)"
        case .stay(let l): "stay-\(l.id)"
        }
    }
    var itemId: String {
        switch self {
        case .place(let p): p.id
        case .transport(let t): t.id
        case .stay(let l): l.id
        }
    }

    var kind: ItineraryItemKind {
        switch self {
        case .place: .location
        case .transport: .transportation
        case .stay: .lodging
        }
    }
}

private struct TripRow: View {
    let kind: ItineraryItemKind
    let title: String
    let subtitle: String?
    var showsKind = true
    let last: Bool
    let chevron: Bool

    var body: some View {
        HStack(spacing: 12) {
            Circle().fill(dot).frame(width: 9, height: 9).accessibilityHidden(true)
            HStack {
                VStack(alignment: .leading, spacing: 1) {
                    Text(title).foregroundStyle(Theme.ink)
                    Text([showsKind ? kindName : nil, subtitle].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " · "))
                        .font(.footnote).foregroundStyle(Theme.muted).lineLimit(2)
                }
                Spacer(minLength: 8)
                if chevron {
                    Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
                }
            }
            .padding(.vertical, 10)
            .frame(minHeight: 58)
            .overlay(alignment: .bottom) {
                if !last { Rectangle().fill(Theme.separator).frame(height: 0.5) }
            }
        }
        .padding(.horizontal, 16)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }

    private var dot: Color {
        switch kind {
        case .transportation: Theme.Tone.blue.ink
        case .lodging: Theme.Tone.coral.ink
        case .note: Theme.Tone.amber.ink
        case .checklist: Theme.Tone.mint.ink
        default: Theme.accent
        }
    }

    private var kindName: String {
        switch kind {
        case .transportation: "Transport"
        case .lodging: "Lodging"
        case .note: "Note"
        case .checklist: "List"
        default: "Place"
        }
    }
}
