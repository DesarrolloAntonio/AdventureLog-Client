import SwiftUI
import ComposeApp

/// Sort, categories and whether places filed in collections count: what the panel's switch
/// doesn't cover. Choices apply on Done, so the list behind doesn't reload on every tap.
struct PlacesFilterSheet: View {
    let vm: LocationsViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var sortField: LocationSortField
    @State private var ascending: Bool
    @State private var categoryNames: Set<String>
    @State private var includeCollections: Bool
    @State private var categories: LocationsViewModel.CategoriesState = LocationsViewModel.CategoriesStateLoading.shared
    private let visitedFilter: VisitedFilter

    init(vm: LocationsViewModel, initial: LocationFilters) {
        self.vm = vm
        _sortField = State(initialValue: initial.sortField)
        _ascending = State(initialValue: initial.sortDirection == .ascending)
        _categoryNames = State(initialValue: Set(initial.categoryNames))
        _includeCollections = State(initialValue: initial.includeCollections)
        visitedFilter = initial.visitedFilter
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Sort by") {
                    Picker("Sort by", selection: $sortField) {
                        Text("Updated").tag(LocationSortField.updatedAt)
                        Text("Visit date").tag(LocationSortField.visitDate)
                        Text("Name").tag(LocationSortField.name)
                        Text("Rating").tag(LocationSortField.rating)
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()

                    Picker("Order", selection: $ascending) {
                        Text(directionLabels.descending).tag(false)
                        Text(directionLabels.ascending).tag(true)
                    }
                    .pickerStyle(.segmented)
                }

                Section("Categories") {
                    switch onEnum(of: categories) {
                    case .loading:
                        ProgressView().frame(maxWidth: .infinity)
                    case .error(let e):
                        VStack(alignment: .leading, spacing: 6) {
                            Text(e.message).foregroundStyle(.secondary)
                            Button("Try again") { vm.retryLoadCategories() }
                        }
                    case .success(let s):
                        if s.categories.isEmpty {
                            Text("No categories yet").foregroundStyle(.secondary)
                        }
                        ForEach(s.categories, id: \.id) { category in
                            Button { toggle(category.name) } label: {
                                HStack {
                                    Text(category.icon).frame(width: 28)
                                    Text(category.displayName).foregroundStyle(.primary)
                                    Spacer()
                                    if categoryNames.contains(category.name) {
                                        Image(systemName: "checkmark").fontWeight(.semibold).foregroundStyle(Theme.accent)
                                    }
                                }
                            }
                            .tint(.primary)
                            .accessibilityAddTraits(categoryNames.contains(category.name) ? .isSelected : [])
                        }
                    }
                }

                Section {
                    Toggle("Include places in collections", isOn: $includeCollections)
                } footer: {
                    Text("Off shows only the places that belong to no collection.")
                }
            }
            .navigationTitle("Filters")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Reset") {
                        sortField = .updatedAt
                        ascending = false
                        categoryNames = []
                        includeCollections = true
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { apply(); dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
        .task { for await c in vm.categoriesState { categories = c } }
    }

    private func toggle(_ name: String) {
        if categoryNames.contains(name) { categoryNames.remove(name) } else { categoryNames.insert(name) }
    }

    /// Words for the order that fit the field: A–Z for names, newest first for dates.
    private var directionLabels: (ascending: String, descending: String) {
        switch sortField {
        case .name: ("A–Z", "Z–A")
        case .rating: ("Lowest first", "Highest first")
        default: ("Oldest first", "Newest first")
        }
    }

    private func apply() {
        vm.onFiltersChanged(filters: LocationFilters(
            categoryNames: categoryNames.sorted(),
            sortField: sortField,
            sortDirection: ascending ? .ascending : .descending,
            visitedFilter: visitedFilter,
            includeCollections: includeCollections
        ))
    }
}
