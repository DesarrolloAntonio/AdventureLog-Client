package com.desarrollodroide.adventurelog.feature.ui.components

import com.desarrollodroide.adventurelog.feature.ui.util.tryOpenUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.platform.isApplePlatform
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

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
    onDismiss: () -> Unit,
    /** Called when no app on the device takes the map link. */
    onCannotOpen: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    val coordinates = "$latitude, $longitude"
    val query = "$latitude,$longitude"

    // The apps first, then the clipboard: two blocks, as the two kinds of answer they are.
    val apps = buildList {
        if (isApplePlatform) {
            add(MapChoice(Icons.Outlined.Map, "Apple Maps") {
                if (!uriHandler.tryOpenUri("https://maps.apple.com/?ll=$query&q=$placeName")) onCannotOpen()
                onDismiss()
            })
        }
        add(MapChoice(Icons.Outlined.Map, "Google Maps") {
            if (!uriHandler.tryOpenUri("https://www.google.com/maps/search/?api=1&query=$query")) onCannotOpen()
            onDismiss()
        })
        add(MapChoice(Icons.Outlined.Place, "OpenStreetMap") {
            val opened = uriHandler.tryOpenUri(
                "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude" +
                    "#map=16/$latitude/$longitude"
            )
            if (!opened) onCannotOpen()
            onDismiss()
        })
    }
    val copies = buildList {
        add(MapChoice(Icons.Outlined.ContentCopy, "Copy coordinates") {
            clipboard.setText(AnnotatedString(coordinates))
            onDismiss()
        })
        if (!shareUrl.isNullOrBlank()) {
            add(MapChoice(Icons.Outlined.Link, "Copy link") {
                clipboard.setText(AnnotatedString(shareUrl))
                onDismiss()
            })
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = placeName,
                    style = AdventureType.display(30.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = coordinates,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MapChoiceGroup(apps)
            MapChoiceGroup(copies)
        }
    }
}

private class MapChoice(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
private fun MapChoiceGroup(choices: List<MapChoice>) {
    Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
        choices.forEachIndexed { index, choice ->
            MapAction(choice, SegmentedListDefaults.shapeFor(index, choices.size))
        }
    }
}

@Composable
private fun MapAction(choice: MapChoice, shape: Shape) {
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(role = Role.Button, onClick = choice.onClick)
                .heightIn(min = 60.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(choice.icon, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Text(text = choice.label, style = MaterialTheme.typography.titleMedium)
        }
    }
}
