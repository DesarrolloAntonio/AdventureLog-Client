import SwiftUI

/// The frame every Mist form shares, as the canvas draws them: a sheet over the mist, the form's
/// title with what it belongs to under it, a round close button and a round teal tick that saves.
/// Closing with unsaved changes asks first; a refusal from the server shows as an alert.
struct FormScaffold<Content: View>: View {
    let title: String
    var subtitle: String = ""
    let canSave: Bool
    let saving: Bool
    let dirty: Bool
    let error: String?
    let clearError: () -> Void
    let save: () -> Void
    /// What a Delete button at the foot of the form removes, as (its label, the action); nil hides it.
    var delete: (label: String, action: () -> Void)? = nil
    @ViewBuilder let content: Content

    @Environment(\.dismiss) private var dismiss
    @State private var confirmingDiscard = false
    @State private var confirmingDelete = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    content
                    if let delete {
                        Button(role: .destructive) { confirmingDelete = true } label: {
                            Text(delete.label).font(.headline).frame(maxWidth: .infinity, minHeight: 52)
                        }
                        .buttonStyle(.plain)
                        .foregroundStyle(Theme.Tone.coral.ink)
                        .mistCard()
                        .disabled(saving)
                        .accessibilityIdentifier("delete")
                        .confirmationDialog("\(delete.label)?", isPresented: $confirmingDelete, titleVisibility: .visible) {
                            Button(delete.label, role: .destructive) { delete.action(); dismiss() }
                            Button("Cancel", role: .cancel) {}
                        } message: {
                            Text("This can't be undone.")
                        }
                    }
                }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
                    .padding(.bottom, 40)
            }
            .scrollDismissesKeyboard(.interactively)
            .mistBackground()
            .navigationTitle(title)
            .navigationSubtitle(subtitle)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button { dirty ? (confirmingDiscard = true) : dismiss() } label: { Image(systemName: "xmark") }
                        .accessibilityLabel("Close")
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(action: save) {
                        if saving { ProgressView().tint(.white) } else { Image(systemName: "checkmark") }
                    }
                    .buttonStyle(.glassProminent)
                    .tint(Theme.accent)
                    .disabled(!canSave || saving)
                    .accessibilityLabel("Save")
                    .accessibilityIdentifier("save")
                }
            }
            .confirmationDialog("Discard your changes?", isPresented: $confirmingDiscard, titleVisibility: .visible) {
                Button("Discard Changes", role: .destructive) { dismiss() }
                Button("Keep Editing", role: .cancel) {}
            }
            .alert(error ?? "", isPresented: Binding(get: { error != nil }, set: { if !$0 { clearError() } })) {
                Button("OK", role: .cancel) {}
            }
        }
        .interactiveDismissDisabled(dirty)
    }
}

/// A frosted card holding form rows, with hairlines between them.
struct FormCard<Content: View>: View {
    @ViewBuilder let content: Content
    var body: some View {
        VStack(alignment: .leading, spacing: 0) { content }.mistCard()
    }
}

/// A hairline between rows of a form card.
struct FormDivider: View {
    var inset: CGFloat = 16
    var body: some View { Rectangle().fill(Theme.separator).frame(height: 0.5).padding(.leading, inset) }
}

/// A text field row. The text lives in the caller's @State and goes to the ViewModel through
/// `.onChange`, never read back from a StateFlow: that dropped characters typed fast.
struct FormField: View {
    let placeholder: String
    @Binding var text: String
    var font: Font = .body
    var axis: Axis = .horizontal
    var keyboard: UIKeyboardType = .default
    var capitalization: TextInputAutocapitalization = .sentences

    var body: some View {
        TextField(placeholder, text: $text, axis: axis)
            .font(font)
            .foregroundStyle(Theme.ink)
            .keyboardType(keyboard)
            .textInputAutocapitalization(capitalization)
            .lineLimit(axis == .vertical ? 3...12 : 1...1)
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .frame(minHeight: 50)
    }
}

/// A row with a title and an optional date: "Add date" until one is set, then a date picker and a
/// way to clear it. The value is the API's "yyyy-MM-dd" (or a full ISO date-time when [withTime]).
struct DateRow: View {
    let title: String
    @Binding var value: String
    var withTime = false

    var body: some View {
        HStack {
            Text(title).foregroundStyle(Theme.ink)
            Spacer(minLength: 8)
            if let date = ISODates.parse(value) {
                DatePicker(title, selection: Binding(get: { date }, set: { value = ISODates.format($0, withTime: withTime) }),
                           displayedComponents: withTime ? [.date, .hourAndMinute] : [.date])
                    .labelsHidden()
                Button { value = "" } label: { Image(systemName: "xmark.circle.fill").foregroundStyle(Theme.glyph) }
                    .buttonStyle(.plain)
                    .accessibilityLabel("Clear \(title)")
            } else {
                Button("Add date") { value = ISODates.format(.now, withTime: withTime) }
                    .font(.subheadline.weight(.semibold))
            }
        }
        .padding(.horizontal, 16)
        .frame(minHeight: 52)
    }
}

/// A switch row.
struct ToggleRow: View {
    let title: String
    @Binding var isOn: Bool
    var body: some View {
        Toggle(title, isOn: $isOn)
            .tint(Theme.accent)
            .foregroundStyle(Theme.ink)
            .padding(.horizontal, 16)
            .frame(minHeight: 52)
    }
}

/// Choices as chips in a row that scrolls: teal when chosen, frosted otherwise.
struct ChoiceChips: View {
    let options: [(value: String, label: String)]
    @Binding var selection: String

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.value) { option in
                    let on = option.value == selection
                    Button { selection = option.value } label: {
                        Text(option.label)
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(on ? Theme.onAccent : Theme.ink)
                            .padding(.horizontal, 14)
                            .frame(minHeight: 34)
                            .background { if on { Capsule().fill(Theme.accent) } }
                    }
                    .buttonStyle(.plain)
                    .background { if !on { Color.clear.mistCard(radius: 17) } }
                    .accessibilityAddTraits(on ? .isSelected : [])
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 4)
        }
        .padding(.horizontal, -16)
    }
}

/// The API's dates: "yyyy-MM-dd" for days, ISO 8601 with time for journeys and stays.
enum ISODates {
    static func parse(_ s: String) -> Date? {
        guard !s.isEmpty else { return nil }
        if s.count <= 10 { return DayRange.day(s) }
        let iso = ISO8601DateFormatter()
        iso.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let d = iso.date(from: s) { return d }
        iso.formatOptions = [.withInternetDateTime]
        if let d = iso.date(from: s) { return d }
        let local = DateFormatter()
        local.locale = Locale(identifier: "en_US_POSIX")
        local.dateFormat = "yyyy-MM-dd'T'HH:mm"
        return local.date(from: String(s.prefix(16))) ?? DayRange.day(s)
    }

    static func format(_ d: Date, withTime: Bool) -> String {
        let f = DateFormatter()
        f.locale = Locale(identifier: "en_US_POSIX")
        f.dateFormat = withTime ? "yyyy-MM-dd'T'HH:mm" : "yyyy-MM-dd"
        return f.string(from: d)
    }
}
