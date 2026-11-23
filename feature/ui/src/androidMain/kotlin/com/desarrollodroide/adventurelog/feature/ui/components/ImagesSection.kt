package com.desarrollodroide.adventurelog.feature.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType
import com.desarrollodroide.adventurelog.core.domain.usecase.WikipediaImageResult
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.compose.SubcomposeAsyncImage
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import kotlin.collections.plus
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("ImagesSection")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImagesSection(
    images: List<ImageFormData>,
    onImagesChange: (List<ImageFormData>) -> Unit,
    wikipediaImageState: WikipediaImageResult,
    onSearchWikipediaImage: (String) -> Unit,
    onResetWikipediaState: () -> Unit,
    /** Open from the start, for a form reached by "Add photo". */
    initiallyExpanded: Boolean = false
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    var selectedTab by remember { mutableStateOf(0) }
    var urlInput by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var showCameraCapture by remember { mutableStateOf(false) }

    SectionCard(
        title = "Images (${images.size})",
        icon = Icons.Outlined.Image,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Where the image comes from: a connected group, one chosen.
            ImageSourceChoice(
                options = listOf("Upload", "URL", "Wikipedia"),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )

            // Tab content
            when (selectedTab) {
                0 -> {
                    // Upload from device
                    if (images.size >= 10) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Maximum of 10 images reached",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SourceTile(
                                icon = Icons.Outlined.PhotoLibrary,
                                label = "Gallery",
                                onClick = { showImagePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            SourceTile(
                                icon = Icons.Outlined.PhotoCamera,
                                label = "Camera",
                                onClick = { showCameraCapture = true },
                                modifier = Modifier.weight(1f)
                            )
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
                            // Outlined like the form's other fields (StyledTextField).
                            shape = RoundedCornerShape(14.dp),
                            colors = formFieldColors(),
                            isError = urlError != null,
                            supportingText = urlError?.let { { Text(it) } }
                        )

                        PrimaryButton(
                            onClick = {
                                if (urlInput.isNotBlank() && images.size < 10) {
                                    isValidatingUrl = true
                                    urlError = null
                                    // URL validation will be handled in a composable effect
                                }
                            },
                            text = if (isValidatingUrl) "Validating..." else "Add image",
                            enabled = urlInput.isNotBlank() && images.size < 10 && !isValidatingUrl
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
                                            LoadingIndicator()
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
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(MaterialTheme.colorScheme.errorContainer),
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
                                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                                Text(
                                                    text = "Failed to load image",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    },
                                    success = {
                                        LaunchedEffect(Unit) {
                                            val newImage = ImageFormData(
                                                uri = urlInput,
                                                type = ImageType.URL,
                                                isPrimary = images.isEmpty()
                                            )
                                            onImagesChange(images + newImage)
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
                                if (wikipediaImageState !is WikipediaImageResult.Idle) {
                                    onResetWikipediaState()
                                }
                            },
                            label = { Text("Search Wikipedia") },
                            placeholder = { Text("Search for images...") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            // Outlined like the form's other fields (StyledTextField).
                            shape = RoundedCornerShape(14.dp),
                            colors = formFieldColors()
                        )

                        PrimaryButton(
                            onClick = {
                                if (wikipediaQuery.isNotBlank() && images.size < 10) {
                                    onSearchWikipediaImage(wikipediaQuery)
                                }
                            },
                            text = when (wikipediaImageState) {
                                is WikipediaImageResult.Loading -> "Searching..."
                                else -> "Search and add image"
                            },
                            enabled = wikipediaQuery.isNotBlank() &&
                                    images.size < 10 &&
                                    wikipediaImageState !is WikipediaImageResult.Loading
                        )

                        // Handle different states
                        when (wikipediaImageState) {
                            is WikipediaImageResult.Success -> {
                                LaunchedEffect(wikipediaImageState) {
                                    val newImage = ImageFormData(
                                        uri = wikipediaImageState.imageUrl,
                                        type = ImageType.WIKIPEDIA,
                                        isPrimary = images.isEmpty()
                                    )
                                    onImagesChange(images + newImage)
                                    wikipediaQuery = ""
                                    onResetWikipediaState()
                                }
                            }

                            is WikipediaImageResult.NotFound -> {
                                SearchProblem("No image found for \"$wikipediaQuery\"")
                            }

                            is WikipediaImageResult.Error -> {
                                SearchProblem(wikipediaImageState.message)
                            }

                            else -> {}
                        }
                    }
                }
            }

            // Images gallery
            if (images.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selected images (${images.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalArrangement = Arrangement.Top,
                        maxItemsInEachRow = 3
                    ) {
                        images.forEachIndexed { index, image ->
                            ImageItem(
                                image = image,
                                isPrimary = image.isPrimary,
                                onDelete = {
                                    val updatedImages = images.toMutableList().apply {
                                        removeAt(index)
                                    }
                                    if (image.isPrimary && updatedImages.isNotEmpty()) {
                                        updatedImages[0] = updatedImages[0].copy(isPrimary = true)
                                    }
                                    onImagesChange(updatedImages)
                                },
                                onSetPrimary = {
                                    val updatedImages = images.mapIndexed { i, img ->
                                        img.copy(isPrimary = i == index)
                                    }
                                    onImagesChange(updatedImages)
                                }
                            )
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
                onImagesChange(
                    images + imageData.copy(isPrimary = images.isEmpty())
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
                onImagesChange(
                    images + imageData.copy(isPrimary = images.isEmpty())
                )
                showCameraCapture = false
            },
            onDismiss = {
                showCameraCapture = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImageItem(
    image: ImageFormData,
    isPrimary: Boolean,
    onDelete: () -> Unit,
    onSetPrimary: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth(1f / 3f)
            .aspectRatio(1f)
            .padding(end = 8.dp, bottom = 8.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(
                if (isPrimary) {
                    Modifier.border(width = 3.dp, color = MaterialTheme.colorScheme.primary, shape = shape)
                } else {
                    Modifier
                }
            )
            .clickable { onSetPrimary() }
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
                        imageLoader = LocalImageLoader.current,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingIndicator(modifier = Modifier.size(32.dp))
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    )
                }

                ImageType.LOCAL_FILE -> {
                    // For local files
                    val context = LocalPlatformContext.current
                    logger.d { "Loading local image with URI: ${image.uri}" }
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(image.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onError = {
                            logger.e { "Error loading local image: ${image.uri}" }
                        },
                        onSuccess = {
                            logger.d { "Successfully loaded local image: ${image.uri}" }
                        }
                    )
                }
            }

            // Delete button: a small round chip over the photo's corner.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.9f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Primary indicator
            if (isPrimary) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Primary",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/** Gallery or camera: a tile with its icon on a cookie, the way into each source. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SourceTile(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.height(120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            CookieBadge(
                icon = icon,
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
                size = 52.dp,
                polygon = MaterialShapes.Cookie4Sided
            )
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/** Upload, URL or Wikipedia: a connected group of toggle buttons, one chosen. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImageSourceChoice(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = index == selectedIndex,
                onCheckedChange = { if (it) onSelect(index) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(
                    containerColor = colors.surfaceContainerHighest,
                    contentColor = colors.onSurfaceVariant,
                    checkedContainerColor = colors.primary,
                    checkedContentColor = colors.onPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** "No image found" or the search's error, in the error container. */
@Composable
private fun SearchProblem(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Error, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** The form's outlined field colours: no fill, the outline taking the primary colour on focus. */
@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor = MaterialTheme.colorScheme.error
)
