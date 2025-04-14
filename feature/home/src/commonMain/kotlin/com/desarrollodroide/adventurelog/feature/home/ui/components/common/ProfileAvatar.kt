package com.desarrollodroide.adventurelog.feature.home.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.rememberAsyncImagePainter

/**
 * A circular profile avatar component that displays a user profile image or a fallback icon
 * Can be used in topbar, drawer header, or profile screens
 */
@Composable
fun ProfileAvatar(
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    size: Int = 40,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
            // Use the same approach as in AdventureItem to load images
            androidx.compose.foundation.Image(
                painter = rememberAsyncImagePainter(
                    model = profileImageUrl
                ),
                contentDescription = "User profile",
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // If there is no URL or it is empty, show the default icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User profile",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size((size * 0.6).dp)
            )
        }
    }
}