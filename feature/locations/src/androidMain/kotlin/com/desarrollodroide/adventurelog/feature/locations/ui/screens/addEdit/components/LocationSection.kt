package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.CompactPrimaryButton
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocationSection(
    formData: LocationFormData,
    onFormDataChange: (LocationFormData) -> Unit,
    locationSearchResults: List<GeocodeSearchResult> = emptyList(),
    isSearchingLocation: Boolean = false,
    onSearchLocation: (String) -> Unit = {},
    onClearLocationSearch: () -> Unit = {},
    onReverseGeocode: (Double, Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    
    // Debounce search
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            kotlinx.coroutines.delay(500) // Wait 500ms after user stops typing
            onSearchLocation(searchQuery)
            showSearchResults = true
        } else if (searchQuery.isEmpty()) {
            showSearchResults = false
            onClearLocationSearch()
        }
    }

    SectionCard(
        title = "Where it is",
        icon = Icons.Outlined.LocationOn,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location display field
            StyledTextField(
                value = formData.location,
                onValueChange = {
                    onFormDataChange(formData.copy(location = it))
                },
                label = "Location",
                icon = Icons.Default.LocationOn,
                singleLine = true
            )

            // Search field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    StyledTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            // Debounced search is handled by LaunchedEffect
                        },
                        label = "Search the map",
                        icon = Icons.Default.Search,
                        singleLine = true
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    if (isSearchingLocation) {
                        LoadingIndicator(modifier = Modifier.padding(top = 4.dp))
                    } else {
                        CompactPrimaryButton(
                            onClick = {
                                onSearchLocation(searchQuery)
                                showSearchResults = true
                            },
                            text = "Search",
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Search results: one segmented block, a tap on a row takes that place.
            if (showSearchResults && locationSearchResults.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Search results",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                        locationSearchResults.forEachIndexed { index, result ->
                            Surface(
                                onClick = {
                                    onFormDataChange(
                                        formData.copy(
                                            location = result.displayName,
                                            latitude = result.latitude,
                                            longitude = result.longitude
                                        )
                                    )
                                    searchQuery = ""
                                    showSearchResults = false
                                    onClearLocationSearch()
                                },
                                shape = SegmentedListDefaults.shapeFor(index, locationSearchResults.size),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .heightIn(min = 64.dp)
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    IconTile(Icons.Outlined.Place)
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = result.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = result.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Map
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                LocationMapSection(
                    latitude = formData.latitude,
                    longitude = formData.longitude,
                    onMapClick = { lat, lon ->
                        onFormDataChange(
                            formData.copy(
                                latitude = lat.toString(),
                                longitude = lon.toString()
                            )
                        )
                        // Trigger reverse geocoding to get location name
                        onReverseGeocode(lat, lon)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Coordinates info (optional display)
            if (formData.latitude != null && formData.longitude != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Primary: the pin is set, the row reports something done.
                        IconTile(
                            Icons.Default.LocationOn,
                            container = MaterialTheme.colorScheme.primary,
                            content = MaterialTheme.colorScheme.onPrimary
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Location set",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Latitude: ${formData.latitude}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Longitude: ${formData.longitude}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                onFormDataChange(
                                    formData.copy(
                                        location = "",
                                        latitude = null,
                                        longitude = null
                                    )
                                )
                                searchQuery = ""
                                showSearchResults = false
                                onClearLocationSearch()
                            },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CleaningServices,
                                contentDescription = "Clear location",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A row's leading icon on a 40dp rounded square, as the settings and sort rows carry theirs. */
@Composable
private fun IconTile(
    icon: ImageVector,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    content: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = container,
        contentColor = content,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
}
