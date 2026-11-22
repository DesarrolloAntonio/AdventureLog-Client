package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.ContentImage
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import kotlin.math.roundToInt

private val MONTHS = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

/** "6 JUL 26", as the canvas stamps a visit; null when there is no date to stamp. */
fun visitStamp(date: String?): String? {
    val parts = date?.substringBefore('T')?.split("-")?.takeIf { it.size == 3 } ?: return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull()?.let { MONTHS.getOrNull(it - 1) } ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return "$day $month ${(year % 100).toString().padStart(2, '0')}"
}

/** "Vestland, Norway"; failing that, the place's own line from the server. */
fun placeWhere(location: Location): String? {
    val structured = listOfNotNull(
        location.region?.name?.takeIf { it.isNotBlank() },
        location.country?.name?.takeIf { it.isNotBlank() },
    )
    return structured.takeIf { it.isNotEmpty() }?.joinToString(", ")
        ?: location.location?.takeIf { it.isNotBlank() }
}

/** One stub of the facts strip. [collection] makes it open that trip. */
data class PlaceFact(val label: String, val value: String, val collection: UltraSlimCollection? = null)

/**
 * The strip under a place's name: when it was last visited, how many times, and the trip it is in.
 * Only facts the place has - a planned place has no visit to stamp. Several trips are counted; the
 * Collections section further down names them.
 */
fun placeFacts(location: Location, collections: List<UltraSlimCollection>): List<PlaceFact> = buildList {
    val last = location.visits.mapNotNull { it.startDate?.substringBefore('T') }.maxOrNull()
    visitStamp(last)?.let { add(PlaceFact("VISITED", it)) }
    if (location.visits.isNotEmpty()) add(PlaceFact("VISITS", location.visits.size.toString()))
    when (collections.size) {
        0 -> Unit
        1 -> add(PlaceFact("TRIP", collections.first().name, collections.first()))
        else -> add(PlaceFact("TRIPS", collections.size.toString()))
    }
}

/**
 * The photographs, large, as the canvas opens a place: one at a time, swiped, with a "1 / 4"
 * counter. Back and Share float over the top corners.
 */
@Composable
fun PlacePhotoHero(
    images: List<ContentImage>,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    showBack: Boolean,
    modifier: Modifier = Modifier,
) {
    val photos = images.filter { it.image.isNotBlank() }
    val pager = rememberPagerState { photos.size }
    val imageLoader = LocalImageLoader.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .aspectRatio(388f / 264f)
            .clip(RoundedCornerShape(36.dp)),
    ) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            Image(
                painter = rememberAsyncImagePainter(model = photos[page].image, imageLoader = imageLoader),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (showBack) HeroButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBackClick) else Spacer(Modifier)
            HeroButton(Icons.Default.Share, "Share", onShareClick)
        }
        if (photos.size > 1) {
            Text(
                text = "${pager.currentPage + 1} / ${photos.size}",
                color = MaterialTheme.colorScheme.inverseOnSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun HeroButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(icon, contentDescription = description)
    }
}

