package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.compose.LocalPlatformContext
import com.desarrollodroide.adventurelog.feature.ui.BuildConfig
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import kotlin.math.abs

/**
 * Stand-in artwork for a location that has no photo yet.
 *
 * Two layers. The base is drawn locally and always present: a survey grid, a sight over the
 * position and the coordinates in monospace - a field-notebook look that costs no network and
 * cannot fail. When a Maps key is configured and the location has coordinates, a static map of
 * that spot fades in on top.
 *
 * Layering them this way means there is no loading state to design and no error state to handle:
 * if the map is slow, missing or fails, the artwork underneath is already the finished card.
 *
 * The card's title sits on a dark scrim along the bottom edge, so both layers stay dark and
 * saturated; the earlier pastel-to-white version left the white title fighting its background and
 * read as a failed image load.
 */
@Composable
fun LocationPlaceholder(
    name: String,
    modifier: Modifier = Modifier,
    latitude: String? = null,
    longitude: String? = null
) {
    val palette = remember(name) {
        placeholderPalettes[abs(name.hashCode()) % placeholderPalettes.size]
    }
    val coordinates = remember(latitude, longitude) {
        val lat = latitude?.toDoubleOrNull()
        val lon = longitude?.toDoubleOrNull()
        if (lat != null && lon != null) lat to lon else null
    }

    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(colors = listOf(palette.first, palette.second))
        )
    ) {
        CoordinateArtwork(name = name, coordinates = coordinates)

        val mapUrl = remember(coordinates) { coordinates?.let(::staticMapUrl) }
        if (mapUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(mapUrl)
                    .crossfade(350)
                    .build(),
                imageLoader = LocalImageLoader.current,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CoordinateArtwork(
    name: String,
    coordinates: Pair<Double, Double>?
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = size.minDimension / 6f
        var x = spacing
        while (x < size.width) {
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += spacing
        }
        var y = spacing
        while (y < size.height) {
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += spacing
        }

        // The sight sits high: the lower half of the card is under the title scrim, which now
        // carries the place label, its rating and its tags as well as the name.
        val centre = Offset(size.width / 2f, size.height * 0.22f)
        val unit = size.minDimension * 0.10f
        drawCircle(Color.White.copy(alpha = 0.20f), unit * 2f, centre, style = Stroke(1.5.dp.toPx()))
        drawCircle(Color.White.copy(alpha = 0.32f), unit, centre, style = Stroke(1.5.dp.toPx()))
        drawCircle(Color.White.copy(alpha = 0.85f), unit * 0.22f, centre)
    }

    Column(
        // Sits clear of the sight above it and of the title scrim below.
        modifier = Modifier.fillMaxSize().offset(y = (-30).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val lines = coordinates?.let { (lat, lon) ->
            listOf(formatLatitude(lat), formatLongitude(lon))
        } ?: listOf(name.trim().firstOrNull()?.uppercase() ?: "?")

        lines.forEach { line ->
            Text(
                text = line,
                fontFamily = FontFamily.Monospace,
                fontSize = if (coordinates != null) 15.sp else 96.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = if (coordinates != null) 1.5.sp else 0.sp,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = if (coordinates != null) 0.85f else 0.22f)
            )
        }
    }
}

/**
 * Google Static Maps thumbnail centred on the location.
 *
 * Returns null when no key is configured, so the coordinate artwork stands alone. Coil caches by
 * URL, so revisiting a card costs nothing; only the first view of each location hits the network.
 */
private fun staticMapUrl(coordinates: Pair<Double, Double>): String? {
    val key = BuildConfig.MAPS_API_KEY
    if (key.isBlank()) return null

    val (lat, lon) = coordinates
    return "https://maps.googleapis.com/maps/api/staticmap" +
        "?center=$lat,$lon" +
        "&zoom=11" +
        "&size=400x260" +
        "&scale=2" +
        "&maptype=terrain" +
        "&markers=color:0xD81B60%7C$lat,$lon" +
        "&key=$key"
}

internal fun formatLatitude(value: Double): String =
    "${formatDegrees(abs(value))}° ${if (value >= 0) "N" else "S"}"

internal fun formatLongitude(value: Double): String =
    "${formatDegrees(abs(value))}° ${if (value >= 0) "E" else "W"}"

/** Three decimals - roughly 100 m, enough to identify a spot without looking like false precision. */
private fun formatDegrees(value: Double): String {
    val scaled = (value * 1000).toLong()
    return "${scaled / 1000}.${(scaled % 1000).toString().padStart(3, '0')}"
}

private val placeholderPalettes = listOf(
    Color(0xFF1F3A5F) to Color(0xFF0B1B2E), // deep blue
    Color(0xFF14524A) to Color(0xFF06231F), // pine
    Color(0xFF5B3A1E) to Color(0xFF2A1809), // umber
    Color(0xFF432C5C) to Color(0xFF1C1128), // aubergine
    Color(0xFF1E4630) to Color(0xFF0A1C13), // moss
    Color(0xFF5C2733) to Color(0xFF260F15), // wine
    Color(0xFF3F4A22) to Color(0xFF1A1F0C), // olive
    Color(0xFF2E3A40) to Color(0xFF12181B)  // slate
)
