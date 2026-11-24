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

    enum Page: Hashable { case itinerary, lists, notes }

    @StateObject private var host = ViewModelHost()
    @State private var state = CollectionDetailUiState(collection: nil, isLoading: true, errorMessage: nil)
    @State private var page: Page = .itinerary
    @State private var working = false
    @State private var message: String?

    private var vm: CollectionDetailViewModel { host.owner.collectionDetailViewModel() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if let collection = state.collection {
                    Picker("View", selection: $page) {
                        Text("Itinerary").tag(Page.itinerary)
                        Text("Lists").tag(Page.lists)
                        Text("Notes").tag(Page.notes)
                    }
                    .pickerStyle(.segmented)
                    .padding(.bottom, 16)

                    switch page {
                    case .itinerary: itinerary(collection)
                    case .lists: lists(collection)
                    case .notes: notes(collection)
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
        .navigationTitle(state.collection?.name ?? name)
        .navigationSubtitle(state.collection.map(Self.subtitle) ?? subtitle)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Menu {
                    NavigationLink { ComingSoon(title: "New Place") } label: { Label("Place", systemImage: "mappin") }
                    NavigationLink { ComingSoon(title: "New Transport") } label: { Label("Transport", systemImage: "airplane") }
                    NavigationLink { ComingSoon(title: "New Lodging") } label: { Label("Lodging", systemImage: "bed.double") }
                    NavigationLink { ComingSoon(title: "New Note") } label: { Label("Note", systemImage: "note.text") }
                    NavigationLink { ComingSoon(title: "New Checklist") } label: { Label("Checklist", systemImage: "checklist") }
                } label: {
                    Image(systemName: "plus")
                }
                .accessibilityLabel("Add to trip")
            }
        }
        .navigationDestination(for: Location.self) { PlaceView(placeId: $0.id, name: $0.name, subtitle: whereabouts($0)) }
        .refreshable { vm.loadCollection(collectionId: collectionId) }
        .alert(message ?? "", isPresented: Binding(get: { message != nil }, set: { if !$0 { vm.clearActionMessage() } })) {
            Button("OK", role: .cancel) {}
        }
        .task { vm.loadCollection(collectionId: collectionId) }
        .task { for await s in vm.uiState { state = s } }
        .task { for await w in vm.itineraryWorking { working = w.boolValue } }
        .task { for await m in vm.actionMessage { message = m } }
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

        ForEach(days, id: \.0.dayNumber) { day, items in
            SectionLabel(text: dayLabel(day), trailing: "\(items.count)").padding(.bottom, 8)
            itemsCard(items, in: collection).padding(.bottom, 20)
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
            if !collection.locations.isEmpty {
                SectionLabel(text: "Places", trailing: "\(collection.locations.count)").padding(.bottom, 8)
                VStack(spacing: 0) {
                    ForEach(Array(collection.locations.enumerated()), id: \.element.id) { index, place in
                        NavigationLink(value: place) {
                            TripRow(kind: .location, title: place.name, subtitle: whereabouts(place),
                                    last: index == collection.locations.count - 1, chevron: true)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .mistCard()
            } else if !plan.canAutoGenerate {
                ContentUnavailableView("Nothing here yet", systemImage: "calendar",
                                       description: Text("Add places, stays and journeys to plan this trip."))
            }
        }
    }

    /// "Saturday 5 Sep · Day 1", or the day's own name where it has one.
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
                if let place = place(for: item, in: collection) {
                    NavigationLink(value: place) {
                        TripRow(kind: item.kind, title: item.title, subtitle: item.subtitle ?? whereabouts(place), last: last, chevron: true)
                    }
                    .buttonStyle(.plain)
                } else {
                    TripRow(kind: item.kind, title: item.title, subtitle: item.subtitle, last: last, chevron: false)
                }
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
                        HStack {
                            Text(list.name).font(.headline).foregroundStyle(Theme.ink)
                            Spacer()
                            Text("\(list.items.filter(\.isChecked).count) of \(list.items.count)")
                                .font(.footnote).foregroundStyle(Theme.faint).monospacedDigit()
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
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
private struct TripRow: View {
    let kind: ItineraryItemKind
    let title: String
    let subtitle: String?
    let last: Bool
    let chevron: Bool

    var body: some View {
        HStack(spacing: 12) {
            Circle().fill(dot).frame(width: 9, height: 9).accessibilityHidden(true)
            HStack {
                VStack(alignment: .leading, spacing: 1) {
                    Text(title).foregroundStyle(Theme.ink)
                    Text([kindName, subtitle].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " · "))
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
