import SwiftUI
import ComposeApp

/// Which collections a place is in, as Android's Manage collections sheet has it: the ones it is in
/// first, then the rest; the search narrows only the rest, so a ticked one never vanishes while
/// typing. Saving waits for the server, which the caller reports through `saving` and `error`.
struct CollectionsPickerView: View {
    let place: Location
    let collections: [UltraSlimCollection]
    let loading: Bool
    let saving: Bool
    let error: String?
    let clearError: () -> Void
    let refresh: () -> Void
    let save: ([String]) -> Void

    @State private var chosen: Set<String>
    @State private var query = ""

    init(place: Location, collections: [UltraSlimCollection], loading: Bool, saving: Bool, error: String?,
         clearError: @escaping () -> Void, refresh: @escaping () -> Void, save: @escaping ([String]) -> Void) {
        self.place = place
        self.collections = collections
        self.loading = loading
        self.saving = saving
        self.error = error
        self.clearError = clearError
        self.refresh = refresh
        self.save = save
        _chosen = State(initialValue: Set(place.collections))
    }

    private var inIt: [UltraSlimCollection] {
        collections.filter { chosen.contains($0.id) }.sorted { $0.name.lowercased() < $1.name.lowercased() }
    }

    private var rest: [UltraSlimCollection] {
        let q = query.trimmingCharacters(in: .whitespaces)
        return collections
            .filter { !chosen.contains($0.id) }
            .filter { q.isEmpty || $0.name.localizedCaseInsensitiveContains(q) || $0.description_.localizedCaseInsensitiveContains(q) }
            .sorted { $0.name.lowercased() < $1.name.lowercased() }
    }

    var body: some View {
        FormScaffold(
            title: "Collections",
            subtitle: place.name,
            canSave: chosen != Set(place.collections),
            saving: saving,
            dirty: chosen != Set(place.collections),
            error: error,
            clearError: clearError,
            save: { save(Array(chosen)) }
        ) {
            if collections.isEmpty {
                VStack(spacing: 12) {
                    if loading {
                        ProgressView()
                    } else {
                        Text("No collections yet").font(.headline).foregroundStyle(Theme.ink)
                        Text("Make one in Collections, then come back to add this place.")
                            .font(.subheadline).foregroundStyle(Theme.muted).multilineTextAlignment(.center)
                        Button("Load Again", action: refresh).buttonStyle(.glass)
                    }
                }
                .frame(maxWidth: .infinity, minHeight: 200)
            } else {
                if !inIt.isEmpty {
                    SectionLabel(text: "In", trailing: "\(inIt.count)").padding(.bottom, -8)
                    rows(inIt)
                }
                SectionLabel(text: inIt.isEmpty ? "Collections" : "Others", trailing: "\(rest.count)").padding(.bottom, -8)
                SearchField(text: $query, prompt: "Search collections")
                if rest.isEmpty {
                    Text(query.isEmpty ? "It's in every collection." : "No collection matches “\(query)”.")
                        .font(.subheadline).foregroundStyle(Theme.muted)
                        .frame(maxWidth: .infinity, minHeight: 60)
                } else {
                    rows(rest)
                }
            }
        }
        .task { if collections.isEmpty { refresh() } }
    }

    private func rows(_ list: [UltraSlimCollection]) -> some View {
        FormCard {
            ForEach(Array(list.enumerated()), id: \.element.id) { index, c in
                if index > 0 { FormDivider() }
                Button { toggle(c.id) } label: {
                    HStack(spacing: 12) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(c.name).foregroundStyle(Theme.ink)
                            Text(c.adventureCount == 1 ? "1 place" : "\(c.adventureCount) places")
                                .font(.footnote).foregroundStyle(Theme.muted)
                        }
                        Spacer()
                        Image(systemName: chosen.contains(c.id) ? "checkmark.circle.fill" : "circle")
                            .font(.title3)
                            .foregroundStyle(chosen.contains(c.id) ? Theme.accent : Theme.glyph)
                    }
                    .padding(.horizontal, 16)
                    .frame(minHeight: 56)
                    .contentShape(.rect)
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(chosen.contains(c.id) ? .isSelected : [])
            }
        }
    }

    private func toggle(_ id: String) {
        if chosen.contains(id) { chosen.remove(id) } else { chosen.insert(id) }
    }
}

/// A place opens sheets by itself (`.sheet(item:)`); its server id is already unique.
extension Location: @retroactive Identifiable {}
