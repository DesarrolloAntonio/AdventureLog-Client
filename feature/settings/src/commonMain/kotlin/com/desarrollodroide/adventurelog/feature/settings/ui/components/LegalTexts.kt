package com.desarrollodroide.adventurelog.feature.settings.ui.components

/**
 * The Terms of Use and the Privacy Policy, approved by the owner on 2026-10-06 (QA #4). They replace
 * texts copied from another app: terms that named "Shiori" under the wrong licence, and a policy
 * still reading "Effective as of [Insert Date Here]" that said the app reached no third party.
 * Every factual line was checked against the code that day; change it when what the app does
 * changes - the permissions, the services it contacts, what it keeps on the phone.
 * Each platform has its own version (IosTermsOfUse, IosPrivacyPolicy for the SwiftUI screen): they
 * differ only where the apps do - maps, Wikipedia, what is kept on the phone, permissions.
 */
data class LegalSection(val heading: String, val body: String)

data class LegalText(val title: String, val effective: String, val sections: List<LegalSection>)

const val LEGAL_EFFECTIVE = "Effective 25 November 2026."
const val LEGAL_CONTACT = "desarrollodroide@gmail.com"

/** What the texts say differently on each platform; the rest is the same on both. */
private class LegalPlatform(
    val name: String,
    val stored: String,
    val maps: String,
    val wikipedia: String?,
    val handedToApps: String,
    val permissions: String,
)

private val AndroidLegal = LegalPlatform(
    name = "Android",
    stored = "• The address of your server and a session token that keeps you signed in. Your password is " +
        "not stored. Signing out deletes the session.\n" +
        "• Your display settings (theme, colours).\n" +
        "• A cache of images, so places and photos don't download again each time.\n\n" +
        "These are excluded from Android backups and device-to-device transfers, so they never " +
        "leave the phone that way.",
    maps = "• Google Maps (Google LLC) - to draw maps. Google receives what any map request carries " +
        "(the area shown, your device's network address) under Google's Privacy Policy " +
        "(policies.google.com/privacy).",
    wikipedia = "• Wikipedia (Wikimedia Foundation) - only when you tap Generate description or look for a " +
        "Wikipedia image: the place name you typed is sent to Wikipedia's API.",
    handedToApps = "• Other apps you choose - Open in Maps, Share, opening a link or an attachment hand that " +
        "item to the app you pick.",
    permissions = "• Camera - only when you choose to take a photo for a place.\n" +
        "• Photos - only to pick an image you choose to add (Android 12 and older ask for this as " +
        "\"storage\").\n" +
        "• Internet and network state - to reach your server and tell you when it can't.",
)

private val IosLegal = LegalPlatform(
    name = "iOS",
    stored = "• The address of your server and a session token that keeps you signed in. Your password is " +
        "not stored. Signing out deletes the session.\n" +
        "• Your display settings (theme).\n\n" +
        "These are kept in the iPhone's keychain for this device only, so they are not carried into " +
        "backups or onto another phone. Images are kept in memory only while the app is open.",
    maps = "• Apple Maps (Apple Inc.) - to draw maps. Apple receives what any map request carries " +
        "(the area shown, your device's network address) under Apple's Privacy Policy " +
        "(apple.com/legal/privacy).",
    wikipedia = null,
    handedToApps = "• Other apps you choose - Directions and Share hand that item to the app you pick.",
    permissions = "The app asks for no permissions on iPhone.",
)

/** Both platforms: a provider sign-in goes through the provider's own page. */
private const val PROVIDER_SIGN_IN =
    "• The sign-in provider you choose - Continue with GitHub, Google or another provider your server " +
        "offers opens that provider's page in a private browser window; it handles the sign-in under " +
        "its own policy and tells your server who you are."

private fun termsOfUse(platform: LegalPlatform) = LegalText(
    title = "Terms of Use",
    effective = LEGAL_EFFECTIVE,
    sections = listOf(
        LegalSection(
            "About the app",
            "AdventureLog for ${platform.name} (the \"app\") is an independent, open-source client for AdventureLog, " +
                "a self-hosted travel log. It is developed by Antonio Corrales and is not affiliated with " +
                "or endorsed by the AdventureLog project."
        ),
        LegalSection(
            "Your server, your account",
            "The app does not provide a service of its own. It connects to an AdventureLog server that you " +
                "choose - usually one you or someone you trust runs - and everything you see and save lives " +
                "on that server, under that server's own rules. You are responsible for the server you " +
                "connect to and for the account you use on it."
        ),
        LegalSection(
            "Licence",
            "The app's source code is published under the MIT License " +
                "(github.com/DesarrolloAntonio/AdventureLog-Client). You may use, copy, modify and " +
                "distribute it under the terms of that licence."
        ),
        LegalSection(
            "No warranty",
            "The app is provided \"as is\", without warranty of any kind, express or implied, including " +
                "fitness for a particular purpose. Keep your own backups of your server's data."
        ),
        LegalSection(
            "Limitation of liability",
            "To the extent permitted by law, the developer is not liable for any loss of data, profits or " +
                "use, or any other damage arising from the use of, or the inability to use, the app."
        ),
        LegalSection(
            "Changes",
            "These terms may be updated with new versions of the app. The date at the top says when they " +
                "last changed."
        ),
        LegalSection("Contact", LEGAL_CONTACT)
    )
)

private fun privacyPolicy(platform: LegalPlatform) = LegalText(
    title = "Privacy Policy",
    effective = LEGAL_EFFECTIVE,
    sections = listOf(
        LegalSection(
            "In short",
            "The developer of this app collects nothing. Your data goes to the AdventureLog server you sign " +
                "in to, and to the few services listed below when you use the feature that needs them."
        ),
        LegalSection("What the app stores on your phone", platform.stored),
        LegalSection(
            "What the app sends, and where",
            listOfNotNull(
                "• Your AdventureLog server - everything you view and save: places, visits, collections, notes " +
                    "and the photos you add. The server's owner decides how that data is kept.",
                platform.maps,
                platform.wikipedia,
                PROVIDER_SIGN_IN,
                platform.handedToApps,
            ).joinToString("\n") +
                "\n\nThe server you use may in turn contact services of its own (for example to look up an " +
                "address); that is the server's business, not the app's."
        ),
        LegalSection(
            "What the app does not do",
            "No analytics, no advertising, no crash reporting, no tracking, no account with the developer. " +
                "It does not read your location."
        ),
        LegalSection("Permissions", platform.permissions),
        LegalSection("Children", "The app is not directed at children and collects nothing about anyone."),
        LegalSection(
            "Changes",
            "This policy may change with new versions of the app; the date at the top says when it last did."
        ),
        LegalSection("Contact", LEGAL_CONTACT)
    )
)

val TermsOfUse = termsOfUse(AndroidLegal)
val PrivacyPolicy = privacyPolicy(AndroidLegal)

/** The iPhone app's: Apple Maps, no Wikipedia lookups, no permissions, the keychain for this device. */
val IosTermsOfUse = termsOfUse(IosLegal)
val IosPrivacyPolicy = privacyPolicy(IosLegal)

/**
 * The whole text as one string: the date, then each heading and body. The Android screens show
 * the same text with the headings in bold (annotated(), in androidMain).
 */
internal fun LegalText.plainText(): String = buildString {
    append("$effective\n\n")
    sections.forEach { section ->
        append("${section.heading}\n\n")
        append("${section.body}\n\n")
    }
}
