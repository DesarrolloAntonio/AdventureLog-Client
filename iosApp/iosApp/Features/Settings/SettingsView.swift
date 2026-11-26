import SwiftUI
import ComposeApp

/// Settings, as the Mist-Settings artboard draws it: who is signed in and to which server, then
/// Appearance (theme, map style), Units (units, currency), Account (sign-in, public profile,
/// people) and About, with Sign out at the end. Every choice applies on the spot, as on Android.
struct SettingsView: View {
    /// Signing out lives with Home's ViewModel, which owns the session.
    let onSignOut: () -> Void

    @StateObject private var host = ViewModelHost()
    @State private var user: UserDetails?
    @State private var form = ProfileForm(username: "", firstName: "", lastName: "", publicProfile: false,
                                          imperialUnits: false, currency: "USD", mapStyle: "default")
    @State private var theme: CommonThemeMode = .auto
    @State private var message: String?
    @State private var confirmingSignOut = false
    @State private var editingProfile = false
    @State private var storage = StorageSectionState(usage: nil, isLoading: true, error: nil)
    @State private var regionsRefreshing = false
    @State private var backingUp = false

    private var vm: SettingsViewModel { host.owner.settingsViewModel() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Button { editingProfile = true } label: { profileCard }
                    .buttonStyle(.plain)
                    .accessibilityHint("Edit your name and username")
                    .accessibilityIdentifier("editProfile")

                group("Appearance") {
                    MenuRow(systemImage: "circle.lefthalf.filled", tone: .blue, title: "Theme", value: themeName(theme)) {
                        ForEach([CommonThemeMode.auto, .light, .dark], id: \.self) { mode in
                            Button(themeName(mode)) { vm.setThemeMode(mode: mode) }
                        }
                    }
                    Divider().padding(.leading, 56)
                    MenuRow(systemImage: "map", tone: .mint, title: "Map style", value: mapStyleName) {
                        ForEach(MapStyles.shared.options, id: \.code) { option in
                            Button(option.label) { set { $0.with(mapStyle: option.code) } }
                        }
                    }
                }

                group("Units") {
                    MenuRow(systemImage: "ruler", tone: .amber, title: "Units", value: form.imperialUnits ? "Imperial" : "Metric") {
                        Button("Metric") { set { $0.with(imperialUnits: false) } }
                        Button("Imperial") { set { $0.with(imperialUnits: true) } }
                    }
                    Divider().padding(.leading, 56)
                    MenuRow(systemImage: "dollarsign", tone: .green, title: "Currency", value: form.currency) {
                        ForEach(Currencies.shared.options, id: \.first) { pair in
                            Button("\(pair.first ?? "") · \(pair.second ?? "")") { set { $0.with(currency: String(pair.first ?? "")) } }
                        }
                    }
                }

                group("Account") {
                    NavigationLink { SignInView() } label: {
                        SettingRow(systemImage: "envelope", tone: .blue, title: "Sign-in & email", chevron: true)
                    }
                    .buttonStyle(.plain)
                    Divider().padding(.leading, 56)
                    HStack(spacing: 12) {
                        ToneBadge(systemImage: "globe", tone: .mint)
                        Toggle("Public profile", isOn: Binding(
                            get: { form.publicProfile },
                            set: { v in set { $0.with(publicProfile: v) } }
                        ))
                        .tint(Theme.accent)
                        .foregroundStyle(Theme.ink)
                    }
                    .padding(.horizontal, 14)
                    .frame(minHeight: 52)
                    Divider().padding(.leading, 56)
                    NavigationLink { PeopleView(me: user?.username) } label: {
                        SettingRow(systemImage: "person.2", tone: .coral, title: "People", chevron: true)
                    }
                    .buttonStyle(.plain)
                }

                group("Your data") {
                    ActionRow(systemImage: "map.circle", tone: .teal, title: "Update visited regions",
                              detail: "Work out which regions and cities your places fall in",
                              busy: regionsRefreshing) { vm.refreshVisitedRegions() }
                    Divider().padding(.leading, 56)
                    ActionRow(systemImage: "arrow.down.circle", tone: .blue, title: "Download a backup",
                              detail: "Places, visits, collections, media and trails, as one zip",
                              busy: backingUp) { vm.downloadBackup() }
                }

                group("Media storage") { StorageCard(state: storage) { vm.loadStorage() } }

                group("About") {
                    Link(destination: URL(string: "https://github.com/DesarrolloAntonio/AdventureLog-Client")!) {
                        SettingRow(systemImage: "chevron.left.forwardslash.chevron.right", tone: .blue, title: "Source code", external: true)
                    }
                    .buttonStyle(.plain)
                    Divider().padding(.leading, 56)
                    Link(destination: URL(string: "https://github.com/DesarrolloAntonio/AdventureLog-Client/issues")!) {
                        SettingRow(systemImage: "bubble.left", tone: .amber, title: "Send feedback", external: true)
                    }
                    .buttonStyle(.plain)
                    Divider().padding(.leading, 56)
                    NavigationLink { LegalView() } label: {
                        SettingRow(systemImage: "doc.text", tone: .mint, title: "Terms & privacy", chevron: true)
                    }
                    .buttonStyle(.plain)
                    Divider().padding(.leading, 56)
                    Link(destination: URL(string: "https://github.com/seanmorley15/AdventureLog")!) {
                        SettingRow(systemImage: "server.rack", tone: .teal, title: "Server setup guide", external: true)
                    }
                    .buttonStyle(.plain)
                    Divider().padding(.leading, 56)
                    HStack(spacing: 12) {
                        ToneBadge(systemImage: "info.circle", tone: .amber)
                        Text("App version").foregroundStyle(Theme.ink)
                        Spacer(minLength: 8)
                        Text(appVersion).foregroundStyle(Theme.muted).monospacedDigit()
                    }
                    .padding(.horizontal, 14)
                    .frame(minHeight: 52)
                    .accessibilityElement(children: .combine)
                    .textSelection(.enabled)
                }

                Button(role: .destructive) { confirmingSignOut = true } label: {
                    Text("Sign out")
                        .font(.body.weight(.semibold))
                        .foregroundStyle(Theme.Tone.coral.ink)
                        .frame(maxWidth: .infinity, minHeight: 52)
                        .mistCard(radius: Theme.Radius.card)
                }
                .buttonStyle(.plain)
                .padding(.top, 24)
                .accessibilityIdentifier("signOut")
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 120)
        }
        .mistBackground()
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.large)
        .confirmationDialog("Sign out?", isPresented: $confirmingSignOut, titleVisibility: .visible) {
            Button("Sign Out", role: .destructive, action: onSignOut)
        } message: {
            Text("You will need your password to sign back in.")
        }
        .alert(message ?? "", isPresented: Binding(get: { message != nil }, set: { if !$0 { message = nil } })) {
            Button("OK", role: .cancel) {}
        }
        .sheet(isPresented: $editingProfile) { ProfileFormView(vm: vm, initial: form) }
        .task { for await u in vm.user { user = u } }
        .task { for await p in vm.profile { form = p.form } }
        .task { for await t in vm.themeMode { theme = t } }
        .task { for await m in vm.messages { message = m } }
        .task { for await s in vm.storage { storage = s } }
        .task { for await r in vm.regionsRefreshing { regionsRefreshing = r.boolValue } }
        .task { for await b in vm.backupInProgress { backingUp = b.boolValue } }
        // How the regions update went, or why a backup didn't arrive.
        .task {
            for await m in vm.regionsMessage {
                if let m { message = m; vm.clearRegionsMessage() }
            }
        }
    }

    /// One change to the profile, sent to the server at once.
    private func set(_ change: @escaping (ProfileForm) -> ProfileForm) {
        vm.updateProfile(transform: change)
    }

    /// "1.1.0 (12)", as the bundle has it.
    private var appVersion: String {
        let info = Bundle.main.infoDictionary
        let version = info?["CFBundleShortVersionString"] as? String ?? "?"
        let build = info?["CFBundleVersion"] as? String
        return build.map { "\(version) (\($0))" } ?? version
    }

    private var mapStyleName: String {
        MapStyles.shared.options.first { $0.code == form.mapStyle }?.label ?? form.mapStyle
    }

    private func themeName(_ mode: CommonThemeMode) -> String {
        switch mode {
        case .light: "Light"
        case .dark: "Dark"
        default: "System"
        }
    }

    // MARK: Pieces

    private var profileCard: some View {
        HStack(spacing: 12) {
            Text(initials)
                .font(.headline.weight(.bold))
                .foregroundStyle(.white)
                .frame(width: 48, height: 48)
                .background { Circle().fill(HeroFill()) }
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(fullName).font(.headline).foregroundStyle(Theme.ink)
                Text("@\(user?.username ?? "") · \(serverHost)")
                    .font(.footnote).foregroundStyle(Theme.muted).lineLimit(1)
            }
            Spacer()
            Image(systemName: "pencil").font(.body.weight(.semibold)).foregroundStyle(Theme.accent)
        }
        .padding(12)
        .mistCard()
        .accessibilityElement(children: .combine)
    }

    private var fullName: String {
        let name = [user?.firstName, user?.lastName].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " ")
        return name.isEmpty ? (user?.username ?? "") : name
    }

    private var initials: String {
        let letters = [user?.firstName.first, user?.lastName.first].compactMap { $0 }.map(String.init).joined()
        return letters.isEmpty ? String((user?.username ?? "?").prefix(1)).uppercased() : letters.uppercased()
    }

    /// The server's host, without the scheme: what the sign-in screen was given.
    private var serverHost: String {
        let url = vm.getServerUrl()
        return URL(string: url)?.host() ?? url
    }

    @ViewBuilder
    private func group<Content: View>(_ title: String, @ViewBuilder content: () -> Content) -> some View {
        SectionLabel(text: title).padding(.top, 20).padding(.bottom, 8)
        VStack(spacing: 0) { content() }.mistCard()
    }
}

