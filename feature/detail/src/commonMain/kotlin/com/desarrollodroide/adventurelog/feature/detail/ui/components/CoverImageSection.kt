package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.jetbrains.compose.resources.painterResource
import com.desarrollodroide.adventurelog.resources.Res
import com.desarrollodroide.adventurelog.resources.adventureitem_placeholder

@Composable
fun CoverImageWithButtons(
    imageUrl: String?,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageLoader = LocalImageLoader.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Use adventure placeholder when no image
        if (imageUrl != null) {
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
            Image(
                painter = painterResource(Res.drawable.adventureitem_placeholder),
                contentDescription = "Adventure placeholder",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Back button
        Box(
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

        // Share button
        Box(
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 24.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f))
                .align(Alignment.TopEnd)
                .clickable { onShareClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
