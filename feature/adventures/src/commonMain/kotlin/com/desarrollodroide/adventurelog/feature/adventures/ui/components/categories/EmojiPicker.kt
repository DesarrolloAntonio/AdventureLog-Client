package com.desarrollodroide.adventurelog.feature.adventures.ui.components.categories

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific emoji picker interface
 */
@Composable
expect fun EmojiPicker(
    modifier: Modifier = Modifier,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
)