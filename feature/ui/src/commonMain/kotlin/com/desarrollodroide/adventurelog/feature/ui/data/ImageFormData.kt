package com.desarrollodroide.adventurelog.feature.ui.data

data class ImageFormData(
    val uri: String,
    val type: ImageType,
    val isPrimary: Boolean = false
)

enum class ImageType {
    LOCAL_FILE,
    URL,
    WIKIPEDIA
}
