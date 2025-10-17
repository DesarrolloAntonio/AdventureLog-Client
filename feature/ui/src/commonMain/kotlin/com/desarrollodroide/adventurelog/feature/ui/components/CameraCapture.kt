package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.runtime.Composable
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData

@Composable
expect fun CameraCapture(
    onImageCaptured: (ImageFormData) -> Unit,
    onDismiss: () -> Unit
)
