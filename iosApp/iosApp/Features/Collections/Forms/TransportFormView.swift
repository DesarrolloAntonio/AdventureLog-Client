import SwiftUI
import ComposeApp

/// New or edit journey, as the Mist-TransportForm artboard draws it: how you travel as chips, from
/// and to with a swap, when it leaves and arrives, and the details. From and To search places as
/// Android's do; a picked one puts the journey's end on the trip's map.
struct TransportFormView: View {
    let collectionId: String
    let collectionName: String
    var transportationId: String?
    let onSaved: () -> Void
    /// Deletes the one being edited; nil for a new one.
    var onDelete: (() -> Void)? = nil

    @StateObject private var host = ViewModelHost()
    @State private var loading = false
    @State private var saving = false
    @State private var error: String?
    @State private var base = IosForms.shared.transport(transportation: nil)
    @State private var initial: TransportationFormData?
    @State private var name = ""
    @State private var type = "car"
    @State private var from = ""
    @State private var to = ""
    @State private var departure = ""
    @State private var arrival = ""
    @State private var allDay = true
    @State private var keepToTrip = false
    @State private var flight = ""
    @State private var link = ""
    @State private var notes = ""
    @State private var isPublic = false
    @State private var seeded = false
    /// Which end is being searched for, the results, and the points picked for each end.
    @FocusState private var focus: End?
    @State private var searchingFor: End?
    @State private var results: [GeocodeSearchResult] = []
    @State private var searchBusy = false
    @State private var describing = false
    @State private var origin: (lat: String, lon: String)?
    @State private var destination: (lat: String, lon: String)?
    /// The text a pick put in a field, which is not a new search.
    @State private var picked: String?

    private enum End { case from, to }
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditTransportationViewModel {
        host.owner.addEditTransportationViewModel(transportationId: transportationId, collectionId: collectionId)
    }

    private var form: TransportationFormData {
        let journey = IosForms.shared.transport(base: base, name: name, type: type, description: notes, link: link, from: from, to: to,
                                                departure: departure, arrival: arrival, allDay: allDay, keepToTripDates: keepToTrip,
                                                flightNumber: flight, isPublic: isPublic)
        return IosForms.shared.transportEnds(base: journey, originLatitude: origin?.lat, originLongitude: origin?.lon,
                                             destinationLatitude: destination?.lat, destinationLongitude: destination?.lon)
    }

    private static let kinds: [(String, String)] = [
        ("car", "Car"), ("train", "Train"), ("plane", "Plane"), ("bus", "Bus"), ("boat", "Boat"),
        ("bike", "Bike"), ("walking", "Walking"), ("other", "Other"),
    ]

