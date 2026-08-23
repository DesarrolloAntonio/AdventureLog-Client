package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Link
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField

/**
 * Trails pointing at an external service - AllTrails, Trailforks and the like. The server wants a
 * link on every trail that is not a Wanderer one, so the add button stays disabled without both
 * a name and a link.
 */
@Composable
fun TrailsSection(
    formData: LocationFormData,
    onFormDataChange: (LocationFormData) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

    SectionCard(
        title = "Trails (${formData.trails.size})",
        icon = Icons.Outlined.Hiking,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StyledTextField(
                value = name,
                onValueChange = { name = it },
                label = "Trail name",
                icon = Icons.Outlined.Hiking,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            StyledTextField(
                value = link,
                onValueChange = { link = it },
                label = "External link (AllTrails, Trailforks, ...)",
                icon = Icons.Outlined.Link,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )
            )

            val canAdd = name.isNotBlank() && link.isNotBlank()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        onFormDataChange(
                            formData.copy(
                                trails = formData.trails + TrailFormData(
                                    name = name.trim(),
                                    link = link.trim()
                                )
                            )
                        )
                        name = ""
                        link = ""
                    },
                    enabled = canAdd
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add trail",
                        tint = if (canAdd) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (formData.trails.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                formData.trails.forEachIndexed { index, trail ->
                    TrailRow(
                        trail = trail,
                        onRemove = {
                            onFormDataChange(
                                formData.copy(
                                    trails = formData.trails.filterIndexed { i, _ -> i != index }
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrailRow(
    trail: TrailFormData,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trail.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = trail.link,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove trail",
                    tint = Color(0xFFFF3B30),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
