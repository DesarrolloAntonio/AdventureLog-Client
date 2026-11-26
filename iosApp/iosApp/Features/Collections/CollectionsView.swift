import SwiftUI
import ComposeApp

/// Collections, as the Mist-Collections artboard draws it: Mine / Shared / Archived / Invites, the
/// next trip as the teal card, then the rest grouped by where they stand (on now, planned, past,
/// folders), each a frosted card with its cover, dates, size and status.
struct CollectionsView: View {
    @StateObject private var host = ViewModelHost()
    @State private var tab: CollectionsTab = .mine
    @State private var mine: [UltraSlimCollection] = []
    @State private var status = PagingStatus(isRefreshing: true, isLoadingMore: false, endReached: false, error: nil)
    @State private var content = CollectionsTabContent(collections: [], invites: [], isLoading: false, error: nil)
    @State private var invites: [CollectionInvite] = []
    @State private var count: Int32 = 0
    @State private var message: String?
    /// What the server is rendering (a share image, a PDF, a ZIP) while the phone waits for it.
    @State private var busy: String?
    /// The collection whose "Share with People" sheet is open, with who it is shared with.
    @State private var sharing: SharingTarget?
    @State private var deleting: UltraSlimCollection?
    /// Who is signed in: a collection shared with you offers none of its owner's actions.
    @State private var userId: String?
    @State private var creating = false
    @State private var editingId: String?

    private var vm: CollectionsViewModel { host.owner.collectionsViewModel() }
    private var presenter: PagingListPresenter<UltraSlimCollection> { host.owner.collectionsPresenter() }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Picker("Show", selection: Binding(get: { tab }, set: { vm.onTabSelected(tab: $0) })) {
                    Text("Mine").tag(CollectionsTab.mine)
                    Text("Shared").tag(CollectionsTab.shared)
                    Text("Archived").tag(CollectionsTab.archived)
                    Text(invites.isEmpty ? "Invites" : "Invites · \(invites.count)").tag(CollectionsTab.invites)
                }
                .pickerStyle(.segmented)
                .padding(.bottom, 16)