    var body: some View {
        FormScaffold(
            title: transportationId == nil ? "New Transport" : "Edit Transport", subtitle: collectionName,
            canSave: !name.trimmingCharacters(in: .whitespaces).isEmpty && seeded,
            saving: saving, dirty: seeded && form != (initial ?? base),
            error: error, clearError: { vm.clearError() }, save: { vm.saveTransportation(formData: form) },
            delete: onDelete.map { action in ("Delete Transport", action) }) {
            if !seeded {
                if let error, transportationId != nil, !loading {
                    ContentUnavailableView("Couldn't load the journey", systemImage: "wifi.exclamationmark", description: Text(error))
                    Button("Try again") { vm.retryLoad() }.buttonStyle(.glassProminent)
                } else {
                    ProgressView().frame(maxWidth: .infinity).padding(.top, 60)
                }
            } else {
                ChoiceChips(options: Self.kinds.map { ($0.0, $0.1) }, selection: $type)
                FormCard {
                    FormField(placeholder: "Name", text: $name, font: .title3.weight(.semibold))
                }
                FormCard {
                    HStack(spacing: 0) {
                        VStack(spacing: 0) {
                            endField("From", text: $from, end: .from, pinned: origin != nil)
                            FormDivider()
                            endField("To", text: $to, end: .to, pinned: destination != nil)
                        }
                        Button {
                            picked = nil
                            swap(&from, &to)
                            swap(&origin, &destination)
                        } label: {
                            Image(systemName: "arrow.up.arrow.down")
                                .font(.body.weight(.semibold))
                                .frame(width: 40, height: 40)
                                .background(Circle().fill(Theme.Tone.teal.fill))
                        }
                        .buttonStyle(.plain)
                        .foregroundStyle(Theme.accent)
                        .padding(.trailing, 12)
                        .accessibilityLabel("Swap from and to")
                    }
                    if searchingFor != nil, !results.isEmpty {
                        ForEach(Array(results.prefix(5).enumerated()), id: \.offset) { _, result in
                            FormDivider()
                            Button { pick(result) } label: {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(result.name).foregroundStyle(Theme.ink)
                                    Text(result.displayName).font(.footnote).foregroundStyle(Theme.muted).lineLimit(2)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.horizontal, 16).padding(.vertical, 10)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
                SectionLabel(text: "When").padding(.bottom, -8)
                FormCard {
                    ToggleRow(title: "All day", isOn: allDayBinding)
                    FormDivider()
                    DateRow(title: "Departs", value: $departure, withTime: !allDay)
                    FormDivider()
                    DateRow(title: "Arrives", value: $arrival, withTime: !allDay)
                    FormDivider()
                    ToggleRow(title: "Keep to trip dates", isOn: $keepToTrip)
                }
                SectionLabel(text: "Details").padding(.bottom, -8)
                FormCard {
                    if type == "plane" {
                        FormField(placeholder: "Flight number", text: $flight, capitalization: .characters)
                        FormDivider()
                    }
                    FormField(placeholder: "Link", text: $link, keyboard: .URL, capitalization: .never)
                    FormDivider()
                    FormField(placeholder: "Notes", text: $notes, axis: .vertical)
                    FormDivider()
                    // The server writes it from the Wikipedia article of the journey's name, as on Android.
                    Button { vm.generateDescription(name: name.trimmingCharacters(in: .whitespaces)) { notes = $0 } } label: {
                        HStack(spacing: 8) {
                            if describing { ProgressView() } else { Image(systemName: "book.pages") }
                            Text(notes.isEmpty ? "Write It from Wikipedia" : "Replace with Wikipedia")
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 16).frame(minHeight: 48)
                    }
                    .buttonStyle(.plain)
                    .foregroundStyle(Theme.accent)
                    .disabled(describing || name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
                FormCard { ToggleRow(title: "Make public", isOn: $isPublic) }
            }
        }
        .task {
            for await s in vm.uiState {
                loading = s.isLoading
                error = s.errorMessage
                results = s.locationSearchResults
                searchBusy = s.isSearchingLocation
                describing = s.isGeneratingDescription
                if s.isLoading, seeded { saving = true }
                if !s.isLoading { saving = false }
                if !seeded, !s.isLoading, transportationId == nil || s.existingTransportation != nil {
                    seed(IosForms.shared.transport(transportation: s.existingTransportation))
                }
                if s.isSaved { onSaved(); dismiss() }
            }
        }
    }

    /// A From or To field that searches as it's typed in; a pin shows when a place was picked.
    private func endField(_ placeholder: String, text: Binding<String>, end: End, pinned: Bool) -> some View {
        HStack(spacing: 8) {
            TextField(placeholder, text: Binding(get: { text.wrappedValue }, set: { typed in
                text.wrappedValue = typed
                guard focus == end, typed != picked else { return }
                // Typed by hand: the point picked before no longer matches the words.
                if end == .from { origin = nil } else { destination = nil }
                searchingFor = end
                vm.searchLocations(query: typed)
            }))
            .focused($focus, equals: end)
            .submitLabel(.search)
            if searchingFor == end, searchBusy { ProgressView() }
            else if pinned { Image(systemName: "mappin.circle.fill").foregroundStyle(Theme.accent).accessibilityLabel("On the map") }
        }
        .foregroundStyle(Theme.ink)
        .padding(.horizontal, 16)
        .frame(minHeight: 52)
    }

    private func pick(_ result: GeocodeSearchResult) {
        let label = result.name.isEmpty ? result.displayName : result.name
        picked = label
        if searchingFor == .from {
            from = label; origin = (result.latitude, result.longitude)
        } else {
            to = label; destination = (result.latitude, result.longitude)
        }
        searchingFor = nil
        focus = nil
        vm.clearLocationSearch()
    }

    /// The departure and arrival keep their day when the switch changes; the time comes or goes.
    /// A binding rather than `.onChange`, which also fired when the loaded value was put in and
    /// rewrote a journey's times to 09:00.
    private var allDayBinding: Binding<Bool> {
        Binding(get: { allDay }, set: { on in
            allDay = on
            func moved(_ value: String) -> String {
                value.isEmpty ? "" : on ? String(value.prefix(10)) : String(value.prefix(10)) + "T09:00"
            }
            departure = moved(departure)
            arrival = moved(arrival)
        })
    }

    private func seed(_ f: TransportationFormData) {
        base = f
        name = f.name; type = f.type; from = f.fromLocation; to = f.toLocation
        allDay = f.isAllDay; keepToTrip = f.constrainToCollectionDates
        departure = IosForms.shared.transportLocal(stored: f.departureDate, zone: f.departureTimezone, allDay: f.isAllDay)
        arrival = IosForms.shared.transportLocal(stored: f.arrivalDate, zone: f.arrivalTimezone, allDay: f.isAllDay)
        flight = f.flightNumber; link = f.link; notes = f.description_; isPublic = f.isPublic
        origin = f.originLatitude.flatMap { lat in f.originLongitude.map { (lat, $0) } }
        destination = f.destinationLatitude.flatMap { lat in f.destinationLongitude.map { (lat, $0) } }
        // What the fields give back untouched, so an unchanged form is not dirty.
        initial = form
        seeded = true
    }
}
