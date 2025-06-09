package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A reusable loading dialog component that displays a circular progress indicator
 * with an optional message text.
 *
 * @param isLoading Whether to show the loading dialog
 * @param message The message to display below the progress indicator
 * @param modifier Modifier for the root Box
 */
@Composable
fun LoadingDialog(
    isLoading: Boolean,
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.7f)), // Semi-transparent white overlay
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * A variant of LoadingDialog that can display on top of content without a full-screen overlay
 *
 * @param message The message to display below the progress indicator
 * @param showOverlay Whether to show the semi-transparent background overlay
 * @param modifier Modifier for the Card
 */
@Composable
fun LoadingCard(
    message: String = "Loading...",
    showOverlay: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (showOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            LoadingCardContent(message, modifier)
        }
    } else {
        LoadingCardContent(message, modifier)
    }
}

@Composable
private fun LoadingCardContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Previews
@Preview
@Composable
private fun LoadingDialogPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoadingDialog(
                isLoading = true,
                message = "Loading your data..."
            )
        }
    }
}

@Preview
@Composable
private fun LoadingDialogCustomMessagePreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoadingDialog(
                isLoading = true,
                message = "Please wait while we\nprocess your request"
            )
        }
    }
}

@Preview
@Composable
private fun LoadingCardWithoutOverlayPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingCard(
                    message = "Syncing...",
                    showOverlay = false
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoadingCardWithOverlayPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LoadingCard(
                message = "Processing...",
                showOverlay = true
            )
        }
    }
}