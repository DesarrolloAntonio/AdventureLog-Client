package com.desarrollodroide.adventurelog.feature.ui.data

data class ImageFormData(
    val uri: String,
    val type: ImageType,
    val isPrimary: Boolean = false,
    /** The id of a photo already on the server; null for one added in this form. */
    val serverId: String? = null
)

enum class ImageType {
    LOCAL_FILE,
    URL,
    WIKIPEDIA
}