/// A settings row: its badge, its name, and a chevron or an arrow out.
private struct SettingRow: View {
    let systemImage: String
    let tone: Theme.Tone
    let title: String
    var chevron = false
    var external = false

    var body: some View {
        HStack(spacing: 12) {
            ToneBadge(systemImage: systemImage, tone: tone)
            Text(title).foregroundStyle(Theme.ink)
            Spacer(minLength: 8)
            if chevron { Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph) }
            if external { Image(systemName: "arrow.up.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph) }
        }
        .padding(.horizontal, 14)
        .frame(minHeight: 52)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

/// A row that does something on the server: what it does under its name, a spinner while it runs.
private struct ActionRow: View {
    let systemImage: String
    let tone: Theme.Tone
    let title: String
    let detail: String
    let busy: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 12) {
                ToneBadge(systemImage: systemImage, tone: tone)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).foregroundStyle(Theme.ink)
                    Text(detail).font(.footnote).foregroundStyle(Theme.muted)
                }
                Spacer(minLength: 8)
                if busy { ProgressView() }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .frame(minHeight: 52)
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .disabled(busy)
        .accessibilityElement(children: .combine)
    }
}

/// What the account's media takes up, as Android's section shows it: the total, then one bar split
/// by what fills it, with a key. Images are nearly all of it, so separate bars said nothing.
private struct StorageCard: View {
    let state: StorageSectionState
    let retry: () -> Void

