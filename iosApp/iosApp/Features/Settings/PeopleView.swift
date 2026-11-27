import SwiftUI
import ComposeApp

/// People, as the Mist-Users artboard draws it: the travellers on this server with a public
/// profile, in one frosted card. The list call doesn't carry their numbers, so a row opens the
/// person's places and countries in a sheet rather than showing them inline as the canvas does.
struct PeopleView: View {
    /// The signed-in user's username, marked "You".
    let me: String?

    @StateObject private var host = ViewModelHost()
    @State private var state = UsersUiState(isLoading: true, error: nil, users: [], query: "", profile: nil)
    @State private var query = ""

    private var vm: UsersViewModel { host.owner.usersViewModel() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("Travellers with a public profile on this server")
                    .font(.subheadline).foregroundStyle(Theme.muted).padding(.horizontal, 4)
                SearchField(text: $query, prompt: "Search people").padding(.top, 14)

                if state.users.isEmpty, state.isLoading {
                    ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 80)
                } else if state.users.isEmpty, let error = state.error {
                    ContentUnavailableView {
                        Label("Couldn't load people", systemImage: "wifi.exclamationmark")
                    } description: {
                        Text(error)
                    } actions: {
                        Button("Try again") { vm.load() }.buttonStyle(.glassProminent)
                    }
                    .padding(.top, 40)
                } else if state.filtered.isEmpty {
                    ContentUnavailableView("Nobody here yet", systemImage: "person.2",
                                           description: Text("People who make their profile public show up here."))
                        .padding(.top, 40)
                } else {
                    SectionLabel(text: "On this server", trailing: "\(state.filtered.count)").padding(.top, 20).padding(.bottom, 8)
                    VStack(spacing: 0) {
                        ForEach(Array(state.filtered.enumerated()), id: \.element.uuid) { index, person in
                            Button { vm.openProfile(person: person) } label: {
                                PersonRow(person: person, isMe: person.username == me, last: index == state.filtered.count - 1)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .mistCard()
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 120)
        }
        .mistBackground()
        .navigationTitle("People")
        .navigationBarTitleDisplayMode(.large)
        .onChange(of: query) { _, v in vm.onQueryChange(value: v) }
        .sheet(isPresented: Binding(get: { state.profile != nil }, set: { if !$0 { vm.closeProfile() } })) {
            if let profile = state.profile { ProfileSheet(profile: profile, retry: { vm.retryProfile() }) }
        }
        .task { for await s in vm.uiState { state = s } }
    }
}

private struct PersonRow: View {
    let person: PublicUser
    let isMe: Bool
    let last: Bool

    var body: some View {
        HStack(spacing: 12) {
            Avatar(person: person, size: 40)
            HStack {
                VStack(alignment: .leading, spacing: 1) {
                    HStack(spacing: 6) {
                        Text(person.displayName).font(.body.weight(.semibold)).foregroundStyle(Theme.ink)
                        if isMe { ToneTag(text: "You", tone: .teal) }
                    }
                    Text("@\(person.username)").font(.footnote).foregroundStyle(Theme.muted)
                }
                Spacer(minLength: 8)
                Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
            }
            .padding(.vertical, 10)
            .frame(minHeight: 60)
            .overlay(alignment: .bottom) {
                if !last { Rectangle().fill(Theme.separator).frame(height: 0.5) }
            }
        }
        .padding(.horizontal, 14)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

/// A profile picture, or initials on a soft colour.
private struct Avatar: View {
    let person: PublicUser
    let size: CGFloat

    var body: some View {
        RemoteImage(url: person.profilePic) {
            Text(initials)
                .font(.system(size: size * 0.36, weight: .bold))
                .foregroundStyle(tone.ink)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(tone.fill)
        }
        .frame(width: size, height: size)
        .clipShape(.circle)
        .accessibilityHidden(true)
    }

    private var initials: String {
        let letters = [person.firstName.first, person.lastName.first].compactMap { $0 }.map(String.init).joined()
        return letters.isEmpty ? String(person.username.prefix(2)).uppercased() : letters.uppercased()
    }

    private var tone: Theme.Tone { categoryTone(person.username) }
}

/// One person's numbers, as the web's profile opens with.
private struct ProfileSheet: View {
    let profile: ProfileState
    let retry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Avatar(person: profile.person, size: 72).padding(.top, 24)
            VStack(spacing: 2) {
                Text(profile.person.displayName).font(.title2.weight(.heavy)).foregroundStyle(Theme.ink)
                Text("@\(profile.person.username)").foregroundStyle(Theme.muted)
            }
            if profile.isLoading {
                ProgressView()
            } else if let error = profile.error {
                VStack(spacing: 8) {
                    Text(error).font(.footnote).foregroundStyle(Theme.muted).multilineTextAlignment(.center)
                    Button("Try again", action: retry).buttonStyle(.glass)
                }
            } else if let stats = profile.stats {
                HStack(spacing: 10) {
                    stat("\(stats.locationCount)", "places")
                    stat("\(stats.visitedCountryCount)", "countries")
                    stat("\(stats.tripsCount)", "collections")
                }
            }
            Spacer()
        }
        .padding(.horizontal, 16)
        .frame(maxWidth: .infinity)
        .mistBackground()
        .presentationDetents([.medium])
    }

    private func stat(_ value: String, _ label: String) -> some View {
        VStack(spacing: 2) {
            Text(value).font(.title.weight(.heavy)).foregroundStyle(Theme.ink).monospacedDigit()
            Text(label).font(.footnote).foregroundStyle(Theme.muted)
        }
        .frame(maxWidth: .infinity, minHeight: 72)
        .mistCard(radius: 18)
        .accessibilityElement(children: .combine)
    }
}
