package com.desarrollodroide.adventurelog.feature.collections.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
import com.desarrollodroide.adventurelog.feature.collections.ui.state.CollectionItinerary
import com.desarrollodroide.adventurelog.feature.collections.ui.state.ItineraryDay
import com.desarrollodroide.adventurelog.feature.collections.ui.state.ItineraryItem

/**
 * The trip laid out day by day.
 *
 * Unlike the Calendar view, which shows only what has a date on it, this shows *every* day of the
 * trip window - including the empty ones, because an empty Thursday in the middle of a fortnight
 * is information: it is the thing you are looking at the itinerary to find.
 */
@Composable
fun CollectionItineraryView(
    itinerary: CollectionItinerary,
    isWorking: Boolean,
    onAutoGenerate: () -> Unit,
    onAddToDay: (date: String?, label: String) -> Unit,
    onRemoveEntry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (itinerary.days.isEmpty()) {
            NoDatesCard()
            return@Column
        }

        if (itinerary.isEmpty && itinerary.canAutoGenerate) {
            AutoGenerateCard(isWorking = isWorking, onAutoGenerate = onAutoGenerate)
        }

        TripContextCard(
            items = itinerary.tripContext,
            onAdd = { onAddToDay(null, "trip context") },
            onRemove = onRemoveEntry
        )

        itinerary.days.forEach { day ->
            DayCard(
                day = day,
                onAdd = { onAddToDay(day.date.toString(), day.label) },
                onRemove = onRemoveEntry
            )
        }
    }
}

@Composable
private fun AutoGenerateCard(isWorking: Boolean, onAutoGenerate: () -> Unit) {
    ItineraryCard {
        Text(
            text = "Build it from the dates you already have",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "This collection has dated records but nothing on the itinerary yet. " +
                "They can be laid out across the days of the trip in one go.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onAutoGenerate, enabled = !isWorking) {
            if (isWorking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Build the itinerary")
            }
        }
    }
}

@Composable
private fun TripContextCard(
    items: List<ItineraryItem>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    ItineraryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trip context",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Belongs to the trip, not to one day of it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AddButton(onAdd)
        }
        if (items.isEmpty()) {
            EmptyLine("Nothing here yet")
        } else {
            items.forEach { ItemRow(it, onRemove) }
        }
    }
}

@Composable
private fun DayCard(day: ItineraryDay, onAdd: () -> Unit, onRemove: (String) -> Unit) {
    ItineraryCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(52.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = weekdayName(day.date.dayOfWeek.ordinal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = monthName(day.date.monthNumber).take(3),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Day ${day.dayNumber} of ${day.totalDays} · " +
                        if (day.items.size == 1) "1 item" else "${day.items.size} items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AddButton(onAdd)
        }

        if (day.description.isNotBlank()) {
            Text(
                text = day.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (day.items.isEmpty()) {
            EmptyLine("Nothing planned for this day")
        } else {
            day.items.forEach { ItemRow(it, onRemove) }
        }
    }
}

@Composable
private fun ItemRow(item: ItineraryItem, onRemove: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = when (item.kind) {
                ItineraryItemKind.LOCATION, ItineraryItemKind.VISIT -> Icons.Default.Place
                ItineraryItemKind.TRANSPORTATION -> Icons.Default.Flight
                ItineraryItemKind.LODGING -> Icons.Default.Hotel
                ItineraryItemKind.NOTE -> Icons.AutoMirrored.Filled.Notes
                ItineraryItemKind.CHECKLIST -> Icons.Default.Checklist
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            item.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = { onRemove(item.entryId) }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Take ${item.title} off this day",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddButton(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clickable(onClick = onAdd),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add something here",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 14.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NoDatesCard() {
    ItineraryCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "This trip has no dates",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "An itinerary is a set of days. Give the collection a start and an end " +
                    "date and they appear here.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ItineraryCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

private fun monthName(month: Int): String = when (month) {
    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
    9 -> "September"; 10 -> "October"; 11 -> "November"; else -> "December"
}

private fun weekdayName(ordinal: Int): String = when (ordinal) {
    0 -> "Mon"; 1 -> "Tue"; 2 -> "Wed"; 3 -> "Thu"
    4 -> "Fri"; 5 -> "Sat"; else -> "Sun"
}
