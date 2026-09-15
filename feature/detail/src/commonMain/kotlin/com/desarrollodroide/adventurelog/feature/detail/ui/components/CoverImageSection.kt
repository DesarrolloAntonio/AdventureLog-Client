package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.feature.ui.components.LocationPlaceholder
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader

@Composable
fun CoverImageWithButtons(
    imageUrl: String?,
    adventureName: String = "Adventure",
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    /** Null where this page has nowhere to edit the place from. */
    onEditClick: (() -> Unit)? = null,
    // False when this page is a pane beside the list it came from: there is nothing to go back
    // to, because the list never left.
    showBack: Boolean = true,
    modifier: Modifier = Modifier
) {
    val imageLoader = LocalImageLoader.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                    imageLoader = imageLoader
                ),
                contentDescription = "Adventure image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LocationPlaceholder(
                name = adventureName,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Back button
        if (showBack) Box(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 24.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f))
                .align(Alignment.TopStart)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // Edit and Share. The page had no way to edit the place: the callback existed and was never
        // attached to anything.
        Row(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 24.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onEditClick != null) {
                CoverButton(icon = Icons.Default.Edit, description = "Edit place", onClick = onEditClick)
            }
            CoverButton(icon = Icons.Default.Share, description = "Share", onClick = onShareClick)
        }
    }
}

@Composable
private fun CoverButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.7f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}