    var body: some View {
        if let usage = state.usage {
            let slices: [(String, Int64, Int32, Theme.Tone)] = [
                ("Images", usage.imagesBytes, usage.imagesFiles, .teal),
                ("Attachments", usage.attachmentsBytes, usage.attachmentsFiles, .amber),
                ("Profile picture", usage.profilePicsBytes, usage.profilePicsFiles, .coral),
            ]
            let scale = Double(max(usage.limitBytes?.int64Value ?? usage.totalBytes, 1))
            VStack(alignment: .leading, spacing: 4) {
                Text(bytes(usage.totalBytes)).font(.title.weight(.bold)).foregroundStyle(Theme.ink)
                Text(summary(usage)).font(.subheadline).foregroundStyle(Theme.muted)
                GeometryReader { geo in
                    HStack(spacing: 3) {
                        ForEach(slices.filter { $0.1 > 0 }, id: \.0) { slice in
                            // A few kilobytes next to hundreds of megabytes would round to nothing.
                            Capsule().fill(slice.3.ink)
                                .frame(width: max(geo.size.width * Double(slice.1) / scale, geo.size.width * 0.02))
                        }
                        Spacer(minLength: 0)
                    }
                }
                .frame(height: 10)
                .background(Capsule().fill(Theme.separator))
                .clipShape(Capsule())
                .padding(.vertical, 10)
                .accessibilityHidden(true)
                ForEach(slices, id: \.0) { name, size, files, tone in
                    HStack(spacing: 8) {
                        Circle().fill(tone.ink).frame(width: 8, height: 8)
                        Text(name).font(.subheadline).foregroundStyle(Theme.ink)
                        Spacer()
                        Text("\(bytes(size)) · \(files == 1 ? "1 file" : "\(files.formatted()) files")")
                            .font(.subheadline).foregroundStyle(Theme.muted).monospacedDigit()
                    }
                    .accessibilityElement(children: .combine)
                }
            }
            .padding(14)
        } else if state.isLoading {
            ProgressView().frame(maxWidth: .infinity, minHeight: 80)
        } else {
            HStack(spacing: 12) {
                ToneBadge(systemImage: "exclamationmark.triangle", tone: .coral)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Storage usage unavailable").foregroundStyle(Theme.ink)
                    if let error = state.error { Text(error).font(.footnote).foregroundStyle(Theme.muted) }
                }
                Spacer(minLength: 8)
                Button("Retry", action: retry).buttonStyle(.glass)
            }
            .padding(.horizontal, 14)
            .frame(minHeight: 60)
        }
    }

    /// "368,2 MB"; "0 KB" rather than the formatter's "Zero KB".
    private func bytes(_ n: Int64) -> String {
        let f = ByteCountFormatter()
        f.countStyle = .file
        f.allowsNonnumericFormatting = false
        return f.string(fromByteCount: n)
    }

    private func summary(_ usage: MediaUsage) -> String {
        let used = usage.limitBytes.map { "of \(bytes($0.int64Value)) used" } ?? "used, no limit set"
        let files = usage.totalFiles == 1 ? "1 file" : "\(usage.totalFiles.formatted()) files"
        return "\(used) · \(files)"
    }
}

