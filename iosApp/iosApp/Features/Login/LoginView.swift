import AuthenticationServices
import SwiftUI
import ComposeApp

/// Sign-in, as the Mist-Login artboard draws it: the app's teal tile and name over the mist, and
/// the form on a frosted card: "Connect to server", three fields, a teal Sign in.
///
/// The fields keep their text here, in `@State`, and push each change to the ViewModel; they never
/// show the value echoed back from its StateFlow, which dropped characters when typed fast.
struct LoginView: View {
    var onSignedIn: () -> Void = {}
    @StateObject private var host = ViewModelHost()
    @State private var server = ""
    @State private var username = ""
    @State private var password = ""
    @State private var state: LoginUiState = LoginUiStateCheckingSession.shared
    @State private var form: LoginFormState?
    @State private var code = ""
    @Environment(\.webAuthenticationSession) private var webAuthentication

    private var vm: LoginViewModel { host.owner.loginViewModel() }

    var body: some View {
        ZStack {
            MistBackground()

            switch onEnum(of: state) {
            case .checkingSession:
                ProgressView().controlSize(.large)
            default:
                content
            }
        }
        .task {
            // Sessions stay signed in on this phone, as the Android app does by default.
            vm.updateRememberSession(value: true)
            // A stored session also arrives as Success at start; only one that follows a sign-in
            // (sent, or waiting for its code) counts as signing in.
            var signingIn = false
            for await s in vm.uiState {
                state = s
                switch onEnum(of: s) {
                case .loading, .codeRequired: signingIn = true
                case .success: if signingIn { onSignedIn() }
                default: break
                }
            }
        }
        .task {
            // A provider sign-in opens the server's page in an in-app browser sheet; the address it
            // comes back to goes to the shared code, which trades it for a session.
            for await url in vm.browserRequests { await signInInBrowser(url) }
        }
        .task {
            for await f in vm.loginFormState {
                form = f
                // Fill the fields once from what the ViewModel restored (remembered server and
                // user); after that, the fields are the source.
                if server.isEmpty, !f.serverUrl.isEmpty { server = f.serverUrl }
                if username.isEmpty, !f.userName.isEmpty { username = f.userName }
            }
        }
    }

    private var content: some View {
        ScrollView {
            VStack(spacing: 0) {
                header.padding(.top, 64)
                Group {
                    if case .codeRequired(let step) = onEnum(of: state) {
                        codeCard(step)
                    } else {
                        card
                    }
                }
                .padding(.top, 34)
                Label("Your data stays on your server", systemImage: "lock.shield.fill")
                    .font(.footnote)
                    .foregroundStyle(Theme.muted)
                    .padding(.top, 28)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 24)
            .frame(maxWidth: 480)
            .frame(maxWidth: .infinity)
        }
        .scrollBounceBehavior(.basedOnSize)
    }

    private var header: some View {
        VStack(spacing: 4) {
            Image(systemName: "mountain.2.fill")
                .font(.system(size: 36, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 84, height: 84)
                .background { HeroGlow().clipShape(.rect(cornerRadius: 22)) }
                .heroCard(radius: 22)
                .accessibilityHidden(true)
            Text("AdventureLog")
                .font(Theme.title())
                .tracking(-0.5)
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .foregroundStyle(Theme.ink)
                .padding(.top, 14)
            Text("Your travels, on your own server.")
                .font(.subheadline)
                .foregroundStyle(Theme.muted)
        }
        .multilineTextAlignment(.center)
    }

    private var isLoading: Bool {
        if case .loading = onEnum(of: state) { return true }
        return false
    }

    private var canSignIn: Bool {
        !server.trimmingCharacters(in: .whitespaces).isEmpty && !username.isEmpty && !password.isEmpty && !isLoading
    }

    private var errorText: String? {
        if case .error(let e) = onEnum(of: state) { return e.message }
        if form?.urlError == true, !server.isEmpty { return "That doesn't look like a server address." }
        return nil
    }

    private var card: some View {
        VStack(spacing: 10) {
            VStack(spacing: 2) {
                Text("Connect to server").font(.headline).foregroundStyle(Theme.ink)
                Text("Sign in with your AdventureLog account").font(.footnote).foregroundStyle(Theme.muted)
            }
            .multilineTextAlignment(.center)
            .padding(.bottom, 6)

            FieldRow(systemImage: "link") {
                TextField("https://your-server", text: $server)
                    .textContentType(.URL)
                    .keyboardType(.URL)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .accessibilityLabel("Server")
                    .accessibilityIdentifier("server")
            }
            FieldRow(systemImage: "person") {
                TextField("Username", text: $username)
                    .textContentType(.username)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .accessibilityIdentifier("username")
            }
            FieldRow(systemImage: "lock") {
                SecureField("Password", text: $password)
                    .textContentType(.password)
                    .accessibilityIdentifier("password")
                    .onSubmit(signIn)
            }

            if let errorText {
                Label(errorText, systemImage: "exclamationmark.circle.fill")
                    .font(.footnote.weight(.medium))
                    .foregroundStyle(Theme.Tone.coral.ink)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 4)
                    .accessibilityIdentifier("loginError")
            }

            Button(action: signIn) {
                ZStack {
                    Text("Sign in").opacity(isLoading ? 0 : 1)
                    if isLoading { ProgressView().tint(.white) }
                }
                .font(.headline)
                .frame(maxWidth: .infinity, minHeight: 50)
                .background { HeroGlow().clipShape(.rect(cornerRadius: 16)) }
                .heroCard(radius: 16)
            }
            .buttonStyle(.plain)
            .disabled(!canSignIn)
            .opacity(canSignIn || isLoading ? 1 : 0.55)
            .padding(.top, 6)
            .accessibilityIdentifier("signIn")

            if let providers = form?.providers, !providers.isEmpty {
                HStack(spacing: 10) {
                    Rectangle().fill(Theme.ink.opacity(0.12)).frame(height: 1)
                    Text("or").font(.footnote).foregroundStyle(Theme.muted)
                    Rectangle().fill(Theme.ink.opacity(0.12)).frame(height: 1)
                }
                .padding(.vertical, 4)
                ForEach(providers, id: \.id) { provider in
                    Button { vm.signInWithProvider(provider: provider) } label: {
                        Text("Continue with \(provider.name)")
                            .font(.headline)
                            .foregroundStyle(Theme.ink)
                            .frame(maxWidth: .infinity, minHeight: 48)
                            .background(Color(light: 0xFFFFFF, dark: 0x1B262C).opacity(0.85), in: .rect(cornerRadius: 16, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).strokeBorder(Theme.ink.opacity(0.12)))
                    }
                    .buttonStyle(.plain)
                    .disabled(isLoading)
                    .accessibilityIdentifier("provider-\(provider.id)")
                }
            }

            // The same guide Settings links to: the upstream AdventureLog project.
            Link("How do I set up a server?", destination: URL(string: "https://github.com/seanmorley15/AdventureLog")!)
                .font(.subheadline.weight(.semibold))
                .frame(minHeight: 40)
        }
        .padding(EdgeInsets(top: 20, leading: 18, bottom: 12, trailing: 18))
        .mistCard(radius: 26)
        .onChange(of: server) { _, v in vm.updateServerUrl(newUrl: v) }
        .onChange(of: username) { _, v in vm.updateUserName(newUserName: v) }
        .onChange(of: password) { _, v in vm.updatePassword(newPassword: v) }
    }

