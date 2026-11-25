package com.desarrollodroide.adventurelog.feature.detail.ui.components

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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Attachment
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults

/**
 * Files attached to a location. Tapping one downloads it and hands it to a viewer - the file
 * cannot be opened straight from its URL, because attachments sit behind the server's auth check.
 */
@Composable
fun AttachmentsSection(
    attachments: List<Attachment>,
    openingAttachmentId: String?,
    onOpenAttachment: (Attachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return

    // One segmented block, as the app's other short lists are.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)
    ) {
        attachments.forEachIndexed { index, attachment ->
            AttachmentRow(
                attachment = attachment,
                isOpening = openingAttachmentId == attachment.id,
                shapeIndex = index,
                count = attachments.size,
                onClick = { onOpenAttachment(attachment) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AttachmentRow(
    attachment: Attachment,
    isOpening: Boolean,
    shapeIndex: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = !isOpening,
        shape = SegmentedListDefaults.shapeFor(shapeIndex, count),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ExtensionBadge(attachment.extension)
            Text(
                text = attachment.name?.takeIf { it.isNotBlank() }
                    ?: attachment.file.substringAfterLast('/'),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                if (isOpening) {
                    LoadingIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/** The file's type on the row's tinted square, where the other lists put their icon. */
@Composable
private fun ExtensionBadge(extension: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.size(width = 48.dp, height = 40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = extension.trimStart('.').uppercase().take(4),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
