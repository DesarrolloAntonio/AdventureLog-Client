package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.components

import com.desarrollodroide.adventurelog.feature.ui.components.PublicToggleRow
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.data.CollectionFormData
import com.desarrollodroide.adventurelog.feature.ui.components.DescriptionSection
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField

@Composable
fun BasicInfoSection(
    formData: CollectionFormData,
    onFormDataChange: (CollectionFormData) -> Unit,
    /** The old in-card back arrow; the form's own header has Close now, so callers leave it out. */
    onNavigateBack: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(true) }
    
    SectionCard(
        title = "Collection information",
        icon = Icons.Outlined.Folder,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        leadingContent = onNavigateBack?.let { back -> {
            IconButton(
                onClick = back,
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
                onValueChange = { 
                    onFormDataChange(formData.copy(name = it))
                },
                label = "Collection name",
                icon = Icons.Outlined.Title,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            
            DescriptionSection(
                description = formData.description,
                onDescriptionChange = {
                    onFormDataChange(formData.copy(description = it))
                }
            )
            
            PublicToggleRow(
                title = "Public collection",
                checked = formData.isPublic,
                onCheckedChange = { onFormDataChange(formData.copy(isPublic = it)) },
            )

            StyledTextField(
                value = formData.link,
                onValueChange = { 
                    onFormDataChange(formData.copy(link = it))
                },
                label = "Link",
                icon = Icons.Outlined.Link,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}
