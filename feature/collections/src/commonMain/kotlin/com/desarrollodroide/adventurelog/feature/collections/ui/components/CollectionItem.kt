package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Collection

@Composable
fun CollectionItem(
    collection: Collection,
    onClick: () -> Unit,
    onEditCollection: () -> Unit = {},
    onDeleteCollection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Box {
            // If there are adventures with images, use the first one as the collection background
            if (collection.adventures.isNotEmpty() && collection.adventures.any { it.images.isNotEmpty() }) {
                val adventure = collection.adventures.first { it.images.isNotEmpty() }
                val primaryImage = adventure.images.find { it.isPrimary } ?: adventure.images.first()
                
                Image(
                    painter = rememberAsyncImagePainter(
                        model = primaryImage.image
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder colored background if no images available
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
            
            // Overlay with collection information
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E).copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Text(
                    text = collection.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (collection.description.isNotEmpty()) {
                    Text(
                        text = collection.description,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tag showing number of adventures
                    Surface(
                        modifier = Modifier.height(24.dp),
                        color = Color(0xFF6B4EFF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${collection.adventures.size} adventures",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Preview of adventure thumbnails if there are any
                    if (collection.adventures.isNotEmpty()) {
                        val thumbnailsToShow = collection.adventures
                            .filter { it.images.isNotEmpty() }
                            .take(3)
                        
                        for (adventure in thumbnailsToShow) {
                            val image = adventure.images.first().image
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = image),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        // If there are more adventures than shown, indicate it
                        if (collection.adventures.size > 3) {
                            Text(
                                text = "+${collection.adventures.size - 3}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            
            // Menu button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View Collection") },
                        onClick = {
                            onClick()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Collection") },
                        onClick = {
                            onEditCollection()
                            showMenu = false
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete Collection",
                                color = Color(0xFFFF3B30)
                            )
                        },
                        onClick = {
                            onDeleteCollection()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}