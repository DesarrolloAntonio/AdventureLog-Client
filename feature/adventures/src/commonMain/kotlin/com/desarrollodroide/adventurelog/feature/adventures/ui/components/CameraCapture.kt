package com.desarrollodroide.adventurelog.feature.adventures.ui.components

import androidx.compose.runtime.Composable
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageFormData

@Composable
expect fun CameraCapture(
    onImageCaptured: (ImageFormData) -> Unit,
    onDismiss: () -> Unit
)