                switch tab {
                case .mine: mineList
                case .invites: invitesList
                default: plainList(content.collections, loading: content.isLoading, error: content.error)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 120)
        }
        .mistBackground()
        .navigationTitle("Collections")
        .navigationSubtitle(count == 1 ? "1 collection" : "\(count) collections")
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { creating = true } label: { Image(systemName: "plus") }
                    .accessibilityLabel("New collection")
            }
        }
        .navigationDestination(for: UltraSlimCollection.self) { TripView(collectionId: $0.id, name: $0.name, subtitle: dates($0)) }
        .sheet(isPresented: $creating) { CollectionFormView { presenter.refresh() } }
        .sheet(isPresented: Binding(get: { editingId != nil }, set: { if !$0 { editingId = nil } })) {
            CollectionFormView(collectionId: editingId) { presenter.refresh() }
        }
        .refreshable {
            if tab == .mine { presenter.refresh() } else { vm.refresh() }
        }
        .alert(message ?? "", isPresented: Binding(get: { message != nil }, set: { if !$0 { vm.clearActionMessage() } })) {
            Button("OK", role: .cancel) {}
        }
        .confirmationDialog(
            "Delete \(deleting?.name ?? "")?",
            isPresented: Binding(get: { deleting != nil }, set: { if !$0 { deleting = nil } }),
            titleVisibility: .visible,
            presenting: deleting
        ) { collection in
            Button("Delete", role: .destructive) { vm.deleteCollection(collectionId: collection.id) }
        } message: { _ in
            Text("Its places stay in Places. Its notes, lists, stays and journeys go with it.")
        }
        .task { for await t in vm.tab { tab = t } }
        .task { for await c in presenter.items { mine = c } }
        .task { for await s in presenter.status { status = s } }
        .task { for await c in vm.tabContent { content = c } }
        .task { for await i in vm.pendingInvites { invites = i } }
        .task { for await user in host.owner.sessionFlow() { userId = user?.uuid } }
        .task { for await n in vm.collectionCount { count = n.int32Value } }
        .task { for await m in vm.actionMessage { message = m } }
        .task { for await b in vm.busyLabel { busy = b } }
        .task { for await t in vm.sharing { sharing = t } }
        .sheet(isPresented: Binding(get: { sharing != nil }, set: { if !$0 { vm.closeSharing() } })) {
            if let sharing { ShareCollectionView(vm: vm, target: sharing) }
        }
        .overlay(alignment: .bottom) {
            if let busy {
                Label { Text(busy) } icon: { ProgressView() }
                    .font(.subheadline.weight(.medium))
                    .padding(.horizontal, 18).padding(.vertical, 12)
                    .glassEffect(.regular, in: .capsule)
                    .padding(.bottom, 24)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.snappy, value: busy)
        .task {
            for await d in vm.deleteState {
                if case .success = onEnum(of: d) {
                    presenter.refresh()
                    vm.clearDeleteState()
                }
            }
        }
    }

    // MARK: Mine

    @ViewBuilder
    private var mineList: some View {
        if mine.isEmpty, status.isRefreshing {
            ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 80)
        } else if mine.isEmpty, let error = status.error {
            failed(error) { presenter.retry() }
        } else if mine.isEmpty {
            ContentUnavailableView("No collections yet", systemImage: "rectangle.stack",
                                   description: Text("Group places into a trip, or into a folder of your own."))
        } else {
            let next = upNext
            VStack(alignment: .leading, spacing: 0) {
                if let next {
                    NavigationLink(value: next) { UpNextCard(collection: next) }
                        .buttonStyle(.plain)
                        .contextMenu { menu(next) }
                }
                let groups = groups(excluding: next?.id)
                ForEach(groups, id: \.title) { group in
                    // Under the picker the first label needs no gap of its own: 16 + 20 stood out.
                    SectionLabel(text: group.title)
                        .padding(.top, next == nil && group.title == groups.first?.title ? 4 : 20)
                        .padding(.bottom, 8)
                    VStack(spacing: 10) {
                        ForEach(group.items, id: \.id) { collection in
                            CollectionCard(collection: collection, menu: { menu(collection) })
                                .onAppear {
                                    if let i = mine.firstIndex(where: { $0.id == collection.id }) {
                                        presenter.itemAccessed(index: Int32(i))
                                    }
                                }
                        }
                    }
                }
                if status.isLoadingMore {
                    ProgressView().frame(maxWidth: .infinity, minHeight: 56)
                }
            }
        }
    }

    /// The trip on now, or else the soonest one ahead.
    private var upNext: UltraSlimCollection? {
        mine.first { $0.status == .inProgress }
            ?? mine.filter { $0.status == .upcoming }.min { ($0.daysUntilStart?.intValue ?? .max) < ($1.daysUntilStart?.intValue ?? .max) }
    }

    private struct Group { let title: String; let items: [UltraSlimCollection] }

    private func groups(excluding id: String?) -> [Group] {
        let rest = mine.filter { $0.id != id }
        let order: [(TripStatus, String)] = [(.inProgress, "On now"), (.upcoming, "Planned"), (.completed, "Past"), (.folder, "Folders")]
        return order.compactMap { status, title in
            // In date order, as the headings promise: the server sends them by last update, which
            // put a 2025 trip between two from 2026.
            let items = rest.filter { $0.status == status }.sorted { a, b in
                switch status {
                case .completed: (a.endDate ?? a.startDate ?? "") > (b.endDate ?? b.startDate ?? "")
                case .upcoming, .inProgress: (a.startDate ?? "~") < (b.startDate ?? "~")
                default: a.name.localizedCaseInsensitiveCompare(b.name) == .orderedAscending
                }
            }
            return items.isEmpty ? nil : Group(title: title, items: items)
        }
    }

    // MARK: Shared and archived

    @ViewBuilder
    private func plainList(_ collections: [UltraSlimCollection], loading: Bool, error: String?) -> some View {
        if collections.isEmpty, loading {
            ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 80)
        } else if collections.isEmpty, let error {
            failed(error) { vm.refresh() }
        } else if collections.isEmpty {
            ContentUnavailableView(
                tab == .shared ? "Nothing shared with you" : "Nothing archived",
                systemImage: tab == .shared ? "person.2" : "archivebox",
                description: Text(tab == .shared ? "Collections other people share with you show up here." : "Collections you archive wait here.")
            )
        } else {
            VStack(spacing: 10) {
                ForEach(collections, id: \.id) { collection in
                    CollectionCard(collection: collection, menu: { menu(collection) })
                }
            }
        }
    }

    // MARK: Invites

    @ViewBuilder
    private var invitesList: some View {
        if content.invites.isEmpty, content.isLoading {
            ProgressView().controlSize(.large).frame(maxWidth: .infinity).padding(.top, 80)
        } else if content.invites.isEmpty {
            ContentUnavailableView("No invitations", systemImage: "envelope.open",
                                   description: Text("When someone shares a collection with you, it waits here."))
        } else {
            VStack(spacing: 10) {
                ForEach(content.invites, id: \.id) { invite in
                    VStack(alignment: .leading, spacing: 10) {
                        HStack(spacing: 12) {
                            ToneBadge(systemImage: "envelope.fill", tone: .blue, size: 38)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(invite.collectionName).font(.body.weight(.semibold)).foregroundStyle(Theme.ink)
                                Text("From \(invite.ownerDisplayName)").font(.subheadline).foregroundStyle(Theme.muted)
                            }
                        }
                        HStack(spacing: 10) {
                            Button("Decline") { vm.respondToInvite(invite: invite, accept: false) }
                                .buttonStyle(.glass)
                            Button("Accept") { vm.respondToInvite(invite: invite, accept: true) }
                                .buttonStyle(.glassProminent)
                        }
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    .padding(14)
                    .mistCard()
                }
            }
        }
    }

    // MARK: Shared pieces

    @ViewBuilder
    private func menu(_ collection: UltraSlimCollection) -> some View {
        let owned = OwnershipKt.ownedBy(ownerId: collection.ownerId, currentUserId: userId)
        // The server renders all three; the share image and the PDF are anyone's, the ZIP the owner's.
        Section {
            Button("Share Image", systemImage: "square.and.arrow.up") { vm.exportCollection(collection: collection, what: .shareCard) }
            Button("Download PDF", systemImage: "doc.richtext") { vm.exportCollection(collection: collection, what: .pdf) }
            if owned {
                Button("Export ZIP", systemImage: "doc.zipper") { vm.exportCollection(collection: collection, what: .zip) }
            }
        }
        .disabled(busy != nil)
        if owned {
            ownerMenu(collection)
        } else {
            // The server refuses these to anyone but the owner, as Android hides them.
            Text("Shared with you. Only its owner can change or delete it.")
        }
    }

    @ViewBuilder
    private func ownerMenu(_ collection: UltraSlimCollection) -> some View {
        Button("Share with People", systemImage: "person.badge.plus") { vm.openSharing(collection: collection) }
        Button("Edit", systemImage: "pencil") { editingId = collection.id }
        Button("Duplicate", systemImage: "plus.square.on.square") { vm.duplicateCollection(collection: collection) }
        if collection.isArchived {
            Button("Restore", systemImage: "tray.and.arrow.up") { vm.setArchived(collection: collection, archived: false) }
        } else {
            Button("Archive", systemImage: "archivebox") { vm.setArchived(collection: collection, archived: true) }
        }
        Button("Delete", systemImage: "trash", role: .destructive) { deleting = collection }
    }

    private func failed(_ error: String, retry: @escaping () -> Void) -> some View {
        ContentUnavailableView {
            Label("Couldn't load collections", systemImage: "wifi.exclamationmark")
        } description: {
            Text(error)
        } actions: {
            Button("Try again", action: retry).buttonStyle(.glassProminent)
        }
    }
}

