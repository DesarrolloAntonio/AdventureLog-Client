package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

data class CollectionFormData(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCollectionScreen(
    onNavigateBack: () -> Unit,
    onSave: (CollectionFormData) -> Unit,
    modifier: Modifier = Modifier,
    initialData: CollectionFormData? = null
) {
    var formData by remember { 
        mutableStateOf(initialData ?: CollectionFormData()) 
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (initialData != null) "Edit collection" else "New collection",
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
            // Name
            OutlinedTextField(
                value = formData.name,
                onValueChange = { 
                    formData = formData.copy(name = it)
                },
                label = { Text("Collection name*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            
            // Description
            OutlinedTextField(
                value = formData.description,
                onValueChange = { 
                    formData = formData.copy(description = it)
                },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )
            
            // Public collection switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Public collection",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Other users will be able to see this collection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = formData.isPublic,
                    onCheckedChange = {
                        formData = formData.copy(isPublic = it)
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        if (formData.name.isNotBlank()) {
                            onSave(formData)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = formData.name.isNotBlank()
                ) {
                    Text("Save")
                }
                
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
