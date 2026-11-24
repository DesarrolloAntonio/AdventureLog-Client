import SwiftUI
import ComposeApp

/// New or edit journey, as the Mist-TransportForm artboard draws it: how you travel as chips, from
/// and to with a swap, when it leaves and arrives, and the details.
struct TransportFormView: View {
    let collectionId: String
    let collectionName: String
    var transportationId: String?
    let onSaved: () -> Void

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
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditTransportationViewModel {
        host.owner.addEditTransportationViewModel(transportationId: transportationId, collectionId: collectionId)
    }

    private var form: TransportationFormData {
        IosForms.shared.transport(base: base, name: name, type: type, description: notes, link: link, from: from, to: to,
                                  departure: departure, arrival: arrival, allDay: allDay, keepToTripDates: keepToTrip,
                                  flightNumber: flight, isPublic: isPublic)
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
            error: error, clearError: { vm.clearError() }, save: { vm.saveTransportation(formData: form) }
        ) {
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
                            FormField(placeholder: "From", text: $from)
                            FormDivider()
                            FormField(placeholder: "To", text: $to)
                        }
                        Button { swap(&from, &to) } label: {
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
                }
                FormCard { ToggleRow(title: "Make public", isOn: $isPublic) }
            }
        }
        .task {
            for await s in vm.uiState {
                loading = s.isLoading
                error = s.errorMessage
                if s.isLoading, seeded { saving = true }
                if !s.isLoading { saving = false }
                if !seeded, !s.isLoading, transportationId == nil || s.existingTransportation != nil {
                    seed(IosForms.shared.transport(transportation: s.existingTransportation))
                }
                if s.isSaved { onSaved(); dismiss() }
            }
        }
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
        // What the fields give back untouched, so an unchanged form is not dirty.
        initial = form
        seeded = true
    }
}
