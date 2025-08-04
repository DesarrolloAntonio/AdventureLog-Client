package com.desarrollodroide.adventurelog.feature.world.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.desarrollodroide.adventurelog.feature.ui.components.TagChip

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
        shape = RoundedCornerShape(26.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box {
            // Flag image
            AsyncImage(
                model = country.flagUrl,
                contentDescription = "${country.name} flag",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay and content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
                    .padding(16.dp)
            ) {
                // Country name
                Text(
                    text = country.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tags row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Subregion tag
                    country.subregion?.let { subregion ->
                        TagChip(
                            text = subregion,
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Capital tag
                    country.capital?.let { capital ->
                        TagChip(
                            text = capital,
                            backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    }

                    // Visit status tag
                    when (visitStatus) {
                        VisitStatus.VISITED -> TagChip(
                            text = "Visited ${country.numVisits} Region${if (country.numVisits > 1) "s" else ""}",
                            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.9f),
                            contentColor = Color.White
                        )
                        VisitStatus.PARTIAL -> TagChip(
                            text = "Visited ${country.numVisits}/${country.numRegions}",
                            backgroundColor = Color(0xFFFFA726).copy(alpha = 0.9f),
                            contentColor = Color.White
                        )
                        VisitStatus.NOT_VISITED -> TagChip(
                            text = "Not visited",
                            backgroundColor = Color(0xFFFF5252).copy(alpha = 0.9f),
                            contentColor = Color.White
                        )
                    }
                }
            }
        }
    }
}

private enum class VisitStatus {
    VISITED,
    PARTIAL,
    NOT_VISITED
}