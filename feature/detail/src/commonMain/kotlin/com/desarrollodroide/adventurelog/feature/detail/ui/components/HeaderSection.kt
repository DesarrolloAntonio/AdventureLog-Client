package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.ui.components.TagChip
import kotlin.math.floor

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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (!location.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        rating?.let {
            Spacer(modifier = Modifier.height(8.dp))
            RatingBar(rating = it)
        }
    }
}

@Composable
fun RatingBar(
    rating: Double,
    maxRating: Int = 5,
    modifier: Modifier = Modifier
) {
    val fullStars = floor(rating).toInt()
    val hasHalfStar = rating - fullStars >= 0.5
    val emptyStars = maxRating - fullStars - if (hasHalfStar) 1 else 0

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Full star",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        }
        
        if (hasHalfStar) {
            Box {
                Icon(
                    imageVector = Icons.Outlined.StarBorder,
                    contentDescription = "Empty star",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Half star",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Outlined.StarBorder,
                contentDescription = "Empty star",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
        }
    }
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
            TagChip(
                text = "${it.icon} ${it.displayName}",
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        if (!isPublic) {
            TagChip(
                text = "🔒 Private",
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        tags.forEach { tag ->
            TagChip(
                text = tag,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
