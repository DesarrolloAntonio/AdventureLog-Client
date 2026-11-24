package com.desarrollodroide.adventurelog.feature.world.ui.components

import androidx.compose.foundation.layout.PaddingValues
import com.desarrollodroide.adventurelog.feature.ui.components.bleedHorizontally
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.toShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import com.desarrollodroide.adventurelog.feature.world.ui.state.FilterMode

/**
 * The world so far as a passport page, as the canvas draws it: how many countries, large, then
 * stubs for the regions, the complete and the partial ones. Dark in light theme and light in dark,
 * like the floating toolbar.
 */
@Composable
fun PassportCard(
    visitedCountries: Int,
    totalCountries: Int,
    regionsVisited: Int,
    complete: Int,
    partial: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val dash = colors.outline
    Surface(shape = RoundedCornerShape(28.dp), color = colors.inverseSurface, contentColor = colors.inverseOnSurface, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(visitedCountries.toString(), style = AdventureType.display(88.sp), color = colors.inversePrimary)
                Column(Modifier.padding(bottom = 6.dp)) {
                    Text("countries visited", style = AdventureType.display(26.sp))
                    Text("of $totalCountries", style = MaterialTheme.typography.bodyMedium, color = colors.inverseOnSurface.copy(alpha = 0.75f))
                }
            }
            Box(
                Modifier.fillMaxWidth().height(2.dp).drawBehind {
                    drawLine(dash, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())))
                }
            )
            Row {
                listOf("REGIONS" to regionsVisited, "COMPLETE" to complete, "PARTIAL" to partial).forEach { (label, value) ->
                    Column(Modifier.weight(1f)) {
                        Text(label, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp,
                            color = colors.inverseOnSurface.copy(alpha = 0.75f))
                        Text(value.toString(), style = AdventureType.display(28.sp))
                    }
                }
            }
        }
    }
}

/** All / Complete / Partial / Not visited as one connected group (see VisitedFilterGroup). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WorldFilterGroup(selected: FilterMode, onSelect: (FilterMode) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val modes = listOf(FilterMode.ALL to "All", FilterMode.COMPLETE to "Complete", FilterMode.PARTIAL to "Partial", FilterMode.NOT_VISITED to "Not visited")
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        modes.forEachIndexed { index, (mode, label) ->
            ToggleButton(
                checked = selected == mode,
                onCheckedChange = { if (it) onSelect(mode) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                // The app's one selection ink: the chosen option dark, the rest neutral.
                colors = ToggleButtonDefaults.colors(
                    containerColor = colors.surfaceContainerHighest,
                    contentColor = colors.onSurfaceVariant,
                    checkedContainerColor = colors.inverseSurface,
                    checkedContentColor = colors.inverseOnSurface,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                modifier = Modifier.weight(if (mode == FilterMode.NOT_VISITED) 1.3f else 1f).heightIn(min = 48.dp)
                    .semantics { contentDescription = label },
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/**
 * A country as a row of the segmented list: its code as a passport stamp - inked when any region
 * is visited, dashed when none is - its name, and how many of its regions are visited.
 */
@Composable
fun CountryRow(country: Country, shape: Shape, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val visited = country.numVisits > 0
    Surface(shape = shape, color = colors.surfaceContainerLowest, modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.clickable { onClick(country.countryCode) }.heightIn(min = 72.dp).padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .size(width = 52.dp, height = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (visited) Modifier.background(colors.primary)
                        else Modifier.border(2.dp, colors.outline, RoundedCornerShape(12.dp))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(country.countryCode, style = AdventureType.stamp().copy(fontSize = 20.sp), color = if (visited) colors.onPrimary else colors.onSurfaceVariant)
            }
            Column(Modifier.weight(1f)) {
                Text(country.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = listOfNotNull(
                        "${country.numVisits} of ${country.numRegions} regions",
                        country.subregion?.takeIf { it.isNotBlank() },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

/** The shapes stamps are inked in, in turn: from Material's shape library, none of them a box. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private val StampShapes = listOf(
    MaterialShapes.Cookie9Sided, MaterialShapes.SoftBurst, MaterialShapes.Clover8Leaf,
    MaterialShapes.Sunny, MaterialShapes.Cookie6Sided, MaterialShapes.Flower,
)

/**
 * The countries visited, as stamps across a passport page: each its code in a shape of its own,
 * inked in one of the theme's colours and pressed on at a slight angle. Tapping one opens it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StampsStrip(countries: List<Country>, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val inks = listOf(
        colors.primary to colors.onPrimary,
        colors.tertiaryContainer to colors.onTertiaryContainer,
        colors.secondaryContainer to colors.onSecondaryContainer,
        colors.primaryContainer to colors.onPrimaryContainer,
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Your stamps", style = AdventureType.display(26.sp))
        // Sized so a fifth stamp shows cut at the edge on a phone: the cut says the row goes on. The
        // cut is the screen's edge, not the page margin, which sliced the stamp as if by mistake.
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.bleedHorizontally(16.dp),
        ) {
            itemsIndexed(countries, key = { _, c -> c.countryCode }) { index, country ->
                val (ink, onInk) = inks[index % inks.size]
                val shape = StampShapes[index % StampShapes.size].toShape()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(76.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClickLabel = "Open") { onClick(country.countryCode) }
                        .semantics(mergeDescendants = true) {}
                        .padding(vertical = 4.dp),
                ) {
                    Box(
                        Modifier
                            .size(70.dp)
                            .graphicsLayer { rotationZ = if (index % 2 == 0) -6f else 5f }
                            .clip(shape)
                            .background(ink),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(country.countryCode, style = AdventureType.stamp().copy(fontSize = 24.sp), color = onInk)
                    }
                    Text(
                        country.name,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
