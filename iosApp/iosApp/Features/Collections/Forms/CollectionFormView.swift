import SwiftUI
import ComposeApp

/// New or edit collection, as the Mist-CollectionForm artboard draws it: name and description,
/// the dates with how many nights they make, and sharing (public, link).
struct CollectionFormView: View {
    var collectionId: String?
    let onSaved: () -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state = AddEditCollectionUiState(isLoading: false, isSaving: false, isSaved: false, errorMessage: nil, initialData: nil)
    @State private var name = ""
    @State private var about = ""
    @State private var start = ""
    @State private var end = ""
    @State private var isPublic = false
    @State private var link = ""
    @State private var seeded = false
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditCollectionViewModel { host.owner.addEditCollectionViewModel(collectionId: collectionId) }

    private var form: CollectionFormData {
        CollectionFormData(name: name, description: about, isPublic: isPublic, startDate: start, endDate: end, link: link)
    }

    private var dirty: Bool {
        guard let initial = state.initialData else { return !name.isEmpty || !about.isEmpty || !start.isEmpty || !link.isEmpty }
        return form != initial
    }

    var body: some View {
        FormScaffold(
            title: collectionId == nil ? "New Collection" : "Edit Collection",
            canSave: !name.trimmingCharacters(in: .whitespaces).isEmpty && !(end < start && !end.isEmpty),
            saving: state.isSaving, dirty: dirty,
            error: state.errorMessage, clearError: { vm.clearSavedState() }, save: { vm.saveCollection(formData: form) }
        ) {
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 60)
            } else {
                FormCard {
                    FormField(placeholder: "Name", text: $name, font: .title3.weight(.semibold))
                    FormDivider()
                    FormField(placeholder: "Description", text: $about, axis: .vertical)
                }
                SectionLabel(text: "Dates").padding(.bottom, -8)
                FormCard {
                    DateRow(title: "Starts", value: $start)
                    FormDivider()
                    DateRow(title: "Ends", value: $end)
                    if let range = DayRange(start: start.isEmpty ? nil : start, end: end.isEmpty ? nil : end), range.nights > 0 {
                        FormDivider()
                        Text(range.nights == 1 ? "1 night" : "\(range.nights) nights")
                            .font(.subheadline).foregroundStyle(Theme.muted)
                            .padding(.horizontal, 16).frame(minHeight: 40)
                    }
                }
                if !end.isEmpty, end < start {
                    Label("The end is before the start.", systemImage: "exclamationmark.circle.fill")
                        .font(.footnote).foregroundStyle(Theme.Tone.coral.ink)
                }
                SectionLabel(text: "Sharing").padding(.bottom, -8)
                FormCard {
                    ToggleRow(title: "Public", isOn: $isPublic)
                    FormDivider()
                    FormField(placeholder: "Link", text: $link, keyboard: .URL, capitalization: .never)
                }
                Text("A public collection can be viewed by anyone with its link.")
                    .font(.footnote).foregroundStyle(Theme.muted).padding(.horizontal, 6)
            }
        }
        .task {
            for await s in vm.uiState {
                state = s
                if !seeded, let d = s.initialData {
                    name = d.name; about = d.description_; start = d.startDate; end = d.endDate; isPublic = d.isPublic; link = d.link
                    seeded = true
                }
                if s.isSaved { onSaved(); dismiss() }
            }
        }
    }
}
