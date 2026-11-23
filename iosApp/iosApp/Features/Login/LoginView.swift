import SwiftUI
import ComposeApp

/// Sign-in, as the iOS-Login artboard draws it: a fjord at sunset behind everything, the app's
/// glass tile and serif name at the top, and the form on a glass card at the bottom.
///
/// The fields keep their text here, in `@State`, and push each change to the ViewModel; they never
/// show the value echoed back from its StateFlow, which dropped characters when typed fast.
struct LoginView: View {
    @StateObject private var host = ViewModelHost()
    @State private var server = ""
    @State private var username = ""
    @State private var password = ""
    @State private var state: LoginUiState = LoginUiStateCheckingSession.shared
    @State private var form: LoginFormState?

    private var vm: LoginViewModel { host.owner.loginViewModel() }

    var body: some View {
        ZStack {
            SunsetFjord().ignoresSafeArea()

            switch onEnum(of: state) {
            case .checkingSession:
                ProgressView().tint(.white).controlSize(.large)
            default:
                content
            }
        }
        .task {
            // Sessions stay signed in on this phone, as the Android app does by default.
            vm.updateRememberSession(value: true)
            for await s in vm.uiState { state = s }
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
        VStack(spacing: 0) {
            header.padding(.top, 56)
            Spacer()
            card
                .padding(.horizontal, 14)
                .padding(.bottom, 8)
        }
    }

    private var header: some View {
        VStack(spacing: 6) {
            Image(systemName: "map")
                .font(.system(size: 34, weight: .semibold))
                .foregroundStyle(.white)
                .frame(width: 72, height: 72)
                .glassEffect(.regular, in: .rect(cornerRadius: 22))
            Text("AdventureLog")
                .font(Theme.display(40))
                .foregroundStyle(.white)
                .padding(.top, 12)
            Text("Your travels, on your own server.")
                .font(.body)
                .foregroundStyle(.white.opacity(0.9))
        }
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
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 6) {
                Text("Server").font(.footnote.weight(.semibold)).foregroundStyle(.white).padding(.leading, 6)
                    .shadow(color: .black.opacity(0.25), radius: 2)
                FieldRow(systemImage: "globe") {
                    TextField("https://your-server", text: $server)
                        .textContentType(.URL)
                        .keyboardType(.URL)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .accessibilityIdentifier("server")
                }
                .background(.white.opacity(0.9), in: .rect(cornerRadius: 16))
            }

            VStack(spacing: 0) {
                FieldRow(systemImage: "person") {
                    TextField("Username", text: $username)
                        .textContentType(.username)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .accessibilityIdentifier("username")
                }
                Divider().padding(.leading, 44)
                FieldRow(systemImage: "lock") {
                    SecureField("Password", text: $password)
                        .textContentType(.password)
                        .accessibilityIdentifier("password")
                        .onSubmit(signIn)
                }
            }
            .background(.white.opacity(0.9), in: .rect(cornerRadius: 16))

            if let errorText {
                Text(errorText)
                    .font(.footnote.weight(.medium))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 6)
                    .accessibilityIdentifier("loginError")
            }

            Button(action: signIn) {
                ZStack {
                    Text("Sign In").opacity(isLoading ? 0 : 1)
                    if isLoading { ProgressView().tint(.white) }
                }
                .font(.headline)
                .frame(maxWidth: .infinity, minHeight: 54)
            }
            .buttonStyle(.glassProminent)
            .tint(Theme.accent)
            .disabled(!canSignIn)
            .padding(.top, 4)
            .accessibilityIdentifier("signIn")

            // The same guide Settings links to: the upstream AdventureLog project.
            Link("How do I set up a server?", destination: URL(string: "https://github.com/seanmorley15/AdventureLog")!)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity, minHeight: 44)
        }
        .padding(EdgeInsets(top: 22, leading: 18, bottom: 14, trailing: 18))
        .glassEffect(.regular, in: .rect(cornerRadius: 36))
        .onChange(of: server) { _, v in vm.updateServerUrl(newUrl: v) }
        .onChange(of: username) { _, v in vm.updateUserName(newUserName: v) }
        .onChange(of: password) { _, v in vm.updatePassword(newPassword: v) }
    }

    private func signIn() {
        guard canSignIn else { return }
        vm.login()
    }
}

/// One field of the form: its icon, then the control, at the canvas's 50-point height.
private struct FieldRow<Content: View>: View {
    let systemImage: String
    @ViewBuilder let content: Content

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: systemImage)
                .foregroundStyle(Color(white: 0.39))
                .frame(width: 20)
            content
                .foregroundStyle(.black)
                .tint(Theme.accent)
        }
        .padding(.horizontal, 14)
        .frame(height: 50)
        // The fields are light in both themes, as the artboard draws them; a dark scheme would
        // paint white placeholders on white.
        .environment(\.colorScheme, .light)
    }
}

/// The artboard's illustration: a dusk sky over a lake, a low sun and its reflection.
private struct SunsetFjord: View {
    var body: some View {
        GeometryReader { geo in
            let h = geo.size.height, w = geo.size.width
            ZStack(alignment: .top) {
                LinearGradient(
                    stops: [
                        .init(color: Color(red: 0.24, green: 0.36, blue: 0.54), location: 0),
                        .init(color: Color(red: 0.85, green: 0.60, blue: 0.53), location: 0.45),
                        .init(color: Color(red: 0.96, green: 0.81, blue: 0.66), location: 0.62),
                    ],
                    startPoint: .top, endPoint: .bottom
                )
                RadialGradient(
                    colors: [Color(red: 1, green: 0.95, blue: 0.84), Color(red: 1, green: 0.88, blue: 0.71).opacity(0)],
                    center: .center, startRadius: 0, endRadius: w * 0.23
                )
                .frame(width: w * 0.46, height: w * 0.46)
                .position(x: w * 0.69, y: h * 0.557)
                Circle()
                    .fill(Color(red: 1, green: 0.95, blue: 0.87))
                    .frame(width: w * 0.113)
                    .position(x: w * 0.69, y: h * 0.557)
                LinearGradient(
                    colors: [Color(red: 0.37, green: 0.50, blue: 0.60), Color(red: 0.08, green: 0.13, blue: 0.18)],
                    startPoint: .top, endPoint: .bottom
                )
                .frame(height: h * 0.36)
                .frame(maxHeight: .infinity, alignment: .bottom)
                Rectangle()
                    .fill(Color(red: 0.97, green: 0.80, blue: 0.65).opacity(0.4))
                    .frame(width: w * 0.041, height: h * 0.095)
                    .position(x: w * 0.69, y: h * 0.69)
            }
        }
    }
}
