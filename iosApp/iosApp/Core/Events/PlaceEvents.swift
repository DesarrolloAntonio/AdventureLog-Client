import Foundation

extension Notification.Name {
    /// A place was saved somewhere (created, edited, a visit added or removed). The screens that list
    /// places hold pages loaded earlier, and a place edited from its own page left them showing the
    /// old photo and name until a pull to refresh; they reload when they hear this.
    static let placeSaved = Notification.Name("placeSaved")
}