/// A row whose value opens a menu of the choices.
private struct MenuRow<Items: View>: View {
    let systemImage: String
    let tone: Theme.Tone
    let title: String
    let value: String
    @ViewBuilder let items: Items

    var body: some View {
        Menu { items } label: {
            HStack(spacing: 12) {
                ToneBadge(systemImage: systemImage, tone: tone)
                Text(title).foregroundStyle(Theme.ink)
                Spacer(minLength: 8)
                Text(value).foregroundStyle(Theme.muted).lineLimit(1)
                Image(systemName: "chevron.up.chevron.down").font(.caption.weight(.semibold)).foregroundStyle(Theme.glyph)
            }
            .padding(.horizontal, 14)
            .frame(minHeight: 52)
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(title)
        .accessibilityValue(value)
    }
}

private extension ProfileForm {
    /// A copy with one setting changed; Swift can't use the Kotlin copy's default arguments.
    func with(publicProfile: Bool? = nil, imperialUnits: Bool? = nil, currency: String? = nil, mapStyle: String? = nil) -> ProfileForm {
        doCopy(
            username: username, firstName: firstName, lastName: lastName,
            publicProfile: publicProfile ?? self.publicProfile,
            imperialUnits: imperialUnits ?? self.imperialUnits,
            currency: currency ?? self.currency,
            mapStyle: mapStyle ?? self.mapStyle
        )
    }
}
