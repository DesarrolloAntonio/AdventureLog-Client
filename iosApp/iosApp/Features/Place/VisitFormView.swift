import SwiftUI
import ComposeApp

/// A new visit to a place: the day it began, the day it ended if it was longer, and a note. It is
/// saved with the place, as the web does, so the place's other visits stay as they were.
struct VisitFormView: View {
    let placeId: String
    let placeName: String
    let onSaved: () -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state: AddEditAdventureUiState?
    @State private var base: LocationFormData?
    @State private var start = ISODates.format(.now, withTime: false)
    @State private var end = ""
    @State private var notes = ""
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditAdventureViewModel { host.owner.addEditAdventureViewModel(adventureId: placeId) }
    private var endsBeforeStart: Bool { !end.isEmpty && end < start }

    var body: some View {
        FormScaffold(
            title: "New Visit", subtitle: placeName,
            canSave: base != nil && !start.isEmpty && !endsBeforeStart,
            saving: state?.isSavingLocation ?? false,
            dirty: !notes.isEmpty || !end.isEmpty,
            error: base == nil ? nil : state?.errorMessage,
            clearError: { vm.clearError() },
            save: {
                guard let base else { return }
                vm.saveLocation(formData: IosForms.shared.addVisit(base: base, start: start, end: end, notes: notes,
                                                                     timezone: TimeZone.current.identifier),
                                intoCollectionId: nil)
            }
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
                    FormDivider()
                    DateRow(title: "To", value: $end)
                }
                if endsBeforeStart {
                    Label("The end is before the start.", systemImage: "exclamationmark.circle.fill")
                        .font(.footnote).foregroundStyle(Theme.Tone.coral.ink)
                }
                FormCard { FormField(placeholder: "Notes", text: $notes, axis: .vertical) }
            }
        }
        .task {
            for await s in vm.uiState {
                state = s
                if base == nil, let place = s.existingLocation {
                    base = IosForms.shared.place(location: place, defaultCurrency: vm.defaultCurrency)
                }
                if s.isSaved { onSaved(); dismiss() }
            }
        }
    }
}
