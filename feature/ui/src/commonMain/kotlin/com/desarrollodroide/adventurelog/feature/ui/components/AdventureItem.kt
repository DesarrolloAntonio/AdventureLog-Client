package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.di.LocalSessionTokenManager

@Composable
fun AdventureItem(
    modifier: Modifier = Modifier,
    adventure: Adventure,
    onClick: () -> Unit = {},
    onOpenDetails: () -> Unit = { onClick() },
    onEdit: () -> Unit = {},
    onRemoveFromCollection: () -> Unit = {},
    onDelete: () -> Unit = {},
    sessionToken: String = ""
) {
    var showMenu by remember { mutableStateOf(false) }

    // Get dependencies from CompositionLocals
    val imageLoader = LocalImageLoader.current
    val sessionTokenManager = LocalSessionTokenManager.current

    // Update session token if needed (runs once per composition)
    LaunchedEffect(sessionToken) {
        if (sessionToken.isNotEmpty()) {
            sessionTokenManager.updateSessionToken(sessionToken)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Box {
            // Image
            Image(
                painter = rememberAsyncImagePainter(
                    model = adventure.images.firstOrNull()?.image ?: "",
                    imageLoader = imageLoader
                ),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // Overlay with text and tags
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E).copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Text(
                    text = adventure.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category tag
                    adventure.category?.let { category ->
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
                                    text = "${category.displayName} ${category.icon}",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Private tag
                    if (!adventure.isPublic) {
                        Surface(
                            modifier = Modifier.height(24.dp),
                            color = Color(0xFFE91E63),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Private",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
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
                        text = { Text("Open Details") },
                        onClick = {
                            onOpenDetails()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Adventure") },
                        onClick = {
                            onEdit()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove from collection") },
                        onClick = {
                            onRemoveFromCollection()
                            showMenu = false
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = Color(0xFFFF3B30)
                            )
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}