/** Category and visited status, the name large and condensed, where it is, and its stars. */
@Composable
fun PlaceHeadline(location: Location, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            location.category?.displayName?.takeIf { it.isNotBlank() }?.let { name ->
                StatusChip(
                    text = name,
                    container = colors.tertiaryContainer,
                    ink = colors.onTertiaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    emoji = location.category?.icon?.takeIf { it.isNotBlank() },
                )
            }
            if (location.isVisited) {
                StatusChip("Visited", colors.primaryContainer, colors.onPrimaryContainer, CircleShape, Icons.Default.Check)
            } else {
                StatusChip("Planned", colors.secondaryContainer, colors.onSecondaryContainer, CircleShape, Icons.Default.Schedule)
            }
        }
        Text(
            text = location.name,
            style = AdventureType.display(58.sp),
            modifier = Modifier.padding(top = 14.dp),
        )
        placeWhere(location)?.let { where ->
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Outlined.Place, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Text(where, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
        }
        val stars = location.rating?.roundToInt()?.coerceIn(0, 5) ?: 0
        if (stars > 0) {
            Text(
                text = "★".repeat(stars) + "☆".repeat(5 - stars),
                color = colors.primary,
                fontSize = 26.sp,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clearAndSetSemantics { contentDescription = "Rated $stars out of 5" },
            )
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    container: androidx.compose.ui.graphics.Color,
    ink: androidx.compose.ui.graphics.Color,
    shape: androidx.compose.ui.graphics.Shape,
    icon: ImageVector? = null,
    /** The category's own emoji, read past by a screen reader: the name says it. */
    emoji: String? = null,
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .background(container)
            .padding(start = if (icon != null) 8.dp else 12.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp)) }
        emoji?.let { Text(it, modifier = Modifier.clearAndSetSemantics { }) }
        Text(text, color = ink, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

/** VISITED / VISITS / TRIP as stubs split by dashed lines, the trip one opening the trip. */
@Composable
fun PlaceFactsStrip(
    facts: List<PlaceFact>,
    onCollectionClick: (UltraSlimCollection) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (facts.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    val dash = colors.outlineVariant
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.height(IntrinsicSize.Min).padding(vertical = 14.dp)) {
            facts.forEachIndexed { index, fact ->
                val open = fact.collection
                Column(
                    modifier = Modifier
                        // The date and the count take what they need; the trip, whose name is the
                        // long one, takes the rest and ellipsizes if it must.
                        .then(if (open != null) Modifier.weight(1f) else Modifier)
                        .fillMaxHeight()
                        .then(
                            if (index > 0) Modifier.drawBehind {
                                drawLine(
                                    dash, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx())),
                                )
                            } else Modifier,
                        )
                        .then(
                            if (open != null) Modifier
                                .clickable { onCollectionClick(open) }
                                .semantics { contentDescription = "Trip: ${open.name}"; role = Role.Button }
                            else Modifier,
                        )
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        fact.label,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        color = colors.onSurfaceVariant,
                    )
                    Text(
                        fact.value,
                        style = AdventureType.display(26.sp),
                        color = if (open != null) colors.primary else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (facts.none { it.collection != null }) Spacer(Modifier.weight(1f))
        }
    }
}

/**
 * The place's own floating toolbar, Material's toolbar-with-FAB: open in maps and the website when
 * the place has them, Edit as the FAB. Nothing to show and nowhere to edit: no toolbar at all.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaceActionsToolbar(
    onOpenMaps: (() -> Unit)?,
    onOpenWebsite: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (onOpenMaps == null && onOpenWebsite == null && onEdit == null) return
    val colors = MaterialTheme.colorScheme
    val toolbarColors = FloatingToolbarDefaults.standardFloatingToolbarColors(
        toolbarContainerColor = colors.inverseSurface,
        toolbarContentColor = colors.inverseOnSurface,
        fabContainerColor = colors.tertiaryContainer,
        fabContentColor = colors.onTertiaryContainer,
    )
    val buttons: @Composable () -> Unit = {
        onOpenMaps?.let {
            IconButton(onClick = it) { Icon(Icons.Default.Map, contentDescription = "Open in maps") }
        }
        onOpenWebsite?.let {
            IconButton(onClick = it) { Icon(Icons.Default.Public, contentDescription = "Website") }
        }
    }
    if (onEdit != null) {
        HorizontalFloatingToolbar(
            expanded = onOpenMaps != null || onOpenWebsite != null,
            floatingActionButton = {
                FloatingToolbarDefaults.StandardFloatingActionButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit place")
                }
            },
            colors = toolbarColors,
            modifier = modifier,
        ) { buttons() }
    } else {
        HorizontalFloatingToolbar(expanded = true, colors = toolbarColors, modifier = modifier) { buttons() }
    }
}
