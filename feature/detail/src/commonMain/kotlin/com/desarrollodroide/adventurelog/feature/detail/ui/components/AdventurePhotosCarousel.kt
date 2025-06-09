package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil3.compose.rememberAsyncImagePainter
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.core.model.AdventureImage
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader

/**
 * Adventure photos carousel with fullscreen view and menu options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventurePhotosCarousel(
    images: List<AdventureImage>,
    onAddPhoto: (() -> Unit)? = null, // Optional for now
    onDeletePhoto: ((AdventureImage) -> Unit)? = null, // Optional for now
    modifier: Modifier = Modifier
) {
    val imageLoader = LocalImageLoader.current
    var showFullscreen by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    
    Column(modifier = modifier) {
        // Header with title and add button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Photos (${images.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Add button - only show if callback is provided
            onAddPhoto?.let {
                IconButton(
                    onClick = it,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Carousel
        val carouselState = rememberCarouselState { images.size }

        HorizontalMultiBrowseCarousel(
            state = carouselState,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            preferredItemWidth = 120.dp,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) { index ->
            val image = images[index]
            
            PhotoCarouselItem(
                image = image,
                index = index,
                imageLoader = imageLoader,
                onClick = {
                    selectedImageIndex = index
                    showFullscreen = true
                },
                onMenuClick = onDeletePhoto?.let { deleteCallback ->
                    { deleteCallback(image) }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .maskClip(RoundedCornerShape(16.dp))
            )
        }
    }
    
    // Fullscreen viewer
    if (showFullscreen) {
        FullscreenImageViewer(
            images = images,
            initialIndex = selectedImageIndex,
            onDismiss = { showFullscreen = false },
            onDeletePhoto = onDeletePhoto
        )
    }
}

/**
 * Individual carousel item with optional menu
 */
@Composable
private fun PhotoCarouselItem(
    image: AdventureImage,
    index: Int,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = image.image,
                imageLoader = imageLoader
            ),
            contentDescription = "Adventure image ${index + 1}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Primary badge
        if (image.isPrimary) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            ) {
                Text(
                    text = "Primary",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        
        // Menu button - only show if callback is provided
        onMenuClick?.let {
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                            .padding(2.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onMenuClick()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * State for managing zoom and pan for each image
 */
@Stable
private class ZoomableImageState {
    var scale by mutableFloatStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    
    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }
}

/**
 * Fullscreen image viewer with zoom support
 */
@Composable
private fun FullscreenImageViewer(
    images: List<AdventureImage>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onDeletePhoto: ((AdventureImage) -> Unit)? = null
) {
    val imageLoader = LocalImageLoader.current
    val selectedImage = images[initialIndex]
    var showControls by remember { mutableStateOf(true) }
    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0f,
        label = "controls alpha"
    )
    
    // Create zoom state for the image
    val zoomState = remember { ZoomableImageState() }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Zoomable image
            ZoomableImage(
                imageUrl = selectedImage.image,
                imageLoader = imageLoader,
                contentDescription = "Fullscreen image",
                zoomState = zoomState,
                onTap = { showControls = !showControls }
            )
            
            // Top bar with controls
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = controlsAlpha },
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .statusBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Close button
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                        
                        // Spacer to center title
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Delete button - only show if callback is provided
                        if (onDeletePhoto != null) {
                            IconButton(
                                onClick = {
                                    onDeletePhoto(selectedImage)
                                    onDismiss()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Zoomable image component with pinch-to-zoom and pan support
 */
@Composable
private fun ZoomableImage(
    imageUrl: String,
    imageLoader: ImageLoader,
    contentDescription: String,
    zoomState: ZoomableImageState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        // Double tap to zoom
                        if (zoomState.scale > 1f) {
                            // Reset zoom
                            zoomState.reset()
                        } else {
                            // Zoom to 2.5x at tap position
                            zoomState.scale = 2.5f
                            // Calculate offset to center on tap position
                            val imageCenter = Offset(size.width / 2f, size.height / 2f)
                            zoomState.offset = (imageCenter - tapOffset) * (zoomState.scale - 1f)
                        }
                    },
                    onTap = { onTap() }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Update scale
                    val newScale = (zoomState.scale * zoom).coerceIn(1f, 5f)
                    
                    // Calculate new offset
                    if (newScale == 1f) {
                        // Reset offset when scale is 1
                        zoomState.offset = Offset.Zero
                    } else {
                        // Apply pan with scale
                        val maxX = (size.width * (newScale - 1f)) / 2f
                        val maxY = (size.height * (newScale - 1f)) / 2f
                        
                        zoomState.offset = Offset(
                            x = (zoomState.offset.x + pan.x).coerceIn(-maxX, maxX),
                            y = (zoomState.offset.y + pan.y).coerceIn(-maxY, maxY)
                        )
                    }
                    
                    zoomState.scale = newScale
                }
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                imageLoader = imageLoader
            ),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomState.scale
                    scaleY = zoomState.scale
                    translationX = zoomState.offset.x
                    translationY = zoomState.offset.y
                }
        )
    }
}
