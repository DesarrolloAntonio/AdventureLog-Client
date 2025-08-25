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
        Spacer(modifier = Modifier.height(24.dp))
        
        // Header with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "🎯",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Visits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

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
        // Timeline indicator on the left
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Top line
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
            }

            // Circle indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Bottom line
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
                // Check if it's a single day visit (All Day)
                val isAllDay = visit.startDate == visit.endDate ||
                              (visit.startDate.contains("T") && visit.endDate.contains("T") &&
                               visit.startDate.split("T").first() == visit.endDate.split("T").first())

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAllDay) {
                        // All Day badge
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
                            text = formatDateDisplay(visit.startDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        // Date range
                        Text(
                            text = "From: ${formatDateDisplay(visit.startDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "To: ${formatDateDisplay(visit.endDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Notes or location info
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
                
                // Location badge if timezone indicates a location
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

// Helper function to format dates for display
private fun formatDateDisplay(dateString: String): String {
    // Keep the incoming format to avoid hardcoded month names.
    // If the string contains a time component (ISO 8601 with 'T'),
    // return only the date part; otherwise return as-is.
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

// Helper function to extract location from timezone
private fun extractLocationFromTimezone(timezone: String): String {
    return when {
        timezone.isEmpty() -> ""
        timezone.contains("/") -> {
            val parts = timezone.split("/")
            parts.lastOrNull()?.replace("_", " ") ?: timezone
        }
        else -> timezone.replace("_", " ")
    }
}
