import SwiftUI
import ComposeApp

/// The calendar, as the Mist-Calendar artboard draws it: how many trips are coming up under the
/// title, a month in a frosted card with the trips' days in teal and a dot on days with a visit,
/// then the trips coming up. Tapping a day lists what happened on it.
struct CalendarView: View {
    @StateObject private var host = ViewModelHost()
    @State private var state: CalendarUiState?
    @State private var month = Calendar.mondayFirst.dateInterval(of: .month, for: .now)!.start
    @State private var selected: Date?

    private var vm: CalendarViewModel { host.owner.calendarViewModel() }
    private let cal = Calendar.mondayFirst

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if let state, state.error == nil || !state.days.isEmpty {
                    monthCard
                        .padding(.bottom, 20)
                    if let selected {
                        dayEvents(selected)
                    } else {
                        upcoming
                        monthEvents
                    }
                } else if let error = state?.error {
                    ContentUnavailableView {
                        Label("Couldn't load the calendar", systemImage: "wifi.exclamationmark")
                    } description: {
                        Text(error)
                    } actions: {
                        Button("Try again") { vm.load() }.buttonStyle(.glassProminent)
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
        .navigationTitle("Calendar")
        .navigationSubtitle(subtitle)
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Today") { withAnimation { month = cal.dateInterval(of: .month, for: .now)!.start; selected = nil } }
            }
        }
        .navigationDestination(for: CalendarEvent.self) { event in
            if event.type == "visit" {
                PlaceView(placeId: event.resourceId, name: event.title)
            } else {
                TripView(collectionId: event.collectionId ?? event.resourceId, name: event.title,
                         subtitle: DayRange(start: Self.day(event.start), end: Self.day(event.end))?.shortText ?? "")
            }
        }
        .refreshable { vm.load() }
        .task { for await s in vm.uiState { state = s } }
    }

    // MARK: Data

    private var events: [CalendarEvent] { state?.days.flatMap(\.events) ?? [] }

    private var trips: [(event: CalendarEvent, start: Date, end: Date)] {
        events.filter { $0.type == "collection" }.compactMap { e in
            guard let start = Self.date(e.start) else { return nil }
            return (e, start, Self.date(e.end) ?? start)
        }
    }

    private var comingUp: [(event: CalendarEvent, start: Date, end: Date)] {
        let today = cal.startOfDay(for: .now)
        return trips.filter { $0.end >= today }.sorted { $0.start < $1.start }
    }

    private var subtitle: String {
        guard state?.isLoading == false else { return "Your trips and visits" }
        let n = comingUp.count
        return n == 0 ? "Nothing coming up" : n == 1 ? "1 trip coming up" : "\(n) trips coming up"
    }

    private static func day(_ s: String) -> String? { s.isEmpty ? nil : String(s.prefix(10)) }
    private static func date(_ s: String) -> Date? { day(s).flatMap(DayRange.day) }

    private func events(on day: Date) -> [CalendarEvent] {
        events.filter { e in
            guard let start = Self.date(e.start) else { return false }
            let end = Self.date(e.end) ?? start
            return e.type == "collection" ? (start...max(start, end)).contains(day) : cal.isDate(start, inSameDayAs: day)
        }
    }

    // MARK: Month

    private var monthCard: some View {
        let days = gridDays
        let today = cal.startOfDay(for: .now)
        return VStack(spacing: 10) {
            HStack {
                Text(month.formatted(.dateTime.month(.wide).year()))
                    .font(.title3.weight(.bold))
                    .foregroundStyle(Theme.ink)
                Spacer()
                Button { shift(-1) } label: { Image(systemName: "chevron.left") }
                    .accessibilityLabel("Previous month")
                    .padding(.trailing, 14)
                Button { shift(1) } label: { Image(systemName: "chevron.right") }
                    .accessibilityLabel("Next month")
            }
            .font(.body.weight(.semibold))
            .tint(Theme.accent)
            .padding(.horizontal, 4)

            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 0), count: 7), spacing: 6) {
                ForEach(Array(cal.veryShortStandaloneWeekdaySymbolsMondayFirst.enumerated()), id: \.offset) { _, symbol in
                    Text(symbol).font(.caption.weight(.semibold)).foregroundStyle(Theme.faint)
                }
                ForEach(days, id: \.self) { day in
                    cell(day, today: today)
                }
            }
        }
        .padding(14)
        .mistCard()
        .gesture(DragGesture(minimumDistance: 30).onEnded { value in
            if abs(value.translation.width) > abs(value.translation.height) { shift(value.translation.width < 0 ? 1 : -1) }
        })
    }

    private var gridDays: [Date] {
        let lead = (cal.component(.weekday, from: month) - cal.firstWeekday + 7) % 7
        let count = cal.range(of: .day, in: .month, for: month)!.count
        let cells = Int(ceil(Double(lead + count) / 7)) * 7
        return (0..<cells).map { cal.date(byAdding: .day, value: $0 - lead, to: month)! }
    }

    private func cell(_ day: Date, today: Date) -> some View {
        let inMonth = cal.isDate(day, equalTo: month, toGranularity: .month)
        let onTrip = trips.contains { ($0.start...max($0.start, $0.end)).contains(day) }
        let visits = events.filter { $0.type == "visit" && Self.date($0.start).map { cal.isDate($0, inSameDayAs: day) } == true }.count
        let isToday = cal.isDate(day, inSameDayAs: today)
        let isSelected = selected.map { cal.isDate($0, inSameDayAs: day) } ?? false
        return Button {
            withAnimation(.snappy) { selected = isSelected ? nil : day }
        } label: {
            VStack(spacing: 2) {
                Text("\(cal.component(.day, from: day))")
                    .font(.callout.weight(isToday || isSelected ? .bold : .regular))
                    .monospacedDigit()
                    .foregroundStyle(isSelected ? .white : onTrip ? Theme.Tone.teal.ink : inMonth ? Theme.ink : Theme.faint.opacity(0.6))
                    .frame(width: 34, height: 34)
                    .background {
                        if isSelected { Circle().fill(Theme.accent) }
                        else if isToday { Circle().stroke(Theme.accent, lineWidth: 1.5) }
                    }
                Circle().fill(visits > 0 ? Theme.Tone.coral.ink : .clear).frame(width: 5, height: 5)
            }
            .frame(maxWidth: .infinity, minHeight: 44)
            .background {
                if onTrip && inMonth { RoundedRectangle(cornerRadius: 10).fill(Theme.Tone.teal.fill) }
            }
        }
        .buttonStyle(.plain)
        .accessibilityLabel(day.formatted(.dateTime.weekday(.wide).day().month(.wide)))
        .accessibilityValue([onTrip ? "On a trip" : nil, visits > 0 ? "\(visits) visit\(visits == 1 ? "" : "s")" : nil]
            .compactMap { $0 }.joined(separator: ", "))
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }

    private func shift(_ months: Int) {
        withAnimation(.snappy) {
            month = cal.date(byAdding: .month, value: months, to: month)!
            selected = nil
        }
    }

    // MARK: Lists

    @ViewBuilder
    private var upcoming: some View {
        let today = cal.startOfDay(for: .now)
        if !comingUp.isEmpty {
            SectionLabel(text: "Upcoming", trailing: "\(comingUp.count)").padding(.bottom, 8)
            VStack(spacing: 0) {
                ForEach(Array(comingUp.enumerated()), id: \.element.event.id) { index, trip in
                    let days = cal.dateComponents([.day], from: today, to: trip.start).day ?? 0
                    let when = days > 1 ? "in \(days) days" : days == 1 ? "tomorrow" : "on now"
                    NavigationLink(value: trip.event) {
                        EventRow(icon: "rectangle.stack", tone: .teal, title: trip.event.title,
                                 detail: DayRange(start: Self.day(trip.event.start), end: Self.day(trip.event.end))?.shortText ?? "",
                                 trailing: when, last: index == comingUp.count - 1)
                    }
                    .buttonStyle(.plain)
                }
            }
            .mistCard()
            .padding(.bottom, 20)
        }
    }

    @ViewBuilder
    private var monthEvents: some View {
        let inMonth = events.filter { e in Self.date(e.start).map { cal.isDate($0, equalTo: month, toGranularity: .month) } == true }
        SectionLabel(text: month.formatted(.dateTime.month(.wide)), trailing: inMonth.isEmpty ? nil : "\(inMonth.count)")
            .padding(.bottom, 8)
        if inMonth.isEmpty, let earlier = earlierOffer {
            earlier
        } else if inMonth.isEmpty {
            Text("Nothing this month.")
                .font(.subheadline).foregroundStyle(Theme.muted)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .mistCard()
        } else {
            eventList(inMonth, showDate: true)
        }
    }

    /// Before the loaded window (a year back) the month looks empty when it may not be: offer the
    /// rest of the journal, once.
    private var earlierOffer: AnyView? {
        guard let state, let start = state.windowStart,
              let windowStart = DayRange.day(start.description()), month < windowStart else { return nil }
        let label: String
        switch onEnum(of: state.earlier) {
        case .notAsked: label = "Show older trips and visits"
        case .loading: return AnyView(ProgressView().frame(maxWidth: .infinity, minHeight: 60).mistCard())
        case .failed(let f): label = "Couldn't load older ones: \(f.message). Try again"
        case .loaded: return nil
        }
        return AnyView(
            Button { vm.loadEarlier() } label: {
                Label(label, systemImage: "clock.arrow.circlepath")
                    .font(.subheadline.weight(.semibold))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .mistCard()
            }
            .buttonStyle(.plain)
            .foregroundStyle(Theme.accent)
        )
    }

    @ViewBuilder
    private func dayEvents(_ day: Date) -> some View {
        let list = events(on: day)
        SectionLabel(text: cal.isDate(day, equalTo: .now, toGranularity: .year)
                        ? day.formatted(.dateTime.weekday(.wide).day().month(.wide))
                        : day.formatted(.dateTime.weekday(.wide).day().month(.wide).year()), trailing: list.isEmpty ? nil : "\(list.count)")
            .padding(.bottom, 8)
        if list.isEmpty {
            Text("Nothing on this day.")
                .font(.subheadline).foregroundStyle(Theme.muted)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .mistCard()
        } else {
            eventList(list, showDate: false)
        }
    }

    private func eventList(_ list: [CalendarEvent], showDate: Bool) -> some View {
        VStack(spacing: 0) {
            ForEach(Array(list.enumerated()), id: \.element.id) { index, event in
                let visit = event.type == "visit"
                let date = Self.date(event.start)
                let detail = visit
                    ? [event.locationLabel, event.collectionName ?? ""].filter { !$0.isEmpty }.joined(separator: " · ")
                    : DayRange(start: Self.day(event.start), end: Self.day(event.end))?.shortText ?? ""
                NavigationLink(value: event) {
                    EventRow(icon: visit ? "mappin" : "rectangle.stack", tone: visit ? .coral : .teal,
                             title: event.title, detail: detail.isEmpty ? (visit ? "Visit" : "Trip") : detail,
                             trailing: showDate ? date?.formatted(.dateTime.day().month(.abbreviated)) : nil,
                             last: index == list.count - 1)
                }
                .buttonStyle(.plain)
            }
        }
        .mistCard()
    }
}

