package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Collection

@Composable
fun CollectionItem(
    collection: Collection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (collection.description.isNotEmpty()) {
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Display number of adventures in the collection
            Text(
                text = "${collection.adventures.size} adventures",
                style = MaterialTheme.typography.bodySmall
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Preview of first few adventures in the collection
            if (collection.adventures.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    collection.adventures.take(3).forEach { adventure ->
                        // Find the primary image or use the first one
                        val primaryImage = adventure.images.find { it.isPrimary } ?: adventure.images.firstOrNull()
                        
                        if (primaryImage != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = adventure.images.firstOrNull()?.image ?: ""
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    
                    // If there are more adventures than shown, indicate it
                    if (collection.adventures.size > 3) {
                        Text(
                            text = "+${collection.adventures.size - 3} more",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
