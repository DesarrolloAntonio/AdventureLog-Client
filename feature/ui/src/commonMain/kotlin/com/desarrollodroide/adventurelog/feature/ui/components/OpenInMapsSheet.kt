package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.ui.platform.isApplePlatform

/**
 * The same choices the web offers behind "Open in maps": a maps app, or the coordinates and the
 * link on the clipboard.
 *
 * Until now the app had one "Open in Maps" row whose callback reached a viewmodel method that
 * only wrote a log line - the button had never done anything.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenInMapsSheet(
    latitude: String,
    longitude: String,
    placeName: String,
    shareUrl: String?,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val coordinates = "$latitude, $longitude"
    val query = "$latitude,$longitude"

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = placeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = coordinates,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))

            if (isApplePlatform) {
                MapAction(Icons.Outlined.Map, "Apple Maps") {
                    uriHandler.openUri("https://maps.apple.com/?ll=$query&q=$placeName")
                    onDismiss()
                }
            }

            MapAction(Icons.Outlined.Map, "Google Maps") {
                uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=$query")
                onDismiss()
            }

            MapAction(Icons.Outlined.Place, "OpenStreetMap") {
                uriHandler.openUri(
                    "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude" +
                        "#map=16/$latitude/$longitude"
                )
                onDismiss()
            }

            MapAction(Icons.Outlined.ContentCopy, "Copy coordinates") {
                clipboard.setText(AnnotatedString(coordinates))
                onDismiss()
            }

            if (!shareUrl.isNullOrBlank()) {
                MapAction(Icons.Outlined.Link, "Copy link") {
                    clipboard.setText(AnnotatedString(shareUrl))
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun MapAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
