package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Visit

@Composable
fun VisitsSection(
    visits: List<Visit>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Visit items with timeline style
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            visits.forEachIndexed { index, visit ->
                VisitItem(
                    visit = visit,
                    isFirst = index == 0,
                    isLast = index == visits.size - 1
                )
            }
        }
    }
}

@Composable
private fun VisitItem(
    visit: Visit,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }
        }

        // Content card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = if (!isLast) 16.dp else 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                visit.startDate?.let { startDate ->
                    val isAllDay = visit.endDate?.let { endDate ->
                        startDate == endDate ||
                                (startDate.contains("T") && endDate.contains("T") &&
                                        startDate.split("T").first() == endDate.split("T").first())
                    } ?: (visit.endDate == null)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isAllDay) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "All Day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatDateDisplay(startDate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "From: ${formatDateDisplay(startDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!isAllDay) {
                        visit.endDate?.let { endDate ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "To: ${formatDateDisplay(endDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                visit.notes?.let { notes ->
                    if (notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                val location = extractLocationFromTimezone(visit.timezone)
                if (location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDateDisplay(dateString: String?): String {
    if (dateString.isNullOrBlank()) {
        return ""
    }
    return try {
        if (dateString.contains("T")) {
            dateString.substringBefore("T")
        } else {
            dateString
        }
    } catch (_: Exception) {
        dateString
    }
}

private fun extractLocationFromTimezone(timezone: String?): String {
    if (timezone.isNullOrBlank()) {
        return ""
    }
    return when {
        timezone.contains("/") -> {
            val parts = timezone.split("/")
            parts.lastOrNull()?.replace("_", " ") ?: timezone
        }
        else -> timezone.replace("_", " ")
    }
}