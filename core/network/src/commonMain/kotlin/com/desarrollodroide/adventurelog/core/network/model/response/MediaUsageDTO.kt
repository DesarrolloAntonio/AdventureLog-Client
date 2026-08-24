package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.MediaUsage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `GET /auth/user-media-usage/`. `limit_bytes` is null when the deployment sets no quota.
 */
@Serializable
data class MediaUsageDTO(
    @SerialName("total_bytes")
    val totalBytes: Long = 0,

    @SerialName("images_bytes")
    val imagesBytes: Long = 0,

    @SerialName("attachments_bytes")
    val attachmentsBytes: Long = 0,

    @SerialName("profile_pics_bytes")
    val profilePicsBytes: Long = 0,

    @SerialName("images_files")
    val imagesFiles: Int = 0,

    @SerialName("attachments_files")
    val attachmentsFiles: Int = 0,

    @SerialName("profile_pics_files")
    val profilePicsFiles: Int = 0,

    @SerialName("limit_bytes")
    val limitBytes: Long? = null
)

fun MediaUsageDTO.toDomainModel(): MediaUsage = MediaUsage(
    totalBytes = totalBytes,
    imagesBytes = imagesBytes,
    attachmentsBytes = attachmentsBytes,
    profilePicsBytes = profilePicsBytes,
    imagesFiles = imagesFiles,
    attachmentsFiles = attachmentsFiles,
    profilePicsFiles = profilePicsFiles,
    // The server sends 0 for "no limit"; treat that as unlimited rather than as a full quota.
    limitBytes = limitBytes?.takeIf { it > 0 }
)