/// A row of the calendar's lists: a tinted badge, what it is, its detail and when.
private struct EventRow: View {
    let icon: String
    let tone: Theme.Tone
    let title: String
    let detail: String
    let trailing: String?
    let last: Bool

    var body: some View {
        HStack(spacing: 12) {
            ToneBadge(systemImage: icon, tone: tone, size: 34)
            VStack(alignment: .leading, spacing: 1) {
                Text(title).foregroundStyle(Theme.ink).lineLimit(2)
                Text(detail).font(.footnote).foregroundStyle(Theme.muted).lineLimit(2)
            }
            Spacer(minLength: 8)
            if let trailing {
                Text(trailing).font(.footnote.weight(.semibold)).foregroundStyle(Theme.faint)
            }
            Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .frame(minHeight: 58)
        .overlay(alignment: .bottom) {
            if !last { Rectangle().fill(Theme.separator).frame(height: 0.5).padding(.leading, 60) }
        }
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

extension Calendar {
    /// The calendar the screen draws: weeks start on Monday, as in the design.
    static var mondayFirst: Calendar {
        var cal = Calendar.current
        cal.firstWeekday = 2
        return cal
    }

    /// "M T W T F S S", whatever the locale's first weekday.
    var veryShortStandaloneWeekdaySymbolsMondayFirst: [String] {
        let symbols = veryShortStandaloneWeekdaySymbols
        return Array(symbols[1...] + symbols[..<1])
    }
}
