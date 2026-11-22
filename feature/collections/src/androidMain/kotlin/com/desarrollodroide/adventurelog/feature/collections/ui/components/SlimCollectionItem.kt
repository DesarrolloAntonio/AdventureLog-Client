package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.ui.components.LocationPlaceholder
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

private val PhotoWidth = 112.dp

private val MONTHS = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

private fun dayMonth(date: String?): Pair<Int, String>? {
    val parts = date?.substringBefore('T')?.split("-")?.takeIf { it.size == 3 } ?: return null
    val month = parts[1].toIntOrNull()?.let { MONTHS.getOrNull(it - 1) } ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return day to month
}

/** "5–9 SEP", "28 SEP – 3 OCT", or "5 SEP" for a single day; null with no start date. */
fun ticketDateRange(start: String?, end: String?): String? {
    val (fromDay, fromMonth) = dayMonth(start) ?: return null
    val to = dayMonth(end)
    return when {
        to == null || (to.first == fromDay && to.second == fromMonth) -> "$fromDay $fromMonth"
        to.second == fromMonth -> "$fromDay–${to.first} $fromMonth"
        else -> "$fromDay $fromMonth – ${to.first} ${to.second}"
    }
}

/**
 * The ticket's badge: where the trip stands against today. A finished trip or a folder needs none;
 * an archived one says it is archived, which is what matters on that tab.
 */
fun collectionBadge(collection: UltraSlimCollection): String? = when {
    collection.isArchived -> "ARCHIVED"
    collection.status == TripStatus.IN_PROGRESS -> "NOW"
    collection.status == TripStatus.UPCOMING -> when (val days = collection.daysUntilStart) {
        null -> "PLANNED"
        0 -> "TODAY"
        1 -> "TOMORROW"
        else -> "IN $days DAYS"
    }
    else -> null
}

/**
 * A collection as a ticket lying on its side, as the canvas draws Collections: the photo is the
 * stub on the left, a dashed tear line, then the name with its badge and the DATES and PLACES it
 * would have printed. The actions open from the menu or a long press.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SlimCollectionItem(
    collection: UltraSlimCollection,
    onClick: () -> Unit,
    onEditCollection: () -> Unit = {},
    onDeleteCollection: () -> Unit = {},
    onShareCollection: () -> Unit = {},
    onShareWithPeople: () -> Unit = {},
    onDuplicateCollection: () -> Unit = {},
    onArchiveCollection: () -> Unit = {},
    onDownloadPdf: () -> Unit = {},
    onExportZip: () -> Unit = {},
    busyLabel: String? = null,
    isOwner: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val dash = colors.outlineVariant

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        // The photo and the tear line fill the ticket's height whatever the text makes it: they sit
        // in a layer matched to the card's size. (An intrinsic-height Row asked the placeholder,
        // built on BoxWithConstraints, for an intrinsic size it cannot give.)
        Box(
            Modifier
                .heightIn(min = 120.dp)
                .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
        ) {
            Box(Modifier.matchParentSize()) {
                CollectionPhoto(collection, Modifier.width(PhotoWidth).fillMaxHeight())
                Box(
                    Modifier
                        .fillMaxHeight()
                        .padding(start = PhotoWidth, top = 12.dp, bottom = 12.dp)
                        .width(2.dp)
                        .drawBehind {
                            drawLine(
                                dash, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                            )
                        }
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = PhotoWidth + 2.dp + 16.dp, top = 12.dp, bottom = 12.dp)
                    .heightIn(min = 96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = collection.name,
                        style = AdventureType.display(26.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    collectionBadge(collection)?.let { Badge(it, highlighted = it != "ARCHIVED") }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = colors.onSurfaceVariant)
                    }
                }
                // Dates are the wide stub; a gap keeps "10–22 OCT" from running into the count
                // when the list is a narrow pane (measured on a tablet: "10–22 OCT3").
                Row(Modifier.padding(top = 8.dp, end = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ticketDateRange(collection.startDate, collection.endDate)?.let { dates ->
                        Stub("DATES", dates, Modifier.weight(1.6f))
                    }
                    Stub("PLACES", collection.adventureCount.toString(), Modifier.weight(1f))
                }
            }
        }
    }

    if (showMenu) {
        CollectionActionsSheet(
            collection = collection,
            busyLabel = busyLabel,
            isOwner = isOwner,
            onDismiss = { showMenu = false },
            onOpen = { showMenu = false; onClick() },
            onEdit = { showMenu = false; onEditCollection() },
            onShare = { showMenu = false; onShareCollection() },
            onShareWithPeople = { showMenu = false; onShareWithPeople() },
            onArchive = { showMenu = false; onArchiveCollection() },
            onDownloadPdf = { showMenu = false; onDownloadPdf() },
            onExportZip = { showMenu = false; onExportZip() },
            onDuplicate = { showMenu = false; onDuplicateCollection() },
            onDelete = { showMenu = false; onDeleteCollection() }
        )
    }
}

@Composable
private fun CollectionPhoto(collection: UltraSlimCollection, modifier: Modifier) {
    val image = collection.featuredImage?.takeIf { it.isNotBlank() }
    Box(modifier) {
        if (image != null) {
            Image(
                painter = rememberAsyncImagePainter(model = image, imageLoader = LocalImageLoader.current),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LocationPlaceholder(icon = null, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun Badge(text: String, highlighted: Boolean) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = text,
        color = if (highlighted) colors.onPrimaryContainer else colors.onSurfaceVariant,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.8.sp,
        maxLines = 1,
        modifier = Modifier
            .padding(start = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlighted) colors.primaryContainer else colors.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

@Composable
private fun Stub(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            label,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value, style = AdventureType.display(20.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
