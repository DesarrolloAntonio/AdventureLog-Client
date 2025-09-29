package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlightLand
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.LocationSearchModal
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.SectionCard
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.StyledTextField
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.MapView

@Composable
fun LocationTransportationSection(
    formData: TransportationFormData,
    onFormDataChange: (TransportationFormData) -> Unit,
    locationSearchResults: List<GeocodeSearchResult>,
    isSearchingLocation: Boolean,
    onSearchLocation: (String) -> Unit,
    onClearLocationSearch: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showFromLocationSearch by remember { mutableStateOf(false) }
    var showToLocationSearch by remember { mutableStateOf(false) }

    SectionCard(
        title = "Location Information",
        icon = Icons.Outlined.LocationOn,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "From Location",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StyledTextField(
                    value = formData.fromLocation,
                    onValueChange = { 
                        onFormDataChange(formData.copy(fromLocation = it))
                    },
                    label = "From Location",
                    icon = Icons.Outlined.FlightTakeoff,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { showFromLocationSearch = true }
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "To Location",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StyledTextField(
                    value = formData.toLocation,
                    onValueChange = { 
                        onFormDataChange(formData.copy(toLocation = it))
                    },
                    label = "To Location",
                    icon = Icons.Outlined.FlightLand,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { showToLocationSearch = true }
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Route Map",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                MapView(
                    originLat = formData.originLatitude?.toDoubleOrNull(),
                    originLng = formData.originLongitude?.toDoubleOrNull(),
                    destinationLat = formData.destinationLatitude?.toDoubleOrNull(),
                    destinationLng = formData.destinationLongitude?.toDoubleOrNull(),
                    onMapClick = { lat, lng ->
                        // Handle map click if needed
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showFromLocationSearch) {
        LocationSearchModal(
            searchResults = locationSearchResults,
            isSearching = isSearchingLocation,
            onSearch = onSearchLocation,
            onLocationSelected = { result ->
                onFormDataChange(
                    formData.copy(
                        fromLocation = result.displayName,
                        originLatitude = result.latitude,
                        originLongitude = result.longitude
                    )
                )
                showFromLocationSearch = false
                onClearLocationSearch()
            },
            onDismiss = {
                showFromLocationSearch = false
                onClearLocationSearch()
            }
        )
    }

    if (showToLocationSearch) {
        LocationSearchModal(
            searchResults = locationSearchResults,
            isSearching = isSearchingLocation,
            onSearch = onSearchLocation,
            onLocationSelected = { result ->
                onFormDataChange(
                    formData.copy(
                        toLocation = result.displayName,
                        destinationLatitude = result.latitude,
                        destinationLongitude = result.longitude
                    )
                )
                showToLocationSearch = false
                onClearLocationSearch()
            },
            onDismiss = {
                showToLocationSearch = false
                onClearLocationSearch()
            }
        )
    }
}
