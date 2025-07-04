package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Wrapper component that dismisses the keyboard when clicking outside of text fields
 */
@Composable
fun KeyboardDismissible(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            focusManager.clearFocus()
        }
    ) {
        content()
    }
}
