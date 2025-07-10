package com.desarrollodroide.adventurelog.feature.adventures.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.ImageType

@Composable
actual fun ImagePicker(
    onImageSelected: (ImageFormData) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Take persistent permission for the URI
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Some URIs don't support persistent permissions, that's ok
            }
            
            onImageSelected(
                ImageFormData(
                    uri = uri.toString(),
                    type = ImageType.LOCAL_FILE,
                    isPrimary = false
                )
            )
        } else {
            onDismiss()
        }
    }
    
    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }
}
