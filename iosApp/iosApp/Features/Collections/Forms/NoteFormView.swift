import SwiftUI
import ComposeApp

/// New or edit note, as the Mist-NoteForm artboard draws it: the title, its day, the text, and
/// whether it is public.
struct NoteFormView: View {
    let collectionId: String
    let collectionName: String
    var noteId: String?
    let onSaved: () -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state = NoteFormState(name: "", content: "", date: "", isPublic: false, isSaving: false,
                                             isLoading: false, loadError: nil, error: nil, saved: false)
    @State private var name = ""
    @State private var content = ""
    @State private var date = ""
    @State private var isPublic = false
    @State private var seeded = false
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditNoteViewModel { host.owner.addEditNoteViewModel() }

    var body: some View {
        FormScaffold(
            title: noteId == nil ? "New Note" : "Edit Note", subtitle: collectionName,
            canSave: state.canSave, saving: state.isSaving, dirty: vm.hasChanges(form: state),
            error: state.error, clearError: { vm.clearError() }, save: { vm.save(collectionId: collectionId) }
        ) {
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 60)
            } else if let error = state.loadError {
                ContentUnavailableView("Couldn't load the note", systemImage: "wifi.exclamationmark", description: Text(error))
                Button("Try again") { vm.retryLoad(collectionId: collectionId) }.buttonStyle(.glassProminent)
            } else {
                FormCard {
                    FormField(placeholder: "Title", text: $name, font: .title3.weight(.semibold))
                    FormDivider()
                    DateRow(title: "Day", value: $date)
                }
                FormCard {
                    FormField(placeholder: "Write the note", text: $content, axis: .vertical)
                }
                FormCard { ToggleRow(title: "Make public", isOn: $isPublic) }
            }
        }
        .onChange(of: name) { _, v in vm.onNameChange(value: v) }
        .onChange(of: content) { _, v in vm.onContentChange(value: v) }
        .onChange(of: date) { _, v in vm.onDateChange(value: v) }
        .onChange(of: isPublic) { _, v in vm.onPublicChange(value: v) }
        .task { vm.load(collectionId: collectionId, id: noteId) }
        .task {
            for await s in vm.state {
                state = s
                // The fields take what was loaded once; after that they are the source.
                if !seeded, !s.isLoading, s.loadError == nil {
                    name = s.name; content = s.content; date = s.date; isPublic = s.isPublic
                    seeded = true
                }
                if s.saved { onSaved(); dismiss() }
            }
        }
    }
}
