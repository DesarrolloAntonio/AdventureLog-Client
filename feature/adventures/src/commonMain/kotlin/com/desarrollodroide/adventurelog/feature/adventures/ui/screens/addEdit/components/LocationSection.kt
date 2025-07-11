package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.AdventureFormData
import com.desarrollodroide.adventurelog.feature.ui.components.CompactPrimaryButton
import kotlinx.coroutines.delay

@Composable
fun LocationSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit,
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
        title = "Location Information",
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
                        label = "Search location",
                        icon = Icons.Default.Search,
                        singleLine = true
                    )
                }
                
                if (searchQuery.isNotEmpty()) {
                    if (isSearchingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 4.dp),
                            strokeWidth = 2.dp
                        )
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
            
            // Search results
            if (showSearchResults && locationSearchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Search Results",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        
                        locationSearchResults.forEach { result ->
                            Card(
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
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = result.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = result.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Location Set",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Latitude: ${formData.latitude}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Longitude: ${formData.longitude}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = {
                                onFormDataChange(formData.copy(
                                    location = "",
                                    latitude = null,
                                    longitude = null
                                ))
                                searchQuery = ""
                                showSearchResults = false
                                onClearLocationSearch()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CleaningServices,
                                contentDescription = "Clear location",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
