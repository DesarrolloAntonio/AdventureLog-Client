package com.desarrollodroide.adventurelog.feature.adventures.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Category
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AddEditAdventureScreen(
    onNavigateBack: () -> Unit,
    onSave: (adventureData: AdventureFormData) -> Unit,
    modifier: Modifier = Modifier,
    initialData: AdventureFormData? = null
) {
    var formData by remember { 
        mutableStateOf(initialData ?: AdventureFormData()) 
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // Add top spacing
            Spacer(modifier = Modifier.height(16.dp))
            
            // Basic information section
            BasicInfoSection(
                formData = formData,
                onFormDataChange = { formData = it },
                onNavigateBack = onNavigateBack
            )
            
            // Location information section
            LocationSection(
                formData = formData,
                onFormDataChange = { formData = it }
            )
            
            // Tags section
            TagsSection(
                formData = formData,
                onFormDataChange = { formData = it }
            )
            
            // Date information section
            DateSection(
                formData = formData,
                onFormDataChange = { formData = it }
            )
            
            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onSave(formData) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (initialData != null) "Update Adventure" else "Create Adventure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onExpandedChange(!expanded) }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { 
                Text(
                    text = label,
                    color = Color.Gray
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            singleLine = singleLine,
            maxLines = 1,
            keyboardOptions = keyboardOptions,
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            isError = isError,
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else Color.Transparent
            )
        )
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun BasicInfoSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name
                    StyledTextField(
                        value = formData.name,
                        onValueChange = { 
                            onFormDataChange(formData.copy(name = it))
                        },
                        label = "Adventure Name",
                        icon = Icons.Outlined.Title,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    // Category
                    CategoryDropdown(
                        selectedCategory = formData.category,
                        onCategorySelected = {
                            onFormDataChange(formData.copy(category = it))
                        }
                    )
                    
                    // Rating
                    RatingBar(
                        rating = formData.rating,
                        onRatingChanged = {
                            onFormDataChange(formData.copy(rating = it))
                        }
                    )
                    
                    // Link
                    StyledTextField(
                        value = formData.link,
                        onValueChange = { 
                            onFormDataChange(formData.copy(link = it))
                        },
                        label = "Website Link",
                        icon = Icons.Outlined.Link,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    // Description
                    DescriptionField(
                        description = formData.description,
                        onDescriptionChange = {
                            onFormDataChange(formData.copy(description = it))
                        }
                    )
                    
                    // Generate description button
                    OutlinedButton(
                        onClick = { /* TODO: Implement AI description generation */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Description")
                    }
                    
                    // Public adventure switch
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Public Adventure",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Switch(
                                checked = formData.isPublic,
                                onCheckedChange = {
                                    onFormDataChange(formData.copy(isPublic = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDropdown(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    // TODO: Get categories from API
    val categories = listOf(
        Category("1", "restaurant", "Restaurant", "🍽️", 0),
        Category("2", "hotel", "Hotel", "🏨", 0),
        Category("3", "museum", "Museum", "🏛️", 0),
        Category("4", "park", "Park", "🌳", 0),
        Category("5", "beach", "Beach", "🏖️", 0)
    )
    
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

@Composable
private fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    maxRating: Int = 5
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Rating",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 1..maxRating) {
                IconButton(
                    onClick = { onRatingChanged(i) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Rating $i",
                        tint = if (i <= rating) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
        ) {
            Column {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Edit") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Preview") }
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    if (selectedTab == 0) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = onDescriptionChange,
                            placeholder = { 
                                Text(
                                    text = "Write your adventure description...",
                                    color = Color.Gray
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Default
                            ),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        ) {
                            Text(
                                text = if (description.isBlank()) 
                                    "Preview will appear here..." 
                                else 
                                    description,
                                modifier = Modifier.padding(16.dp),
                                color = if (description.isBlank()) 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    SectionCard(
        title = "Location Information",
        icon = Icons.Outlined.LocationOn,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        StyledTextField(
            value = formData.location,
            onValueChange = { 
                onFormDataChange(formData.copy(location = it))
            },
            label = "Location Name",
            icon = Icons.Default.LocationOn
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledTextField(
                value = formData.latitude,
                onValueChange = { 
                    onFormDataChange(formData.copy(latitude = it))
                },
                label = "Latitude",
                icon = Icons.Default.MyLocation,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            
            StyledTextField(
                value = formData.longitude,
                onValueChange = { 
                    onFormDataChange(formData.copy(longitude = it))
                },
                label = "Longitude",
                icon = Icons.Default.MyLocation,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        }
        
        // Add map button
        OutlinedButton(
            onClick = { /* TODO: Open map picker */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pick Location on Map")
        }
    }
}

@Composable
private fun TagsSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    SectionCard(
        title = "Tags (${formData.tags.size})",
        icon = Icons.Outlined.Tag,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🏷️",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Column {
                        Text(
                            text = "Tag functionality coming soon!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Add custom tags to categorize your adventures",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    SectionCard(
        title = "Date Information",
        icon = Icons.Outlined.CalendarToday,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📅",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Column {
                        Text(
                            text = "Date picker coming soon!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Set when you visited this adventure",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Data class for form
data class AdventureFormData(
    val name: String = "",
    val description: String = "",
    val category: Category? = null,
    val rating: Int = 0,
    val link: String = "",
    val location: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val isPublic: Boolean = false,
    val tags: List<String> = emptyList(),
    val date: String? = null
)

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AddEditAdventureScreenPreview() {
    MaterialTheme {
        androidx.compose.material3.Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
                onNavigateBack = {},
                onSave = {}
            )
        }
    }
}

@Preview
@Composable
private fun AddEditAdventureScreenWithDataPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
                onNavigateBack = {},
                onSave = {},
                initialData = AdventureFormData(
                    name = "Visit to Prado Museum",
                    description = "An incredible experience visiting one of the most important art galleries in the world.",
                    category = Category("3", "museum", "Museum", "🏛️", 0),
                    rating = 5,
                    link = "https://www.museodelprado.es",
                    location = "Madrid, Spain",
                    latitude = "40.4138",
                    longitude = "-3.6921",
                    isPublic = true,
                    tags = listOf("art", "culture", "madrid"),
                    date = "2024-01-15"
                )
            )
        }
    }
}

@Preview
@Composable
private fun AddEditAdventureScreenDarkPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        androidx.compose.material3.Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
                onNavigateBack = {},
                onSave = {}
            )
        }
    }
}
