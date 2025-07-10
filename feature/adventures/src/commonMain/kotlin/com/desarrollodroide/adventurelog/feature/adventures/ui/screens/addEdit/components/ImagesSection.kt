package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.AdventureFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageType
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import com.desarrollodroide.adventurelog.feature.adventures.ui.components.ImagePicker
import com.desarrollodroide.adventurelog.feature.adventures.ui.components.CameraCapture
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.WikipediaImageState
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.compose.SubcomposeAsyncImage
import androidx.compose.material3.CircularProgressIndicator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImagesSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit,
    wikipediaImageState: WikipediaImageState,
    onSearchWikipediaImage: (String) -> Unit,
    onResetWikipediaState: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var urlInput by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var showCameraCapture by remember { mutableStateOf(false) }

    SectionCard(
        title = "Images (${formData.images.size})",
        icon = Icons.Outlined.Image,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image source tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Upload") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("URL") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Wikipedia") }
                )
            }

            // Tab content
            when (selectedTab) {
                0 -> {
                    // Upload from device
                    if (formData.images.size >= 10) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Maximum of 10 images reached",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Gallery option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(120.dp)
                                    .clickable { showImagePicker = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    )
                                ),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PhotoLibrary,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "GALLERY",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Camera option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(120.dp)
                                    .clickable { showCameraCapture = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.3f
                                    )
                                ),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PhotoCamera,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "CAMERA",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // URL input
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        var isValidatingUrl by remember { mutableStateOf(false) }
                        var urlError by remember { mutableStateOf<String?>(null) }

                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = {
                                urlInput = it
                                urlError = null
                            },
                            label = { Text("Image URL") },
                            placeholder = { Text("https://example.com/image.jpg") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                ),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            ),
                            isError = urlError != null,
                            supportingText = urlError?.let { { Text(it) } }
                        )

                        PrimaryButton(
                            onClick = {
                                if (urlInput.isNotBlank() && formData.images.size < 10) {
                                    isValidatingUrl = true
                                    urlError = null
                                    // URL validation will be handled in a composable effect
                                }
                            },
                            text = if (isValidatingUrl) "Validating..." else "Add image",
                            enabled = urlInput.isNotBlank() && formData.images.size < 10 && !isValidatingUrl
                        )

                        // URL validation effect
                        if (isValidatingUrl) {
                            val context = LocalPlatformContext.current

                            // Preview image to validate
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(vertical = 8.dp)
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(urlInput)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    },
                                    error = {
                                        LaunchedEffect(Unit) {
                                            urlError = "Failed to load image. Please check the URL."
                                            isValidatingUrl = false
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.errorContainer.copy(
                                                        alpha = 0.1f
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(48.dp),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                                Text(
                                                    text = "Failed to load image",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    },
                                    success = {
                                        LaunchedEffect(Unit) {
                                            val newImage = ImageFormData(
                                                uri = urlInput,
                                                type = ImageType.URL,
                                                isPrimary = formData.images.isEmpty()
                                            )
                                            onFormDataChange(
                                                formData.copy(images = formData.images + newImage)
                                            )
                                            urlInput = ""
                                            isValidatingUrl = false
                                        }
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(urlInput)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Wikipedia search
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        var wikipediaQuery by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = wikipediaQuery,
                            onValueChange = {
                                wikipediaQuery = it
                                if (wikipediaImageState !is WikipediaImageState.Idle) {
                                    onResetWikipediaState()
                                }
                            },
                            label = { Text("Search Wikipedia") },
                            placeholder = { Text("Search for images...") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.3f
                                ),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        PrimaryButton(
                            onClick = {
                                if (wikipediaQuery.isNotBlank() && formData.images.size < 10) {
                                    onSearchWikipediaImage(wikipediaQuery)
                                }
                            },
                            text = when (wikipediaImageState) {
                                is WikipediaImageState.Searching -> "Searching..."
                                else -> "Search and add image"
                            },
                            enabled = wikipediaQuery.isNotBlank() && 
                                     formData.images.size < 10 && 
                                     wikipediaImageState !is WikipediaImageState.Searching
                        )

                        // Handle different states
                        when (wikipediaImageState) {
                            is WikipediaImageState.Success -> {
                                LaunchedEffect(wikipediaImageState) {
                                    val newImage = ImageFormData(
                                        uri = wikipediaImageState.imageUrl,
                                        type = ImageType.WIKIPEDIA,
                                        isPrimary = formData.images.isEmpty()
                                    )
                                    onFormDataChange(
                                        formData.copy(images = formData.images + newImage)
                                    )
                                    wikipediaQuery = ""
                                    onResetWikipediaState()
                                }
                            }
                            
                            is WikipediaImageState.NotFound -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                                            alpha = 0.1f
                                        )
                                    ),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        width = 1.dp,
                                        brush = SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "No image found for \"$wikipediaQuery\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            
                            is WikipediaImageState.Error -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                                            alpha = 0.1f
                                        )
                                    ),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        width = 1.dp,
                                        brush = SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = wikipediaImageState.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            
                            else -> {}
                        }
                    }
                }
            }

            // Images gallery
            if (formData.images.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Selected images (${formData.images.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalArrangement = Arrangement.Top,
                            maxItemsInEachRow = 3
                        ) {
                            formData.images.forEachIndexed { index, image ->
                                ImageItem(
                                    image = image,
                                    isPrimary = image.isPrimary,
                                    onDelete = {
                                        val updatedImages = formData.images.toMutableList().apply {
                                            removeAt(index)
                                        }
                                        if (image.isPrimary && updatedImages.isNotEmpty()) {
                                            updatedImages[0] = updatedImages[0].copy(isPrimary = true)
                                        }
                                        onFormDataChange(formData.copy(images = updatedImages))
                                    },
                                    onSetPrimary = {
                                        val updatedImages = formData.images.mapIndexed { i, img ->
                                            img.copy(isPrimary = i == index)
                                        }
                                        onFormDataChange(formData.copy(images = updatedImages))
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No images",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    // Image picker dialog
    if (showImagePicker) {
        ImagePicker(
            onImageSelected = { imageData ->
                onFormDataChange(
                    formData.copy(
                        images = formData.images + imageData.copy(isPrimary = formData.images.isEmpty())
                    )
                )
                showImagePicker = false
            },
            onDismiss = {
                showImagePicker = false
            }
        )
    }
    
    // Camera capture dialog
    if (showCameraCapture) {
        CameraCapture(
            onImageCaptured = { imageData ->
                onFormDataChange(
                    formData.copy(
                        images = formData.images + imageData.copy(isPrimary = formData.images.isEmpty())
                    )
                )
                showCameraCapture = false
            },
            onDismiss = {
                showCameraCapture = false
            }
        )
    }
}

@Composable
private fun ImageItem(
    image: ImageFormData,
    isPrimary: Boolean,
    onDelete: () -> Unit,
    onSetPrimary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(1f / 3f)
            .aspectRatio(1f)
            .padding(end = 8.dp, bottom = 8.dp)
            .clickable { onSetPrimary() }
            .then(
                if (isPrimary) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Image preview
            when (image.type) {
                ImageType.URL, ImageType.WIKIPEDIA -> {
                    val context = LocalPlatformContext.current
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

                ImageType.LOCAL_FILE -> {
                    // For local files
                    val context = LocalPlatformContext.current
                    println("Loading local image with URI: ${image.uri}")
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onError = {
                            println("Error loading local image: ${image.uri}")
                        },
                        onSuccess = {
                            println("Successfully loaded local image: ${image.uri}")
                        }
                    )
                }
            }

            // Delete button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomStart = 12.dp)
                    )
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Primary indicator
            if (isPrimary) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topEnd = 12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Primary",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
