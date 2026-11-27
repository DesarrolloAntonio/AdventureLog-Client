import SwiftUI
import ComposeApp

/// The name and username, as Android's Edit profile dialog has them, saved together. The sheet
/// waits for the server: a refused username ("already taken") is said here, with what was typed
/// still in the fields, rather than after the sheet has gone.
struct ProfileFormView: View {
    let vm: SettingsViewModel
    let initial: ProfileForm

    @State private var username: String
    @State private var firstName: String
    @State private var lastName: String
    @State private var saving = false
    @State private var refusal: String?
    @Environment(\.dismiss) private var dismiss

    init(vm: SettingsViewModel, initial: ProfileForm) {
        self.vm = vm
        self.initial = initial
        _username = State(initialValue: initial.username)
        _firstName = State(initialValue: initial.firstName)
        _lastName = State(initialValue: initial.lastName)
    }

    private var fields: [String] { [username, firstName, lastName] }

    var body: some View {
        FormScaffold(
            title: "Edit Profile",
            canSave: true,
            saving: saving,
            dirty: fields != [initial.username, initial.firstName, initial.lastName],
            error: refusal,
            clearError: { refusal = nil },
            save: save
        ) {
            SectionLabel(text: "Username").padding(.bottom, -8)
            FormCard {
                HStack(spacing: 4) {
                    Text("@").foregroundStyle(Theme.muted)
                    TextField("username", text: $username)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .textContentType(.username)
                }
                .padding(.horizontal, 16).frame(minHeight: 52)
            }
            SectionLabel(text: "Name").padding(.bottom, -8)
            FormCard {
                FormField(placeholder: "First name", text: $firstName)
                FormDivider()
                FormField(placeholder: "Last name", text: $lastName)
            }
            Text("Your username is how other people find you to share a collection.")
                .font(.footnote).foregroundStyle(Theme.faint).padding(.horizontal, 6)
        }
    }

    private func save() {
        saving = true
        vm.saveIdentity(
            username: username.trimmingCharacters(in: .whitespaces),
            firstName: firstName.trimmingCharacters(in: .whitespaces),
            lastName: lastName.trimmingCharacters(in: .whitespaces)
        ) { refused in
            saving = false
            if let refused { refusal = refused } else { dismiss() }
        }
    }
}
