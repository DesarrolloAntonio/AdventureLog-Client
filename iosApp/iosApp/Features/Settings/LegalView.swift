import SwiftUI
import ComposeApp

/// Terms of Use and Privacy Policy, as the Mist-Legal artboard draws them: Terms / Privacy at the
/// top, when the text took effect, each section in a frosted card, and a way to write to us. The
/// texts are the shared ones the Android app shows.
struct LegalView: View {
    enum Page: Hashable { case terms, privacy }

    @State private var page: Page = .terms
    @Environment(\.openURL) private var openURL

    private var text: LegalText { page == .terms ? LegalTextsKt.TermsOfUse : LegalTextsKt.PrivacyPolicy }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                Picker("Document", selection: $page) {
                    Text("Terms").tag(Page.terms)
                    Text("Privacy").tag(Page.privacy)
                }
                .pickerStyle(.segmented)
                .padding(.bottom, 4)

                VStack(alignment: .leading, spacing: 4) {
                    Text(text.title).font(.title2.weight(.bold)).foregroundStyle(Theme.ink)
                    Text(text.effective).font(.subheadline).foregroundStyle(Theme.muted)
                }
                .padding(.horizontal, 4)

                ForEach(text.sections, id: \.heading) { section in
                    VStack(alignment: .leading, spacing: 8) {
                        Text(section.heading).font(.headline).foregroundStyle(Theme.ink)
                        Text(section.body).font(.body).foregroundStyle(Theme.muted)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .mistCard()
                }

                VStack(alignment: .leading, spacing: 10) {
                    Text("Questions?").font(.headline).foregroundStyle(Theme.ink)
                    Button {
                        if let url = URL(string: "mailto:\(LegalTextsKt.LEGAL_CONTACT)") { openURL(url) }
                    } label: {
                        Label("Contact us", systemImage: "envelope")
                            .font(.headline)
                            .frame(maxWidth: .infinity, minHeight: 48)
                            .background { HeroGlow().clipShape(.rect(cornerRadius: 16)) }
                            .heroCard(radius: 16)
                    }
                    .buttonStyle(.plain)
                }
                .padding(16)
                .mistCard()
            }
            .padding(.horizontal, 16)
            .padding(.top, 4)
            .padding(.bottom, 60)
        }
        .mistBackground()
        .navigationTitle("Terms and privacy")
        .navigationBarTitleDisplayMode(.inline)
    }
}
