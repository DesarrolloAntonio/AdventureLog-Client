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
    /// Managing categories, as Android's sheet does: a new one or one being changed (nil id for
    /// new), the one waiting for its Delete to be confirmed, and what the server refused.
    @State private var editingCategory: (id: String?, name: String, icon: String)?
    @State private var deletingCategory: ComposeApp.Category?
    @State private var categoryError: String?

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
                        Text("Country").tag(LocationSortField.country)
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()

                    Picker("Order", selection: $ascending) {
                        Text(directionLabels.descending).tag(false)
                        Text(directionLabels.ascending).tag(true)
                    }
                    .pickerStyle(.segmented)
                }

                Section {
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
                            .swipeActions {
                                Button("Delete", role: .destructive) { deletingCategory = category }.tint(.red)
                                Button("Edit") { editingCategory = (category.id, category.displayName, category.icon) }
                                    .tint(Theme.accent)
                            }
                        }
                        Button { editingCategory = (nil, "", "") } label: {
                            Label("New Category", systemImage: "plus")
                        }
                        .accessibilityIdentifier("newCategory")
                    }
                } header: {
                    Text("Categories")
                } footer: {
                    Text("Swipe a category to change or delete it.")
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
        .task {
            for await op in vm.categoryOperationState {
                if case .error(let e) = onEnum(of: op) { categoryError = e.message; vm.clearCategoryOperationState() }
            }
        }
        .alert(editingCategory?.id == nil ? "New Category" : "Edit Category",
               isPresented: Binding(get: { editingCategory != nil }, set: { if !$0 { editingCategory = nil } })) {
            TextField("Name", text: Binding(get: { editingCategory?.name ?? "" }, set: { editingCategory?.name = $0 }))
            TextField("Emoji", text: Binding(get: { editingCategory?.icon ?? "" }, set: { editingCategory?.icon = $0 }))
            Button("Cancel", role: .cancel) {}
            Button(editingCategory?.id == nil ? "Create" : "Save") { saveCategory() }
        }
        .confirmationDialog("Delete \(deletingCategory?.displayName ?? "")?",
                            isPresented: Binding(get: { deletingCategory != nil }, set: { if !$0 { deletingCategory = nil } }),
                            titleVisibility: .visible) {
            Button("Delete Category", role: .destructive) {
                guard let category = deletingCategory else { return }
                categoryNames.remove(category.name)
                vm.deleteCategory(categoryId: category.id)
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            let places = Int(deletingCategory?.numAdventures ?? "") ?? 0
            Text(places == 0 ? "No place uses it. This can't be undone."
                 : "\(places) \(places == 1 ? "place uses" : "places use") it. This can't be undone.")
        }
        .alert(categoryError ?? "", isPresented: Binding(get: { categoryError != nil }, set: { if !$0 { categoryError = nil } })) {
            Button("OK", role: .cancel) {}
        }
    }

    private func saveCategory() {
        guard let edit = editingCategory else { return }
        let name = edit.name.trimmingCharacters(in: .whitespaces)
        guard !name.isEmpty else { return }
        let typed = edit.icon.trimmingCharacters(in: .whitespaces)
        let icon = typed.isEmpty ? "📍" : String(typed.prefix(2))
        if let id = edit.id {
            vm.updateCategory(categoryId: id, name: name, icon: icon)
        } else {
            vm.createCategory(name: name, icon: icon)
        }
    }

    private func toggle(_ name: String) {
        if categoryNames.contains(name) { categoryNames.remove(name) } else { categoryNames.insert(name) }
    }

    /// Words for the order that fit the field: A–Z for names, newest first for dates.
    private var directionLabels: (ascending: String, descending: String) {
        switch sortField {
        case .name, .country: ("A–Z", "Z–A")
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
