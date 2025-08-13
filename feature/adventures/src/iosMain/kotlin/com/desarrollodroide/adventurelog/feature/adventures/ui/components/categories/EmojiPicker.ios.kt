package com.desarrollodroide.adventurelog.feature.adventures.ui.components.categories

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS implementation of emoji picker
 * TODO: Implement using MCEmojiPicker library
 * https://github.com/izyumkin/MCEmojiPicker
 */
@Composable
actual fun EmojiPicker(
    modifier: Modifier,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // TODO: Implement iOS emoji picker using MCEmojiPicker
    // For now, just show a placeholder
    Text(
        text = "Emoji picker not yet implemented for iOS",
        modifier = modifier
    )
}