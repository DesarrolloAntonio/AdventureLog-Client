package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.MediaUsage
import com.desarrollodroide.adventurelog.core.model.formatBytes
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.StorageSectionState

@Composable
fun StorageSection(
    state: StorageSectionState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsGroup(title = "Media storage", modifier = modifier) {
        val usage = state.usage
        when {
            state.isLoading && usage == null -> Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            }

            usage == null -> SettingsRow(
                title = "Storage usage unavailable",
                supporting = state.error,
                tint = MaterialTheme.colorScheme.error,
                showChevron = false,
                trailing = { TextButton(onClick = onRetry) { Text("Retry") } }
            )

            else -> UsageBody(usage)
        }
    }
}

/**
 * One bar, split by what fills it.
 *
 * Three separate bars scaled against the largest slice said nothing useful: images are always
 * nearly all of it, so one bar was always full and the other two were always hairlines. Slices of
 * a single bar at least show the proportion honestly.
 */
@Composable
private fun UsageBody(usage: MediaUsage) {
    val slices = listOf(
        Slice("Images", usage.imagesBytes, usage.imagesFiles, MaterialTheme.colorScheme.primary),
        Slice(
            "Attachments",
            usage.attachmentsBytes,
            usage.attachmentsFiles,
            MaterialTheme.colorScheme.tertiary
        ),
        Slice(
            "Profile picture",
            usage.profilePicsBytes,
            usage.profilePicsFiles,
            MaterialTheme.colorScheme.secondary
        )
    )
    val limit = usage.limitBytes
    val scale = (limit ?: usage.totalBytes).coerceAtLeast(1L)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = formatBytes(usage.totalBytes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = buildString {
                append(if (limit == null) "used, no limit set" else "of ${formatBytes(limit)} used")
                append(" · ")
                append(grouped(usage.totalFiles))
                append(if (usage.totalFiles == 1) " file" else " files")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            slices.filter { it.bytes > 0 }.forEach { slice ->
                // A slice worth a few kilobytes next to a few hundred megabytes rounds to nothing;
                // a floor keeps it visible without pretending it is bigger than it is.
                val fraction = (slice.bytes.toFloat() / scale.toFloat()).coerceAtLeast(0.02f)
                Box(
                    modifier = Modifier
                        .weight(fraction)
                        .fillMaxHeight()
                        .background(slice.color)
                )
            }
            val used = slices.sumOf { it.bytes }
            if (used < scale) {
                Box(Modifier.weight((scale - used).toFloat() / scale.toFloat()))
            }
        }

        Spacer(Modifier.height(16.dp))
        slices.forEach { LegendRow(it) }
    }
}

private data class Slice(val label: String, val bytes: Long, val files: Int, val color: Color)

@Composable
private fun LegendRow(slice: Slice) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(slice.color)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = slice.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${formatBytes(slice.bytes)} · ${grouped(slice.files)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Four figures without a separator read as a serial number, not a count. */
private fun grouped(value: Int): String =
    value.toString().reversed().chunked(3).joinToString(",").reversed()
