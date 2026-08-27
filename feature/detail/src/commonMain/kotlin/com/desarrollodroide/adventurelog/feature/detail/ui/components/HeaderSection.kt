package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.ui.components.ChipTone
import com.desarrollodroide.adventurelog.feature.ui.components.MetaChip
import kotlin.math.floor
import kotlin.math.roundToInt

private val RatingGold = Color(0xFFF5B301)

/**
 * The name of the place, where it is, and what it was worth.
 *
 * Place and rating share a line: they are both one short fact about the same thing, and stacking
 * them pushed the first real content a third of the way down the page.
 */
@Composable
fun HeaderInfo(
    title: String,
    location: String?,
    rating: Double? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            lineHeight = MaterialTheme.typography.headlineSmall.fontSize * 1.2
        )

        val hasPlace = !location.isNullOrBlank()
        val hasRating = rating != null && rating > 0

        if (hasPlace || hasRating) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hasPlace) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = location.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (hasPlace && hasRating) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                }
                if (hasRating) {
                    RatingBar(rating = rating!!)
                }
            }
        }
    }
}

/**
 * Stars, and the number they add up to. The stars alone made a four and a five hard to tell apart
 * at a glance, and a half star impossible.
 */
@Composable
fun RatingBar(
    rating: Double,
    maxRating: Int = 5,
    modifier: Modifier = Modifier
) {
    val fullStars = floor(rating).toInt().coerceIn(0, maxRating)
    val hasHalfStar = rating - fullStars >= 0.5 && fullStars < maxRating
    val emptyStars = maxRating - fullStars - if (hasHalfStar) 1 else 0

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = RatingGold,
                modifier = Modifier.size(18.dp)
            )
        }
        if (hasHalfStar) {
            Box {
                Icon(
                    imageVector = Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = RatingGold,
                    modifier = Modifier.size(18.dp)
                )
                Box(modifier = Modifier.size(18.dp).fillMaxWidth(0.5f)) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = RatingGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = RatingGold.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = formatRating(rating),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** "4" rather than "4.0", but "4.5" when the half matters. */
private fun formatRating(rating: Double): String {
    val rounded = (rating * 10).roundToInt()
    return if (rounded % 10 == 0) (rounded / 10).toString() else "${rounded / 10}.${rounded % 10}"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryTags(
    category: Category?,
    isPublic: Boolean,
    tags: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        category?.let {
            MetaChip(text = "${it.icon} ${it.displayName}", tone = ChipTone.ACCENT)
        }
        if (!isPublic) {
            MetaChip(text = "🔒 Private", tone = ChipTone.WARNING)
        }
        tags.forEach { tag ->
            MetaChip(text = tag, tone = ChipTone.NEUTRAL)
        }
    }
}
