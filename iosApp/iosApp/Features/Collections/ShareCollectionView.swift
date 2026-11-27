import SwiftUI
import ComposeApp

/// Who a collection is shared with, and who else it could be: the people with a public profile on
/// the server, as Android's share sheet lists them. Only a public profile can be invited, so an
/// empty list says why. "Invited just now" is only this sitting's: the server cannot list what is
/// still pending.
struct ShareCollectionView: View {
    let vm: CollectionsViewModel
    let target: SharingTarget

    @Environment(\.dismiss) private var dismiss

    private var state: ShareSheetState { target.state }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    if let message = state.message {
                        Text(message.text)
                            .font(.subheadline)
                            .foregroundStyle(message.isError ? Theme.Tone.coral.ink : Theme.ink)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(14)
                            .background((message.isError ? Theme.Tone.coral : Theme.Tone.mint).fill,
                                        in: .rect(cornerRadius: 16, style: .continuous))
                            .accessibilityAddTraits(.updatesFrequently)
                    }
                    content
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .padding(.bottom, 40)
            }
            .mistBackground()
            .navigationTitle("Share")
            .navigationSubtitle(target.collectionName)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button { dismiss() } label: { Image(systemName: "xmark") }
                        .accessibilityLabel("Close")
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    @ViewBuilder
    private var content: some View {
        if state.isLoading {
            ProgressView().frame(maxWidth: .infinity, minHeight: 160)
        } else if let error = state.error {
            ContentUnavailableView {
                Label("Couldn't load people", systemImage: "wifi.exclamationmark")
            } description: {
                Text(error)
            } actions: {
                Button("Try again") { vm.retrySharing() }.buttonStyle(.glassProminent)
            }
        } else if state.people.isEmpty {
            ContentUnavailableView(
                "No one to share with",
                systemImage: "person.2",
                description: Text("Nobody else on this server has a public profile yet. A profile becomes public in that person's own settings.")
            )
        } else {
            let shared = state.people.filter { state.sharedWith.contains($0.uuid) }
            let invited = state.people.filter { state.invitedThisVisit.contains($0.uuid) && !state.sharedWith.contains($0.uuid) }
            let rest = state.people.filter { !state.sharedWith.contains($0.uuid) && !state.invitedThisVisit.contains($0.uuid) }
            if !shared.isEmpty {
                group("Shared with", shared, action: "Remove", prominent: false) { vm.unshare(person: $0) }
            }
            if !invited.isEmpty {
                group("Invited just now", invited, action: "Revoke", prominent: false) { vm.revoke(person: $0) }
            }
            if !rest.isEmpty {
                group(shared.isEmpty && invited.isEmpty ? "People" : "Invite someone else", rest,
                      action: "Invite", prominent: true) { vm.invite(person: $0) }
            }
        }
    }

    private func group(_ title: String, _ people: [PublicUser], action: String, prominent: Bool,
                       perform: @escaping (PublicUser) -> Void) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionLabel(text: title, trailing: "\(people.count)")
            VStack(spacing: 0) {
                ForEach(Array(people.enumerated()), id: \.element.uuid) { index, person in
                    if index > 0 { Divider().padding(.leading, 70) }
                    row(person, action: action, prominent: prominent) { perform(person) }
                }
            }
            .mistCard()
        }
    }

    private func row(_ person: PublicUser, action: String, prominent: Bool, perform: @escaping () -> Void) -> some View {
        HStack(spacing: 14) {
            Text(person.displayName.trimmingCharacters(in: .whitespaces).first.map { String($0).uppercased() } ?? "?")
                .font(.headline)
                .foregroundStyle(Theme.Tone.mint.ink)
                .frame(width: 40, height: 40)
                .background(Theme.Tone.mint.fill, in: .circle)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(person.displayName).font(.body.weight(.medium)).foregroundStyle(Theme.ink).lineLimit(1)
                Text("@\(person.username)").font(.footnote).foregroundStyle(Theme.muted).lineLimit(1)
            }
            Spacer(minLength: 8)
            if state.busyUuid == person.uuid {
                ProgressView().frame(width: 72)
            } else if prominent {
                Button(action, action: perform).buttonStyle(.glassProminent).tint(Theme.accent)
                    .disabled(state.busyUuid != nil)
            } else {
                Button(action, action: perform).buttonStyle(.glass)
                    .disabled(state.busyUuid != nil)
            }
        }
        .padding(.horizontal, 14)
        .frame(minHeight: 64)
    }
}
