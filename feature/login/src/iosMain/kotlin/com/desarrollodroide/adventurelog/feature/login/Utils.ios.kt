import platform.Foundation.NSURLComponents

actual fun isValidUrl(url: String): Boolean {
    if (url.isBlank()) return false

    val components = NSURLComponents(url) ?: return false
    return !components.scheme.isNullOrBlank() && !components.host.isNullOrBlank()
}