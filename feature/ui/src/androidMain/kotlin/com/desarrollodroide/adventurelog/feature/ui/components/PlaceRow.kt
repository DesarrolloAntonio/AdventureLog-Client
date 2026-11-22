package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import kotlin.math.roundToInt

/** "Vestland · Hiking": where the place is, then what it is; either may be missing. */
fun placeRowDetail(location: Location): String = listOfNotNull(
    location.region?.name?.takeIf { it.isNotBlank() }
        ?: location.location?.substringBefore(',')?.trim()?.takeIf { it.isNotBlank() },
    location.category?.displayName?.takeIf { it.isNotBlank() },
).joinToString(" · ")

/**
 * A place as one row of a segmented list, as the canvas draws Places: a rounded photo, the name,
 * where and what it is, and its rating as a pill. [shape] is its place in the group
 * ([SegmentedListDefaults.shapeFor]). The actions sheet opens from the trailing button or a long
 * press - the canvas draws neither, and the list had them on every card.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaceRow(
    location: Location,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onShare: () -> Unit = {},
    onManageCollections: () -> Unit = {},
    onDelete: () -> Unit = {},
    isOwner: Boolean = true,
    onRemoveFromCollection: (() -> Unit)? = null,
    /** The editor on its images. Offered in the actions only while the place has no photo. */
    onAddPhoto: (() -> Unit)? = null,
    /** False where the row only shows the place (the map's card): no menu, no long press. */
    showActions: Boolean = true,
) {
    var showMenu by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = shape,
        color = colors.surfaceContainerLowest,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 76.dp)
                .combinedClickable(onClick = onClick, onLongClick = if (showActions) ({ showMenu = true }) else null)
                .padding(start = 10.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PlaceThumbnail(location)
            Column(Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val detail = placeRowDetail(location)
                if (detail.isNotEmpty()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            val rating = location.rating?.roundToInt()?.takeIf { it > 0 }
            if (rating != null) RatingPill(rating)
            if (showActions) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = colors.onSurfaceVariant)
                }
            }
        }
    }

    PlaceActions(
        location = location,
        visible = showMenu,
        onDismiss = { showMenu = false },
        onOpenDetails = onClick,
        onEdit = onEdit,
        onDuplicate = onDuplicate,
        onShare = onShare,
        onManageCollections = onManageCollections,
        onDelete = onDelete,
        isOwner = isOwner,
        onRemoveFromCollection = onRemoveFromCollection,
        onAddPhoto = onAddPhoto?.takeIf { location.images.none { it.image.isNotBlank() } },
    )
}

@Composable
private fun PlaceThumbnail(location: Location) {
    val photo = location.images.firstOrNull()?.image?.takeIf { it.isNotBlank() }
    Box(Modifier.size(56.dp).clip(RoundedCornerShape(18.dp))) {
        if (photo != null) {
            Image(
                painter = rememberAsyncImagePainter(model = photo, imageLoader = LocalImageLoader.current),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // The slot keeps its size: a row without a photo is not a shorter row.
            LocationPlaceholder(icon = location.category?.icon, modifier = Modifier.fillMaxSize())
        }
    }
}

/** "★ 5" on a pill: the top mark in the primary container, the rest in the secondary one. */
@Composable
private fun RatingPill(rating: Int) {
    val colors = MaterialTheme.colorScheme
    val top = rating >= 5
    Text(
        text = "★ $rating",
        color = if (top) colors.onPrimaryContainer else colors.onSecondaryContainer,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (top) colors.primaryContainer else colors.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clearAndSetSemantics { contentDescription = "Rated $rating out of 5" },
    )
}
