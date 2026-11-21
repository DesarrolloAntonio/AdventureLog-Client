package com.desarrollodroide.adventurelog.feature.world.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.feature.ui.components.ChipTone
import com.desarrollodroide.adventurelog.feature.ui.components.MetaChip
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CountryCard(
    country: Country,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visitStatus = when {
        country.numVisits == 0 -> VisitStatus.NOT_VISITED
        country.numVisits == country.numRegions -> VisitStatus.VISITED
        else -> VisitStatus.PARTIAL
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // A row, not a poster. This list is 250 long, and a flag given the full width of the
        // column put one country on the screen at a time. The flag is decoration here; the name
        // and how much of the country has been seen are the content.
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = country.flagUrl,
                contentDescription = "${country.name} flag",
                modifier = Modifier
                    .width(60.dp)
                    .aspectRatio(3f / 2f)
                    .clip(RoundedCornerShape(6.dp))
                    // A hairline edge: a flag that is mostly white (Japan) had no outline on the
                    // white card and read as a red dot floating beside the name.
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val where = listOfNotNull(
                    country.subregion?.takeIf { it.isNotBlank() },
                    country.capital?.takeIf { it.isNotBlank() }
                ).joinToString(" \u00b7 ")
                if (where.isNotEmpty()) {
                    Text(
                        text = where,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            when (visitStatus) {
                // Only the state gets a colour; 250 tinted rows would be noise.
                VisitStatus.VISITED -> MetaChip(
                    text = "${country.numRegions}/${country.numRegions}",
                    tone = ChipTone.POSITIVE
                )
                VisitStatus.PARTIAL -> MetaChip(
                    text = "${country.numVisits}/${country.numRegions}",
                    tone = ChipTone.ACCENT
                )
                VisitStatus.NOT_VISITED -> MetaChip(
                    text = "\u2013",
                    tone = ChipTone.NEUTRAL
                )
            }
        }
    }
}

private enum class VisitStatus {
    VISITED,
    PARTIAL,
    NOT_VISITED
}
