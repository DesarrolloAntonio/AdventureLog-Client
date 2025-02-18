
actual fun isValidUrl(url: String): Boolean {
    return android.webkit.URLUtil.isValidUrl(url)
}