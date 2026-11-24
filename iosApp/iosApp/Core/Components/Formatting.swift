import Foundation
import ComposeApp

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

    /// The canvas's short form: "5–9 Sep", "28 Aug – 3 Sep", and the year only when it isn't
    /// this one ("3–11 Jul 2026" read in 2027).
    var shortText: String {
        guard case let .range(a, b) = self else { return "" }
        let cal = Calendar.current
        let thisYear = cal.component(.year, from: .now) == cal.component(.year, from: b)
        let day = Date.FormatStyle().day()
        let dayMonth = thisYear ? Date.FormatStyle().day().month(.abbreviated) : Date.FormatStyle().day().month(.abbreviated).year()
        if cal.isDate(a, inSameDayAs: b) { return b.formatted(dayMonth) }
        if cal.component(.month, from: a) == cal.component(.month, from: b), cal.component(.year, from: a) == cal.component(.year, from: b) {
            return "\(a.formatted(day))–\(b.formatted(dayMonth))"
        }
        return "\(a.formatted(Date.FormatStyle().day().month(.abbreviated))) – \(b.formatted(dayMonth))"
    }

    /// Nights between the first and the last day.
    var nights: Int {
        guard case let .range(a, b) = self else { return 0 }
        return max(Calendar.current.dateComponents([.day], from: a, to: b).day ?? 0, 0)
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
