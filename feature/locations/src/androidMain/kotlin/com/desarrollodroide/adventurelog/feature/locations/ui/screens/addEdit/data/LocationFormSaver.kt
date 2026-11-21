package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data

import androidx.compose.runtime.saveable.Saver
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

private val formJson = Json { ignoreUnknownKeys = true }

/**
 * Keeps the place form across a process death as one JSON string - a half-filled form came back
 * empty after the system killed the app in the background (measured).
 */
val LocationFormSaver: Saver<LocationFormData, String> = Saver(
    save = { formJson.encodeToString(LocationFormData.serializer(), it) },
    restore = { runCatching { formJson.decodeFromString(LocationFormData.serializer(), it) }.getOrNull() }
)

@Serializable
private data class SavedImage(val uri: String, val type: String, val isPrimary: Boolean, val serverId: String?)

/** ImageFormData lives in feature/ui, which has no serialization plugin; this writes it as [SavedImage]. */
internal object ImageFormDataSerializer : KSerializer<ImageFormData> {
    override val descriptor: SerialDescriptor = SavedImage.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ImageFormData) =
        encoder.encodeSerializableValue(
            SavedImage.serializer(),
            SavedImage(value.uri, value.type.name, value.isPrimary, value.serverId)
        )

    override fun deserialize(decoder: Decoder): ImageFormData =
        decoder.decodeSerializableValue(SavedImage.serializer()).let {
            ImageFormData(it.uri, ImageType.valueOf(it.type), it.isPrimary, it.serverId)
        }
}
