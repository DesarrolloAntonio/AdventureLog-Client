package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlightLand
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.LocationSearchModal
import com.desarrollodroide.adventurelog.feature.ui.components.MapView
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField

@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
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
        title = "Location information",
        icon = Icons.Outlined.LocationOn,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

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
                    label = "From location",
                    icon = Icons.Outlined.FlightTakeoff,
                    modifier = Modifier.weight(1f)
                )
                
                // A tonal button the size of the field, like the date pickers' buttons beside theirs.
                FilledTonalIconButton(
                    onClick = { showFromLocationSearch = true },
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search location")
                }
            }


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
                    label = "To location",
                    icon = Icons.Outlined.FlightLand,
                    modifier = Modifier.weight(1f)
                )
                
                // A tonal button the size of the field, like the date pickers' buttons beside theirs.
                FilledTonalIconButton(
                    onClick = { showToLocationSearch = true },
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search location")
                }
            }

            Text(
                text = "Route map",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                MapView(
                    originLat = formData.originLatitude?.toDoubleOrNull(),
                    originLng = formData.originLongitude?.toDoubleOrNull(),
                    destinationLat = formData.destinationLatitude?.toDoubleOrNull(),
                    destinationLng = formData.destinationLongitude?.toDoubleOrNull(),
                    // The two ends are set by searching above, not by pointing at the map: a tap
                    // cannot say which of the two it means.
                    onMapClick = { _, _ -> },
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