/// "5–9 Sep · 2 places": the dates when there are any, then the size.
func dates(_ collection: UltraSlimCollection) -> String {
    let places = collection.adventureCount == 1 ? "1 place" : "\(collection.adventureCount) places"
    guard let range = DayRange(start: collection.startDate, end: collection.endDate) else { return places }
    return "\(range.shortText) · \(places)"
}

/// The next trip in teal: when, what it is, how big, and its cover on the right.
private struct UpNextCard: View {
    let collection: UltraSlimCollection

    var body: some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 6) {
                Label(eyebrow, systemImage: "calendar")
                    .font(.caption.weight(.bold))
                    .tracking(0.9)
                    .foregroundStyle(.white.opacity(0.9))
                    .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
                Spacer(minLength: 8)
                Text(collection.name).font(.title2.weight(.heavy))
                Text(dates(collection)).font(.subheadline.weight(.medium)).foregroundStyle(.white.opacity(0.92))
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            if let cover = collection.featuredImage {
                RemoteImage(url: cover) { Color.white.opacity(0.15) }
                    .frame(width: 94, height: 116)
                    .clipShape(.rect(cornerRadius: 16, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).strokeBorder(.white.opacity(0.5)))
                    .accessibilityHidden(true)
            }
        }
        .padding(18)
        .frame(minHeight: 146)
        .background { HeroGlow().clipShape(.rect(cornerRadius: Theme.Radius.hero)) }
        .heroCard()
        .accessibilityElement(children: .combine)
    }

    private var eyebrow: String {
        if collection.status == .inProgress { return "ON NOW" }
        guard let days = collection.daysUntilStart?.intValue else { return "UP NEXT" }
        switch days {
        case ...0: return "UP NEXT · TODAY"
        case 1: return "UP NEXT · TOMORROW"
        default: return "UP NEXT · IN \(days) DAYS"
        }
    }
}

