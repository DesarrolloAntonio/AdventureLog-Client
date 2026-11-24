package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField
import com.desarrollodroide.adventurelog.feature.ui.components.RatingBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicInfoTransportationSection(
    formData: TransportationFormData,
    transportationTypes: List<String>,
    onFormDataChange: (TransportationFormData) -> Unit,
    /** The old in-card back arrow; the form's own header has Close now, so callers leave it out. */
    onNavigateBack: (() -> Unit)? = null,
    onGenerateDescription: () -> Unit,
    isGeneratingDescription: Boolean
) {
    var expanded by remember { mutableStateOf(true) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    SectionCard(
        title = "Basic information",
        icon = Icons.Outlined.Info,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        leadingContent = onNavigateBack?.let { back -> {
            IconButton(
                onClick = back,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        } }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = formData.name,
                onValueChange = { onFormDataChange(formData.copy(name = it)) },
                label = "Transportation name",
                icon = Icons.Outlined.Title
            )

            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    value = getTransportationDisplayName(formData.type),
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    transportationTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(getTransportationIcon(type))
                                    Text(getTransportationDisplayName(type))
                                }
                            },
                            onClick = {
                                onFormDataChange(formData.copy(type = type))
                                typeDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            if (formData.type == "Plane") {
                StyledTextField(
                    value = formData.flightNumber,
                    onValueChange = { onFormDataChange(formData.copy(flightNumber = it)) },
                    label = "Flight number",
                    icon = Icons.Outlined.Flight
                )
            }

            StyledTextField(
                value = formData.link,
                onValueChange = { onFormDataChange(formData.copy(link = it)) },
                label = "Link (optional)",
                icon = Icons.Outlined.Link,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = onGenerateDescription,
                        enabled = formData.name.isNotBlank() && !isGeneratingDescription,
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text(text = if (isGeneratingDescription) "Generating..." else "Generate")
                    }
                }

                OutlinedTextField(
                    value = formData.description,
                    onValueChange = { onFormDataChange(formData.copy(description = it)) },
                    placeholder = { Text("Enter description...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    // No leading icon: in a multi-line field Material centres it vertically, far from the text.
                    minLines = 3,
                    maxLines = 5
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // RatingBar carries its own "Rating" heading; a second one sat above it.
                RatingBar(
                    rating = formData.rating,
                    onRatingChanged = { onFormDataChange(formData.copy(rating = it)) }
                )
            }
        }
    }
}

private fun getTransportationIcon(type: String): String {
    return when (type.lowercase()) {
        "car" -> "🚗"
        "plane" -> "✈️"
        "train" -> "🚆"
        "bus" -> "🚌"
        "boat" -> "⛵"
        "bike" -> "🚴"
        "walking" -> "🚶"
        "other" -> "🚐"
        else -> "🚗"
    }
}

private fun getTransportationDisplayName(type: String): String {
    return when (type.lowercase()) {
        "car" -> "Car"
        "plane" -> "Plane"
        "train" -> "Train"
        "bus" -> "Bus"
        "boat" -> "Boat"
        "bike" -> "Bike"
        "walking" -> "Walking"
        "other" -> "Other"
        else -> type.replaceFirstChar { it.uppercase() }
    }
}
