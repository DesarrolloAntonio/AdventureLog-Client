package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    SettingsCard(
        emoji = "📦",
        title = "Media storage",
        subtitle = "What your images and attachments take up on the server",
        modifier = modifier
    ) {
        val usage = state.usage
        when {
            state.isLoading && usage == null -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            usage == null -> {
                Text(
                    text = state.error ?: "Could not load storage usage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRetry) { Text("Try again") }
            }

            else -> UsageBody(usage)
        }
    }
}

@Composable
private fun UsageBody(usage: MediaUsage) {
    Text(
        text = buildString {
            append("Using ${formatBytes(usage.totalBytes)}")
            val limit = usage.limitBytes
            if (limit == null) append(" with no configured limit.")
            else append(" of ${formatBytes(limit)}.")
        },
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = "${usage.totalFiles} files",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))

    // Each bar is scaled against the largest slice rather than the quota: with no limit set there
    // is nothing else to scale against, and images would otherwise sit at a hairline.
    val largest = maxOf(usage.imagesBytes, usage.attachmentsBytes, usage.profilePicsBytes, 1L)
    UsageBar("Images", usage.imagesBytes, usage.imagesFiles, largest)
    UsageBar("Attachments", usage.attachmentsBytes, usage.attachmentsFiles, largest)
    UsageBar("Profile picture", usage.profilePicsBytes, usage.profilePicsFiles, largest)
}

@Composable
private fun UsageBar(label: String, bytes: Long, files: Int, largest: Long) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatBytes(bytes)} · $files",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { bytes.toFloat() / largest.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}
