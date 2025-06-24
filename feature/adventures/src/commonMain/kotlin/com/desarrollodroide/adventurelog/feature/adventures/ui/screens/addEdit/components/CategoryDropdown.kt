package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Category

@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedTextField(
            value = selectedCategory?.let { "${it.icon} ${it.displayName}" } ?: "",
            onValueChange = { },
            placeholder = { 
                Text(
                    text = "Category",
                    color = Color.Gray
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select category",
                    tint = Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Category,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(30.dp))
                .clickable { expanded = true }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { 
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.icon,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
