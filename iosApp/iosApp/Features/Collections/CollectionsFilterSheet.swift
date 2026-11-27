import SwiftUI
import ComposeApp

/// How Collections is sorted and which trips it shows, as Android's sort sheet has it. Choices
/// apply on Done, so the list behind doesn't reload on every tap.
struct CollectionsFilterSheet: View {
    let vm: CollectionsViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var sortField: CollectionSortField
    @State private var ascending: Bool
    @State private var status: TripStatus?

    init(vm: CollectionsViewModel, sort: CollectionSortOptions, status: TripStatus?) {
        self.vm = vm
        _sortField = State(initialValue: sort.sortField)
        _ascending = State(initialValue: sort.sortDirection == .ascending)
        _status = State(initialValue: status)
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Show") {
                    Picker("Show", selection: $status) {
                        Text("Every collection").tag(TripStatus?.none)
                        Text("On now").tag(TripStatus?.some(.inProgress))
                        Text("Planned").tag(TripStatus?.some(.upcoming))
                        Text("Past").tag(TripStatus?.some(.completed))
                        Text("Folders").tag(TripStatus?.some(.folder))
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()
                }

                Section {
                    Picker("Sort by", selection: $sortField) {
                        Text("Where they stand").tag(CollectionSortField.updatedAt)
                        Text("Name").tag(CollectionSortField.name)
                        Text("Start date").tag(CollectionSortField.startDate)
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()

                    if sortField != .updatedAt {
                        Picker("Order", selection: $ascending) {
                            Text(sortField == .name ? "Z–A" : "Newest first").tag(false)
                            Text(sortField == .name ? "A–Z" : "Oldest first").tag(true)
                        }
                        .pickerStyle(.segmented)
                    }
                } header: {
                    Text("Sort by")
                } footer: {
                    Text(sortField == .updatedAt
                         ? "On now first, then planned, past and folders, each in date order."
                         : "One list in this order, without the groups.")
                }
            }
            .navigationTitle("Filters")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Reset") {
                        sortField = .updatedAt
                        ascending = false
                        status = nil
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { apply(); dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func apply() {
        // "Where they stand" is the server's default order (last updated, newest first): the
        // groups then put the trips in date order themselves.
        let direction: SortDirection = sortField == .updatedAt ? .descending : (ascending ? .ascending : .descending)
        vm.onSortOptionsChanged(options: CollectionSortOptions(sortField: sortField, sortDirection: direction))
        vm.onStatusFilterChanged(status: status)
    }
}