    private func signIn() {
        guard canSignIn else { return }
        vm.login()
    }

    /// The second step of an account with two-step verification: the code from the authenticator
    /// app, or a recovery code. The password step is behind it; Cancel goes back to it.
    private func codeCard(_ step: LoginUiStateCodeRequired) -> some View {
        VStack(spacing: 10) {
            VStack(spacing: 2) {
                Text("Two-step verification").font(.headline).foregroundStyle(Theme.ink)
                Text("Enter the code from your authenticator app, or a recovery code.")
                    .font(.footnote)
                    .foregroundStyle(Theme.muted)
            }
            .multilineTextAlignment(.center)
            .padding(.bottom, 6)

            FieldRow(systemImage: "lock.shield") {
                TextField("123456", text: $code)
                    .textContentType(.oneTimeCode)
                    .keyboardType(.numberPad)
                    .font(.title3.monospacedDigit())
                    .accessibilityLabel("Code")
                    .accessibilityIdentifier("loginCode")
                    .onSubmit(verifyCode)
            }

            if let error = step.error {
                Label(error, systemImage: "exclamationmark.circle.fill")
                    .font(.footnote.weight(.medium))
                    .foregroundStyle(Theme.Tone.coral.ink)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 4)
                    .accessibilityIdentifier("loginCodeError")
            }

            Button(action: verifyCode) {
                ZStack {
                    Text("Verify").opacity(step.verifying ? 0 : 1)
                    if step.verifying { ProgressView().tint(.white) }
                }
                .font(.headline)
                .frame(maxWidth: .infinity, minHeight: 50)
                .background { HeroGlow().clipShape(.rect(cornerRadius: 16)) }
                .heroCard(radius: 16)
            }
            .buttonStyle(.plain)
            .disabled(code.isEmpty || step.verifying)
            .opacity(code.isEmpty && !step.verifying ? 0.55 : 1)
            .padding(.top, 6)
            .accessibilityIdentifier("verifyCode")

            Button("Cancel") {
                code = ""
                vm.cancelLoginCode()
            }
            .font(.subheadline.weight(.semibold))
            .frame(minHeight: 40)
        }
        .padding(EdgeInsets(top: 20, leading: 18, bottom: 12, trailing: 18))
        .mistCard(radius: 26)
        // A wrong code empties the field, as the ViewModel does with its own copy.
        .onChange(of: step.error) { _, error in if error != nil { code = "" } }
    }

    /// The field is the source of the code: it goes to the ViewModel at the moment it is sent,
    /// not echoed back from it on every keystroke (that round trip left Verify disabled).
    private func verifyCode() {
        let digits = code.filter(\.isNumber)
        guard !digits.isEmpty else { return }
        vm.updateLoginCode(newCode: digits)
        vm.verifyLoginCode()
    }

    /// Opens the provider's sign-in in the system's web-authentication sheet and hands the address
    /// it comes back to to the shared code. Closing the sheet leaves the form as it was.
    private func signInInBrowser(_ address: String) async {
        guard let url = URL(string: address) else { return }
        do {
            let callback = try await webAuthentication.authenticate(
                using: url,
                callbackURLScheme: "adventurelog",
                preferredBrowserSession: .ephemeral
            )
            host.owner.deliverSignInRedirect(uri: callback.absoluteString)
        } catch {
            // Cancelled by the user, or the sheet could not open: nothing was started on the server.
        }
    }
}

/// One field of the form: its icon, then the control, on a near-white rounded box 46 points high.
private struct FieldRow<Content: View>: View {
    let systemImage: String
    @ViewBuilder let content: Content

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: systemImage)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(Theme.muted)
                .frame(width: 20)
            content
                .foregroundStyle(Theme.ink)
                .tint(Theme.accent)
        }
        .padding(.horizontal, 12)
        .frame(minHeight: 46)
        .background(Color(light: 0xFFFFFF, dark: 0x1B262C).opacity(0.85), in: .rect(cornerRadius: Theme.Radius.field, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: Theme.Radius.field, style: .continuous).strokeBorder(Theme.ink.opacity(0.08)))
    }
}
