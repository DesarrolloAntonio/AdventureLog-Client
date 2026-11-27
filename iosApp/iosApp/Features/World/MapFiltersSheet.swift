import SwiftUI
import ComposeApp

/// What the World map draws, as Android's layers sheet has it: visited and planned places, the
/// visited regions and cities as layers of their own, and the categories. Each switch applies on
/// the spot (the map behind shows it) and is remembered by the shared code.
struct MapFiltersSheet: View {
    let vm: MapViewModel
    @State var state: MapUiState
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section("Places") {
                    toggle("Visited", count: state.filters.visitedCount, on: state.filters.showVisited) { vm.toggleVisitedFilter() }
                    toggle("Planned", count: state.filters.plannedCount, on: state.filters.showPlanned) { vm.togglePlannedFilter() }
                }

                Section {
                    toggle("Visited regions", count: Int32(state.visitedRegions.count), on: state.filters.showRegions) { vm.toggleShowRegions() }
                    toggle("Visited cities", count: Int32(state.visitedCities.count), on: state.filters.showCities) { vm.toggleShowCities() }
                } header: {
                    Text("Layers")
                } footer: {
                    Text("A dot for each region or city your places fall in.")
                }

                if !state.categoryCounts.isEmpty {
                    Section {
                        ForEach(state.categoryCounts, id: \.self) { pair in
                            let name = String(pair.first ?? "")
                            Button { vm.toggleCategory(category: name) } label: {
                                HStack {
                                    Text(name).foregroundStyle(.primary)
                                    Spacer()
                                    Text("\(pair.second?.intValue ?? 0)").foregroundStyle(.secondary).monospacedDigit()
                                    Image(systemName: "checkmark")
                                        .foregroundStyle(Theme.accent)
                                        .opacity(state.filters.selectedCategories.contains(name) ? 1 : 0)
                                }
                            }
                            .accessibilityAddTraits(state.filters.selectedCategories.contains(name) ? .isSelected : [])
                        }
                    } header: {
                        Text("Categories")
                    } footer: {
                        Text("None ticked shows every category.")
                    }
                }
            }
            .navigationTitle("Map")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Reset") { vm.clearFilters() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
        .task { for await s in vm.uiState { state = s } }
    }

    private func toggle(_ title: String, count: Int32, on: Bool, action: @escaping () -> Void) -> some View {
        Toggle(isOn: Binding(get: { on }, set: { _ in action() })) {
            HStack {
                Text(title)
                Spacer()
                Text("\(count)").foregroundStyle(.secondary).monospacedDigit()
            }
        }
        .tint(Theme.accent)
    }
}