/// One collection: cover, name, dates and size, its status tag, a chevron; long-press for more.
private struct CollectionCard<Menu: View>: View {
    let collection: UltraSlimCollection
    @ViewBuilder let menu: () -> Menu
    @Environment(\.dynamicTypeSize) private var typeSize

    var body: some View {
        NavigationLink(value: collection) {
            // At the accessibility sizes the cover goes above, so the text gets the whole width.
            (typeSize.isAccessibilitySize ? AnyLayout(VStackLayout(alignment: .leading, spacing: 10)) : AnyLayout(HStackLayout(spacing: 14))) {
                RemoteImage(url: collection.featuredImage) {
                    Rectangle().fill(Theme.Tone.teal.fill)
                        .overlay(Image(systemName: "rectangle.stack").foregroundStyle(Theme.Tone.teal.ink))
                }
                .frame(width: 64, height: 64)
                .clipShape(.rect(cornerRadius: 14, style: .continuous))

                VStack(alignment: .leading, spacing: 3) {
                    Text(collection.name).font(.body.weight(.semibold)).foregroundStyle(Theme.ink)
                    Text(dates(collection)).font(.subheadline).foregroundStyle(Theme.muted)
                    statusTag.padding(.top, 2)
                }
                if !typeSize.isAccessibilitySize {
                    Spacer(minLength: 8)
                    Image(systemName: "chevron.right").font(.footnote.weight(.bold)).foregroundStyle(Theme.glyph)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
            .mistCard()
            .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .contextMenu { menu() }
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private var statusTag: some View {
        switch collection.status {
        case .upcoming: ToneTag(text: "Planned", tone: .coral)
        case .inProgress: ToneTag(text: "On now", tone: .teal)
        case .completed: ToneTag(text: "Completed", tone: .green, systemImage: "checkmark")
        default: ToneTag(text: "Folder", tone: .blue)
        }
    }
}
