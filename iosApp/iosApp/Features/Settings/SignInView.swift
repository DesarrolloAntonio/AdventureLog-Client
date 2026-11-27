import SwiftUI
import ComposeApp

/// Sign-in and email: the account's addresses (which is primary, which still needs verifying, a
/// menu on each), adding one, and changing the password. Every change goes through the shared
/// SettingsViewModel, which re-reads the addresses from the server after each.
struct SignInView: View {
    @StateObject private var host = ViewModelHost()
    @State private var emails: EmailsSectionState?
    @State private var changingPassword = false
    @State private var message: String?
    @State private var newEmail = ""
    @State private var current = ""
    @State private var new = ""
    @State private var repeated = ""
    @State private var removing: EmailAddress?

    private var vm: SettingsViewModel { host.owner.settingsViewModel() }

    private var passwordProblem: String? {
        if new.isEmpty { return nil }
        if new.count < 8 { return "At least 8 characters." }
        if !repeated.isEmpty && repeated != new { return "The two new passwords don't match." }
        return nil
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                SectionLabel(text: "Email addresses").padding(.bottom, -6)
                FormCard { addresses }

                FormCard {
                    HStack(spacing: 10) {
                        TextField("Add an address", text: $newEmail)
                            .keyboardType(.emailAddress)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                            .textContentType(.emailAddress)
                            .submitLabel(.send)
                            .onSubmit(add)
                        Button("Add", action: add)
                            .font(.subheadline.weight(.semibold))
                            .disabled(!newEmail.contains("@") || emails?.isBusy == true)
                    }
                    .padding(.horizontal, 16)
                    .frame(minHeight: 52)
                }
                Text("We send a link to a new address; it can sign you in once you open it.")
                    .font(.footnote).foregroundStyle(Theme.muted).padding(.horizontal, 6)

                SectionLabel(text: "Password").padding(.top, 10).padding(.bottom, -6)
                FormCard {
                    SecureField("Current password", text: $current)
                        .textContentType(.password)
                        .padding(.horizontal, 16).frame(minHeight: 50)
                    FormDivider()
                    SecureField("New password", text: $new)
                        .textContentType(.newPassword)
                        .padding(.horizontal, 16).frame(minHeight: 50)
                    FormDivider()
                    SecureField("Repeat the new password", text: $repeated)
                        .textContentType(.newPassword)
                        .padding(.horizontal, 16).frame(minHeight: 50)
                }
                if let passwordProblem {
                    Label(passwordProblem, systemImage: "exclamationmark.circle.fill")
                        .font(.footnote).foregroundStyle(Theme.Tone.coral.ink)
                }
                Button {
                    vm.changePassword(currentPassword: current, newPassword: new) {
                        current = ""; new = ""; repeated = ""
                    }
                } label: {
                    ZStack {
                        Text("Change password").opacity(changingPassword ? 0 : 1)
                        if changingPassword { ProgressView().tint(.white) }
                    }
                    .font(.headline)
                    .frame(maxWidth: .infinity, minHeight: 50)
                    .background { HeroGlow().clipShape(.rect(cornerRadius: 16)) }
                    .heroCard(radius: 16)
                }
                .buttonStyle(.plain)
                .disabled(current.isEmpty || new.count < 8 || repeated != new || changingPassword)
                .opacity(current.isEmpty || new.count < 8 || repeated != new ? 0.5 : 1)
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 60)
        }
        .scrollDismissesKeyboard(.interactively)
        .mistBackground()
        .navigationTitle("Sign-in & email")
        // Large, like People beside it in Settings.
        .navigationBarTitleDisplayMode(.large)
        .confirmationDialog("Remove \(removing?.email ?? "")?", isPresented: Binding(get: { removing != nil }, set: { if !$0 { removing = nil } }),
                            titleVisibility: .visible) {
            Button("Remove", role: .destructive) { if let removing { vm.removeEmail(address: removing.email) } }
        } message: {
            Text("You won't be able to sign in with it any more.")
        }
        .alert(message ?? "", isPresented: Binding(get: { message != nil }, set: { if !$0 { message = nil } })) {
            Button("OK", role: .cancel) {}
        }
        .task { for await e in vm.emails { emails = e } }
        .task { for await c in vm.isChangingPassword { changingPassword = c.boolValue } }
        .task { for await m in vm.messages { message = m } }
    }

    @ViewBuilder
    private var addresses: some View {
        if let emails, !emails.isLoading || !emails.addresses.isEmpty {
            if let error = emails.error, emails.addresses.isEmpty {
                HStack {
                    Text(error).font(.subheadline).foregroundStyle(Theme.muted)
                    Spacer()
                    Button("Try again") { vm.loadEmails() }.font(.subheadline.weight(.semibold))
                }
                .padding(16)
            } else if emails.addresses.isEmpty {
                Text("No addresses on this account.").font(.subheadline).foregroundStyle(Theme.muted).padding(16)
            } else {
                ForEach(Array(emails.addresses.enumerated()), id: \.element.email) { index, address in
                    if index > 0 { FormDivider(inset: 56) }
                    row(address)
                }
            }
        } else {
            ProgressView().frame(maxWidth: .infinity, minHeight: 60)
        }
    }

    private func row(_ address: EmailAddress) -> some View {
        HStack(spacing: 12) {
            ToneBadge(systemImage: "envelope", tone: address.primary ? .teal : .blue)
            VStack(alignment: .leading, spacing: 4) {
                Text(address.email).foregroundStyle(Theme.ink).lineLimit(1).truncationMode(.middle)
                HStack(spacing: 6) {
                    if address.primary { ToneTag(text: "Primary", tone: .teal) }
                    ToneTag(text: address.verified ? "Verified" : "Not verified", tone: address.verified ? .green : .amber)
                }
            }
            Spacer(minLength: 8)
            Menu {
                if !address.primary && address.verified {
                    Button("Make primary", systemImage: "star") { vm.setPrimaryEmail(address: address.email) }
                }
                if !address.verified {
                    Button("Send verification again", systemImage: "paperplane") { vm.verifyEmail(address: address.email) }
                }
                if !address.primary {
                    Button("Remove", systemImage: "trash", role: .destructive) { removing = address }
                }
            } label: {
                Image(systemName: "ellipsis").frame(width: 36, height: 36).contentShape(.rect)
            }
            .disabled(address.primary && address.verified)
            .opacity(address.primary && address.verified ? 0 : 1)
            .accessibilityLabel("Actions for \(address.email)")
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
    }

    private func add() {
        guard newEmail.contains("@") else { return }
        vm.addEmail(address: newEmail)
        newEmail = ""
    }
}
