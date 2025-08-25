package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdventuresDTO(
    @SerialName("count")
    val count: Int? = null,
    @SerialName("next")
    val next: String? = null,
    @SerialName("previous")
    val previous: String? = null,
    @SerialName("results")
    val results: List<AdventureDTO>? = null
) {
    companion object {
        val fakeData = AdventuresDTO(
            count = 2,
            next = null,
            previous = null,
            results = listOf(
                AdventureDTO(
                    id = "da55cb2e-d2cf-4ff0-b243-86da0a5e5a2f",
                    user = UserDTO(
                        uuid = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
                        username = "memnoch",
                        firstName = "Antonio",
                        lastName = "Corrales",
                        profilePic = "http://192.168.1.27:8016/media/profile-pics/1200x655_iStock-2097492658.webp",
                        publicProfile = true,
                        measurementSystem = "metric",
                        dateJoined = "2025-01-30T07:15:10.367579Z",
                        isStaff = false,
                        disablePassword = false,
                        hasPassword = true
                    ),
                    name = "Teruel",
                    description = "Albarracín\nMorada de Rubielos\nRubielos de Mora\nLinares de Mora\nMosqueruela\nCatavieja",
                    rating = null,
                    activityTypes = emptyList(),
                    location = null,
                    isPublic = false,
                    collections = listOf("239c3a34-3b6c-46f8-b06e-764f0b5dac53"),
                    createdAt = "2025-02-15T12:40:34.618987Z",
                    updatedAt = "2025-02-15T12:45:50.495445Z",
                    images = listOf(
                        AdventureImageDTO(
                            id = "63a7715f-04db-4c4e-bf02-c880955009fc",
                            image = "http://192.168.1.27:8016/media/images/3860e7f5-0064-4bda-8136-7210a57417a7.webp",
                            adventure = "da55cb2e-d2cf-4ff0-b243-86da0a5e5a2f",
                            isPrimary = true,
                            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
                            immichId = null
                        )
                    ),
                    link = "https://www.tiktok.com/@pasaportealatierra/video/7468289726114450720",
                    longitude = "-1.444279",
                    latitude = "40.407283",
                    visits = emptyList(),
                    isVisited = "false",
                    category = CategoryDTO(
                        id = "6c0a78cf-f657-45ca-9303-4bb1a7ab48e3",
                        name = "tour",
                        displayName = "Tour",
                        icon = "🚗",
                        numAdventures = "1"
                    ),
                    attachments = emptyList()
                ),
                AdventureDTO(
                    id = "2ac911dd-8742-45e6-b105-5c04779e8bea",
                    user = UserDTO(
                        uuid = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
                        username = "memnoch",
                        firstName = "Antonio",
                        lastName = "Corrales",
                        profilePic = "http://192.168.1.27:8016/media/profile-pics/1200x655_iStock-2097492658.webp",
                        publicProfile = true,
                        measurementSystem = "metric",
                        dateJoined = "2025-01-30T07:15:10.367579Z",
                        isStaff = false,
                        disablePassword = false,
                        hasPassword = true
                    ),
                    name = "Navalagamella",
                    description = "https://sendasdeviaje.com/navalagamella-ruta-molinos/",
                    rating = null,
                    activityTypes = emptyList(),
                    location = null,
                    isPublic = false,
                    collections = listOf("02dca32b-b707-42a0-8065-6a494d6e44c4"),
                    createdAt = "2025-02-15T12:26:12.551383Z",
                    updatedAt = "2025-02-15T12:29:34.376775Z",
                    images = listOf(
                        AdventureImageDTO(
                            id = "a7c575a7-f45d-4105-bb60-206d8f89f227",
                            image = "http://192.168.1.27:8016/media/images/079332e0-3099-4b62-b13a-dddb6b04dda3.webp",
                            adventure = "2ac911dd-8742-45e6-b105-5c04779e8bea",
                            isPrimary = true,
                            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
                            immichId = null
                        )
                    ),
                    link = "https://maps.app.goo.gl/8NkEZekZQUSHqUAt7",
                    longitude = "-4.122708",
                    latitude = "40.469059",
                    visits = emptyList(),
                    isVisited = "false",
                    category = CategoryDTO(
                        id = "87667725-8055-4754-be59-1155fac675f1",
                        name = "ruta",
                        displayName = "Ruta",
                        icon = "🏞️",
                        numAdventures = "1"
                    ),
                    attachments = emptyList()
                )
            )
        )
    }
}