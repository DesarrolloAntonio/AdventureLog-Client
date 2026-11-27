import SwiftUI
import ComposeApp

/// New or edit checklist, as the Mist-ChecklistForm artboard draws it: the name, how far through
/// it is, its lines with a tick each, and a field to add one.
struct ChecklistFormView: View {
    let collectionId: String
    let collectionName: String
    var checklistId: String?
    let onSaved: () -> Void
    /// Deletes the one being edited; nil for a new one.
    var onDelete: (() -> Void)? = nil

    @StateObject private var host = ViewModelHost()
    @State private var state = ChecklistFormState(name: "", lines: [], draft: "", date: "", isPublic: false, isSaving: false,
                                                  isLoading: false, loadError: nil, error: nil, saved: false)
    @State private var name = ""
    @State private var draft = ""
    @State private var date = ""
    @State private var isPublic = false
    @State private var seeded = false
    @FocusState private var adding: Bool
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditChecklistViewModel { host.owner.addEditChecklistViewModel() }

    var body: some View {
        FormScaffold(
            title: checklistId == nil ? "New Checklist" : "Edit Checklist", subtitle: collectionName,
            canSave: state.canSave, saving: state.isSaving, dirty: vm.hasChanges(form: state),
            error: state.error, clearError: { vm.clearError() }, save: { vm.save(collectionId: collectionId) },
            delete: onDelete.map { action in ("Delete Checklist", action) }) {
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 60)
            } else if let error = state.loadError {
                ContentUnavailableView("Couldn't load the checklist", systemImage: "wifi.exclamationmark", description: Text(error))
                Button("Try again") { vm.retryLoad(collectionId: collectionId) }.buttonStyle(.glassProminent)
            } else {
                FormCard {
                    FormField(placeholder: "Name", text: $name, font: .title3.weight(.semibold))
                    FormDivider()
                    DateRow(title: "Day", value: $date)
                }

                if !state.lines.isEmpty {
                    let left = state.lines.count - Int(state.doneCount)
                    SectionLabel(text: "\(state.doneCount) of \(state.lines.count) done · \(left) to go")
                        .padding(.bottom, -8)
                }
                FormCard {
                    ForEach(Array(state.lines.enumerated()), id: \.offset) { index, line in
                        HStack(spacing: 12) {
                            Button { vm.toggleLine(index: Int32(index)) } label: {
                                Image(systemName: line.checked ? "checkmark.circle.fill" : "circle")
                                    .font(.title3)
                                    .foregroundStyle(line.checked ? Theme.accent : Theme.glyph)
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel(line.name)
                            .accessibilityValue(line.checked ? "Done" : "Not done")
                            Text(line.name)
                                .foregroundStyle(line.checked ? Theme.muted : Theme.ink)
                                .strikethrough(line.checked, color: Theme.muted)
                            Spacer()
                            Button { vm.removeLine(index: Int32(index)) } label: {
                                Image(systemName: "minus.circle").foregroundStyle(Theme.Tone.coral.ink)
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Remove \(line.name)")
                        }
                        .padding(.horizontal, 16)
                        .frame(minHeight: 48)
                        FormDivider(inset: 52)
                    }
                    HStack(spacing: 12) {
                        Image(systemName: "plus.circle.fill").font(.title3).foregroundStyle(Theme.accent)
                        TextField("Add an item", text: $draft)
                            .focused($adding)
                            .submitLabel(.next)
                            .onSubmit { vm.addLine(); draft = ""; adding = true }
                    }
                    .padding(.horizontal, 16)
                    .frame(minHeight: 50)
                }

                FormCard { ToggleRow(title: "Make public", isOn: $isPublic) }
            }
        }
        .onChange(of: name) { _, v in vm.onNameChange(value: v) }
        .onChange(of: draft) { _, v in vm.onDraftChange(value: v) }
        .onChange(of: date) { _, v in vm.onDateChange(value: v) }
        .onChange(of: isPublic) { _, v in vm.onPublicChange(value: v) }
        .task { vm.load(collectionId: collectionId, id: checklistId) }
        .task {
            for await s in vm.state {
                state = s
                if !seeded, !s.isLoading, s.loadError == nil {
                    name = s.name; date = s.date; isPublic = s.isPublic
                    seeded = true
                }
                if s.saved { onSaved(); dismiss() }
            }
        }
    }
}
