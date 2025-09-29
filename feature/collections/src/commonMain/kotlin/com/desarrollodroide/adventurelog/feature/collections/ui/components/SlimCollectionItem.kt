package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Card
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader

@Composable
fun SlimCollectionItem(
    collection: UltraSlimCollection,
    onClick: () -> Unit,
    onEditCollection: () -> Unit = {},
    onDeleteCollection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val imageLoader = LocalImageLoader.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Box {
            // Use featured image if available
            if (!collection.featuredImage.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = collection.featuredImage,
                        imageLoader = imageLoader
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Simple but nice design for collections without featured image
                EmptyCollectionDesign(
                    collectionName = collection.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
            
            // Overlay with collection information
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
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
                                text = "${collection.adventureCount} ${if (collection.adventureCount == 1) "adventure" else "adventures"}",
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

@Composable
private fun EmptyCollectionDesign(
    collectionName: String,
    modifier: Modifier = Modifier
) {
    // Generate a simple but nice design based on collection name
    val designIndex = remember(collectionName) { 
        kotlin.math.abs(collectionName.hashCode() % 8)
    }
    
    // Predefined safe color combinations
    val (backgroundColor, accentColor) = when (designIndex) {
        0 -> Color(0xFFE8EAF6) to Color(0xFF5C6BC0) // Indigo
        1 -> Color(0xFFE0F2F1) to Color(0xFF26A69A) // Teal
        2 -> Color(0xFFFFF3E0) to Color(0xFFFF9800) // Orange
        3 -> Color(0xFFF3E5F5) to Color(0xFF9C27B0) // Purple
        4 -> Color(0xFFE8F5E9) to Color(0xFF4CAF50) // Green
        5 -> Color(0xFFFFEBEE) to Color(0xFFEF5350) // Red
        6 -> Color(0xFFF1F8E9) to Color(0xFF689F38) // Light Green
        else -> Color(0xFFFAFAFA) to Color(0xFF607D8B) // Blue Grey
    }
    
    Box(modifier = modifier) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.7f),
                            Color.White
                        )
                    )
                )
        )
        
        // Simple geometric decoration - just the grid
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw subtle grid lines
            val gridSize = 100.dp.toPx()
            val gridAlpha = 0.03f
            
            // Vertical lines
            var x = gridSize
            while (x < size.width) {
                drawLine(
                    color = accentColor.copy(alpha = gridAlpha),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                x += gridSize
            }
            
            // Horizontal lines
            var y = gridSize
            while (y < size.height) {
                drawLine(
                    color = accentColor.copy(alpha = gridAlpha),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += gridSize
            }
        }
        
        // Icon positioned higher but avoiding overlay overlap
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 30.dp) // Positioned in upper third to avoid overlay
        ) {
            // White circle background
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Colored background for icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = accentColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    )
                    
                    // Always the same icon - PhotoLibrary
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = accentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
