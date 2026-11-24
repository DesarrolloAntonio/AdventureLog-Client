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
