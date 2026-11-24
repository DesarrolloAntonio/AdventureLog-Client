import SwiftUI
import ComposeApp

/// New or edit stay, as the Mist-LodgingForm artboard draws it: the kind of place as chips, its
/// name and where it is, check-in and check-out, then the booking details.
struct LodgingFormView: View {
    let collectionId: String
    let collectionName: String
    var lodgingId: String?
    let onSaved: () -> Void

    @StateObject private var host = ViewModelHost()
    @State private var state = LodgingFormState(name: "", type: "hotel", description: "", checkIn: "", checkOut: "",
                                                reservationNumber: "", price: "", priceCurrency: "", link: "", location: "",
                                                isPublic: false, isSaving: false, isLoading: false, loadError: nil, error: nil, saved: false)
    @State private var name = ""
    @State private var type = "hotel"
    @State private var location = ""
    @State private var checkIn = ""
    @State private var checkOut = ""
    @State private var reservation = ""
    @State private var price = ""
    @State private var link = ""
    @State private var notes = ""
    @State private var isPublic = false
    @State private var seeded = false
    @Environment(\.dismiss) private var dismiss

    private var vm: AddEditLodgingViewModel { host.owner.addEditLodgingViewModel() }

    var body: some View {
        FormScaffold(
            title: lodgingId == nil ? "New Lodging" : "Edit Lodging", subtitle: collectionName,
            canSave: state.canSave, saving: state.isSaving, dirty: vm.hasChanges(form: state),
            error: state.error, clearError: { vm.clearError() }, save: { vm.save(collectionId: collectionId) }
        ) {
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity).padding(.top, 60)
            } else if let error = state.loadError {
                ContentUnavailableView("Couldn't load the stay", systemImage: "wifi.exclamationmark", description: Text(error))
                Button("Try again") { vm.retryLoad(collectionId: collectionId) }.buttonStyle(.glassProminent)
            } else {
                ChoiceChips(options: AddEditLodgingViewModelKt.LodgingTypes.map { ($0, label($0)) }, selection: $type)
                FormCard {
                    FormField(placeholder: "Name", text: $name, font: .title3.weight(.semibold))
                    FormDivider()
                    FormField(placeholder: "Address or area", text: $location)
                }
                SectionLabel(text: "Stay").padding(.bottom, -8)
                FormCard {
                    DateRow(title: "Check-in", value: $checkIn)
                    FormDivider()
                    DateRow(title: "Check-out", value: $checkOut)
                }
                SectionLabel(text: "Booking").padding(.bottom, -8)
                FormCard {
                    FormField(placeholder: "Reservation number", text: $reservation, capitalization: .characters)
                    FormDivider()
                    FormField(placeholder: "Price", text: $price, keyboard: .decimalPad)
                    FormDivider()
                    FormField(placeholder: "Link", text: $link, keyboard: .URL, capitalization: .never)
                }
                FormCard { FormField(placeholder: "Notes", text: $notes, axis: .vertical) }
                FormCard { ToggleRow(title: "Make public", isOn: $isPublic) }
            }
        }
        .onChange(of: name) { _, v in vm.onNameChange(v: v) }
        .onChange(of: type) { _, v in vm.onTypeChange(v: v) }
        .onChange(of: location) { _, v in vm.onLocationChange(v: v) }
        .onChange(of: checkIn) { _, v in vm.onCheckInChange(v: v) }
        .onChange(of: checkOut) { _, v in vm.onCheckOutChange(v: v) }
        .onChange(of: reservation) { _, v in vm.onReservationChange(v: v) }
        .onChange(of: price) { _, v in vm.onPriceChange(v: v) }
        .onChange(of: link) { _, v in vm.onLinkChange(v: v) }
        .onChange(of: notes) { _, v in vm.onDescriptionChange(v: v) }
        .onChange(of: isPublic) { _, v in vm.onPublicChange(v: v) }
        .task { vm.load(collectionId: collectionId, id: lodgingId) }
        .task {
            for await s in vm.state {
                state = s
                if !seeded, !s.isLoading, s.loadError == nil {
                    name = s.name; type = s.type; location = s.location; checkIn = s.checkIn; checkOut = s.checkOut
                    reservation = s.reservationNumber; price = s.price; link = s.link; notes = s.description_; isPublic = s.isPublic
                    seeded = true
                }
                if s.saved { onSaved(); dismiss() }
            }
        }
    }

    private func label(_ type: String) -> String {
        type == "bnb" ? "B&B" : type.prefix(1).uppercased() + type.dropFirst()
    }
}
