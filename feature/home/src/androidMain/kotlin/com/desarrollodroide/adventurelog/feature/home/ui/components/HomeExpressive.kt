package com.desarrollodroide.adventurelog.feature.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.ui.components.LocationPlaceholder
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import androidx.graphics.shapes.RoundedPolygon

/** "1,234" - the counts here reach the thousands (cities in the world). */
internal fun Int.groupedThousands(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()

/**
 * "Your map so far" in Expressive pieces: how much of what you saved you have visited, as a wavy
 * ring, then countries, regions and cities each in a shape of its own from Material's shape
 * library - a cookie, a clover, a sun - with the total beneath.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MapSoFarTiles(stats: UserStats, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PlacesVisitedCard(visited = stats.visitedLocationCount, total = stats.locationCount)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShapeStat(
                value = stats.visitedCountryCount, total = stats.totalCountries,
                singular = "country", plural = "countries",
                polygon = MaterialShapes.Cookie9Sided, container = colors.primary, content = colors.onPrimary,
                modifier = Modifier.weight(1f),
            )
            ShapeStat(
                value = stats.visitedRegionCount, total = stats.totalRegions,
                singular = "region", plural = "regions",
                polygon = MaterialShapes.Clover4Leaf, container = colors.tertiaryContainer, content = colors.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )
            ShapeStat(
                value = stats.visitedCityCount, total = stats.totalCities,
                singular = "city", plural = "cities",
                polygon = MaterialShapes.Sunny, container = colors.secondaryContainer, content = colors.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlacesVisitedCard(visited: Int, total: Int) {
    val colors = MaterialTheme.colorScheme
    val fraction = if (total > 0) (visited.toFloat() / total).coerceIn(0f, 1f) else 0f
    val stroke = with(LocalDensity.current) { Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round) }
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .padding(20.dp)
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxSize(),
                    color = colors.primary,
                    trackColor = colors.onPrimaryContainer.copy(alpha = 0.14f),
                    stroke = stroke,
                    trackStroke = stroke,
                )
                Text(visited.groupedThousands(), style = AdventureType.display(34.sp), color = colors.onPrimaryContainer)
            }
            Column {
                Text(if (visited == 1) "place visited" else "places visited", style = AdventureType.display(28.sp))
                Text(
                    text = "of ${total.groupedThousands()} saved",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShapeStat(
    value: Int,
    total: Int,
    singular: String,
    plural: String,
    polygon: RoundedPolygon,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    val shape = polygon.toShape()
    val number = value.groupedThousands()
    Column(
        modifier.semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(88.dp)
                .clip(shape)
                .background(container),
            contentAlignment = Alignment.Center,
        ) {
            // Condensed display figures; a four-digit count steps down so it stays inside the shape.
            Text(number, style = AdventureType.display(if (number.length > 3) 24.sp else 34.sp), color = content, maxLines = 1)
        }
        Text(
            text = if (value == 1) singular else plural,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "of ${total.groupedThousands()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The places touched last, as Material's multi-browse carousel: one large photograph, the next
 * ones narrowing towards the edge, each masked to the carousel's rounded shape. The name sits on
 * a dark fade at the foot of the photo - never straight on a bright sky - and fades with the item
 * as it narrows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecentPlacesCarousel(
    locations: List<Location>,
    onClick: (Location) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageLoader = LocalImageLoader.current
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { locations.size },
        preferredItemWidth = 200.dp,
        itemSpacing = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
    ) { index ->
        val location = locations[index]
        val photo = location.images.firstOrNull()?.image?.takeIf { it.isNotBlank() }
        Box(
            Modifier
                .fillMaxSize()
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable(role = Role.Button, onClickLabel = "Open") { onClick(location) }
                .clearAndSetSemantics { contentDescription = location.name },
        ) {
            if (photo != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = photo, imageLoader = imageLoader),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LocationPlaceholder(icon = location.category?.icon, modifier = Modifier.fillMaxSize())
            }
            val info = carouselItemDrawInfo
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .graphicsLayer {
                        // Gone well before the item narrows: the name is laid out at full width,
                        // so on a narrow item it would show cut at both ends ("efchaouen").
                        val span = info.maxSize - info.minSize
                        val open = if (span <= 0f) 1f else (info.size - info.minSize) / span
                        alpha = ((open - 0.8f) / 0.2f).coerceIn(0f, 1f)
                    }
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.62f))))
                    .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 14.dp),
            ) {
                Text(
                    text = location.name,
                    style = AdventureType.display(22.sp),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                location.location?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
