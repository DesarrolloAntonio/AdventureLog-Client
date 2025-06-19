package com.desarrollodroide.adventurelog.feature.adventures.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Category

@OptIn(ExperimentalMaterial3Api::class)
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
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (initialData != null) "Edit adventure" else "New adventure",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic information section
            BasicInfoSection(
                formData = formData,
                onFormDataChange = { formData = it }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onSave(formData) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save and Next")
                }
                
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BasicInfoSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Basic information",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
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
                    OutlinedTextField(
                        value = formData.name,
                        onValueChange = { 
                            onFormDataChange(formData.copy(name = it))
                        },
                        label = { Text("Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
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
                    OutlinedTextField(
                        value = formData.link,
                        onValueChange = { 
                            onFormDataChange(formData.copy(link = it))
                        },
                        label = { Text("Link") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
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
                    TextButton(
                        onClick = { /* TODO: Implement AI description generation */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate description")
                    }
                    
                    // Public adventure switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Public adventure")
                        Switch(
                            checked = formData.isPublic,
                            onCheckedChange = {
                                onFormDataChange(formData.copy(isPublic = it))
                            }
                        )
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
            value = selectedCategory?.displayName ?: "",
            onValueChange = { },
            label = { Text("Category*") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select category"
                )
            }
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.icon)
                            Text(category.displayName)
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
        Text(
            text = "Rating",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..maxRating) {
                IconButton(
                    onClick = { onRatingChanged(i) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Rating $i",
                        tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent
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
                .padding(top = 8.dp)
        ) {
            if (selectedTab == 0) {
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    placeholder = { Text("Write here with markdown...") },
                    modifier = Modifier.fillMaxSize(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Default
                    )
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (description.isBlank()) "Markdown preview..." else description,
                        modifier = Modifier.padding(16.dp),
                        color = if (description.isBlank()) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Location information",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = formData.location,
                    onValueChange = { 
                        onFormDataChange(formData.copy(location = it))
                    },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = formData.latitude,
                        onValueChange = { 
                            onFormDataChange(formData.copy(latitude = it))
                        },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    
                    OutlinedTextField(
                        value = formData.longitude,
                        onValueChange = { 
                            onFormDataChange(formData.copy(longitude = it))
                        },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TagsSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tags (${formData.tags.size})",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // TODO: Implement tags input
                Text(
                    text = "Tags functionality to be implemented",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Date information",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // TODO: Implement date picker
                Text(
                    text = "Date picker to be implemented",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun AddEditAdventureScreenWithDataPreview() {
    MaterialTheme {
        androidx.compose.material3.Surface(
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

@org.jetbrains.compose.ui.tooling.preview.Preview
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
