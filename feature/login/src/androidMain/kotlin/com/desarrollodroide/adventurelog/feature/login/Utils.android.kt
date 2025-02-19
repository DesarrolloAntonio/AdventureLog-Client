import android.net.Uri

actual fun isValidUrl(url: String): Boolean {
    return try {
        val uri = Uri.parse(url)
        uri.scheme in listOf("http", "https") && uri.host?.isNotEmpty() == true
    } catch (e: Exception) {
        false
    }
}