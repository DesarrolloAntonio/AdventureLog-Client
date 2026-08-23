package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import platform.UIKit.*
import platform.Foundation.*
// NSObject lives in platform.darwin, not Foundation - the wildcard imports above never brought it in.
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ImagePicker(
    onImageSelected: (ImageFormData) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val imagePickerController = remember { 
        UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            allowsEditing = false
        }
    }
    
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                val imageUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
                
                scope.launch {
                    if (image != null) {
                        val tempUrl = saveImageToTemp(image)
                        onImageSelected(
                            ImageFormData(
                                uri = tempUrl ?: imageUrl?.absoluteString ?: "",
                                type = ImageType.LOCAL_FILE,
                                isPrimary = false
                            )
                        )
                    }
                    picker.dismissViewControllerAnimated(true, completion = null)
                }
            }
            
            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, completion = null)
                onDismiss()
            }
        }
    }
    
    LaunchedEffect(Unit) {
        imagePickerController.delegate = delegate
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            imagePickerController,
            animated = true,
            completion = null
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun saveImageToTemp(image: UIImage): String? {
    val data = UIImageJPEGRepresentation(image, 0.8) ?: return null
    val tempDir = NSTemporaryDirectory()
    val fileName = "${NSUUID().UUIDString}.jpg"
    val filePath = "$tempDir$fileName"
    
    return if (data.writeToFile(filePath, atomically = true)) {
        NSURL.fileURLWithPath(filePath).absoluteString
    } else {
        null
    }
}
