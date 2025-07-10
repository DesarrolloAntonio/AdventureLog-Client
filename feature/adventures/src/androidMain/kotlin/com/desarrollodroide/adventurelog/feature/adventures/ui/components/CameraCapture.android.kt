package com.desarrollodroide.adventurelog.feature.adventures.ui.components

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
actual fun CameraCapture(
    onImageCaptured: (ImageFormData) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val photoUri = remember { createImageFileUri(context) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImageCaptured(
                ImageFormData(
                    uri = photoUri.toString(),
                    type = ImageType.LOCAL_FILE,
                    isPrimary = false
                )
            )
        } else {
            onDismiss()
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            hasCameraPermission = true
            cameraLauncher.launch(photoUri)
        } else {
            showPermissionDialog = true
        }
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDialog = false
                onDismiss()
            },
            title = { Text(text = "Camera Permission Required") },
            text = { Text(text = "This app needs camera permission to take photos. Please grant the permission in app settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

private fun createImageFileUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = File(context.cacheDir, "images").apply {
        if (!exists()) mkdirs()
    }
    val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
    
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}
