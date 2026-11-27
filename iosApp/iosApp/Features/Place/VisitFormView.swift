import SwiftUI
import ComposeApp

/// A visit to a place: the day it began, the day it ended if it was longer, all day or between two
/// times in a time zone (as Android has it), and a note. A new one
/// is added, or an existing one ([visitId]) changed or deleted; either way it is saved with the
/// place, as the web does, so the place's other visits stay as they were.
struct VisitFormView: View {
    let placeId: String
    let placeName: String
    /// The visit being changed; nil adds a new one.
    var visitId: String? = nil
    let onSaved: () -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state: AddEditAdventureUiState?
    @State private var base: LocationFormData?
    @State private var start = ISODates.format(.now, withTime: false)
    @State private var end = ""
    @State private var notes = ""
    /// All day unless the visit had hours; the times are "HH:mm" in [zone].
    @State private var allDay = true
    @State private var startTime = "10:00"
    @State private var endTime = "12:00"
    @State private var zone = TimeZone.current.identifier
    @State private var initial: [String] = []
    @State private var confirmingDelete = false
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditAdventureViewModel { host.owner.addEditAdventureViewModel(adventureId: placeId) }
    /// The end on an earlier day, or on the same day at an earlier hour.
    private var endsBeforeStart: Bool {
        let endDay = end.isEmpty ? start : end
        if endDay < start { return true }
        return !allDay && endDay == start && endTime < startTime
    }
    private var fields: [String] { [start, end, notes, allDay ? "all" : "\(startTime)-\(endTime)", zone] }

    var body: some View {
        FormScaffold(
            title: visitId == nil ? "New Visit" : "Edit Visit", subtitle: placeName,
            canSave: base != nil && !start.isEmpty && !endsBeforeStart,
            saving: state?.isSavingLocation ?? false,
            dirty: base != nil && fields != initial,
            error: base == nil ? nil : state?.errorMessage,
            clearError: { vm.clearError() },
            save: save
        ) {
            if base == nil {
                if let error = state?.loadError {
                    ContentUnavailableView("Couldn't load the place", systemImage: "wifi.exclamationmark", description: Text(error))
                    Button("Try again") { vm.retryLoad() }.buttonStyle(.glassProminent)
                } else {
                    ProgressView().frame(maxWidth: .infinity).padding(.top, 60)
                }
            } else {
                FormCard {
                    DateRow(title: "From", value: $start)
                    if !allDay {
                        FormDivider()
                        TimeRow(title: "At", value: $startTime)
                    }
                    FormDivider()
                    DateRow(title: "To", value: $end)
                    if !allDay {
                        FormDivider()
                        TimeRow(title: "Until", value: $endTime)
                    }
                }
                FormCard {
                    ToggleRow(title: "All day", isOn: $allDay)
                    if !allDay {
                        FormDivider()
                        Picker(selection: $zone) {
                            ForEach(TimeZone.knownTimeZoneIdentifiers, id: \.self) { id in
                                Text(id.replacingOccurrences(of: "_", with: " ")).tag(id)
                            }
                        } label: {
                            Text("Time zone").foregroundStyle(Theme.ink)
                        }
                        .pickerStyle(.navigationLink)
                        .padding(.horizontal, 16).frame(minHeight: 52)
                    }
                }
                if endsBeforeStart {
                    Label("The end is before the start.", systemImage: "exclamationmark.circle.fill")
                        .font(.footnote).foregroundStyle(Theme.Tone.coral.ink)
                }
                FormCard { FormField(placeholder: "Notes", text: $notes, axis: .vertical) }
                if visitId != nil {
                    Button(role: .destructive) { confirmingDelete = true } label: {
                        Text("Delete Visit").font(.headline).frame(maxWidth: .infinity, minHeight: 52)
                    }
                    .buttonStyle(.plain)
                    .foregroundStyle(Theme.Tone.coral.ink)
                    .mistCard()
                    .disabled(state?.isSavingLocation ?? false)
                    .accessibilityIdentifier("deleteVisit")
                }
            }
        }
        .confirmationDialog("Delete this visit?", isPresented: $confirmingDelete, titleVisibility: .visible) {
            Button("Delete Visit", role: .destructive) {
                guard let base, let visitId else { return }
                vm.saveLocation(formData: IosForms.shared.removeVisit(base: base, id: visitId), intoCollectionId: nil)
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("The place keeps its other visits.")
        }
        .task {
            for await s in vm.uiState {
                state = s
                if base == nil, let place = s.existingLocation {
                    seed(IosForms.shared.place(location: place, defaultCurrency: vm.defaultCurrency))
                }
                if s.isSaved { NotificationCenter.default.post(name: .placeSaved, object: nil); onSaved(); dismiss() }
            }
        }
    }

    private func seed(_ form: LocationFormData) {
        base = form
        if let visitId, let visit = form.visits.first(where: { $0.id == visitId }) {
            start = visit.startDate
            end = visit.endDate ?? ""
            notes = visit.notes
            allDay = visit.isAllDay
            if let t = visit.startTime { startTime = t }
            if let t = visit.endTime { endTime = t }
            zone = visit.timezone
        }
        initial = fields
    }

    private func save() {
        guard let base else { return }
        let form = if let visitId {
            IosForms.shared.editVisit(base: base, id: visitId, start: start, end: end, notes: notes,
                                      startTime: startTime, endTime: endTime, allDay: allDay, timezone: zone)
        } else {
            IosForms.shared.addVisit(base: base, start: start, end: end, notes: notes,
                                     startTime: startTime, endTime: endTime, allDay: allDay, timezone: zone)
        }
        vm.saveLocation(formData: form, intoCollectionId: nil)
    }
}

/// An hour of the day as "HH:mm", picked with the wheel.
private struct TimeRow: View {
    let title: String
    @Binding var value: String

    private static let format: DateFormatter = {
        let f = DateFormatter()
        f.locale = Locale(identifier: "en_US_POSIX")
        f.dateFormat = "HH:mm"
        return f
    }()

    var body: some View {
        HStack {
            Text(title).foregroundStyle(Theme.ink)
            Spacer(minLength: 8)
            DatePicker(title, selection: Binding(
                get: { Self.format.date(from: value) ?? .now },
                set: { value = Self.format.string(from: $0) }
            ), displayedComponents: .hourAndMinute)
            .labelsHidden()
        }
        .padding(.horizontal, 16)
        .frame(minHeight: 52)
    }
}
