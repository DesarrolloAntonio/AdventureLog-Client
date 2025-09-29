package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionsSection(
    collections: List<UltraSlimCollection>,
    onCollectionClick: (UltraSlimCollection) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = if (collections.size == 1) "Collection" else "Collections",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            collections.forEach { collection ->
                AssistChip(
                    onClick = { onCollectionClick(collection) },
                    label = { 
                        Text(
                            text = collection.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun CollectionsSectionPreview() {
    val mockCollections = PreviewData.sampleUltraSlimCollections.take(3)
    
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            CollectionsSection(
                collections = mockCollections,
                onCollectionClick = { }
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun CollectionsSectionSinglePreview() {
    val mockCollections = listOf(PreviewData.sampleUltraSlimCollections[3])
    
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            CollectionsSection(
                collections = mockCollections,
                onCollectionClick = { }
            )
        }
    }
}
