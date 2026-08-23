package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("rating")
    val rating: Double? = null,

    @SerialName("tags")
    val tags: List<String>? = null,

    @SerialName("location")
    val location: String? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("collections")
    val collections: List<String>? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("images")
    val images: List<ContentImageDTO>? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("longitude")
    val longitude: String? = null,

    @SerialName("latitude")
    val latitude: String? = null,

    @SerialName("visits")
    val visits: List<VisitDTO>? = null,

    @SerialName("is_visited")
    val isVisited: Boolean = false,

    @SerialName("category")
    val category: CategoryDTO? = null,

    @SerialName("attachments")
    val attachments: List<AttachmentDTO>? = null,

    @SerialName("user")
    val user: UserDetailsDTO,

    @SerialName("city")
    val city: CityDTO? = null,

    @SerialName("country")
    val country: CountryDTO? = null,

    @SerialName("region")
    val region: RegionDTO? = null,

    @SerialName("trails")
    val trails: List<TrailDTO>? = null,

    @SerialName("price")
    val price: Double? = null,

    @SerialName("price_currency")
    val priceCurrency: String? = null
)

fun LocationDTO.toDomainModel(): Location = Location(
    id = id,
    name = name,
    description = description,
    rating = rating,
    tags = tags ?: emptyList(),
    location = location,
    isPublic = isPublic,
    collections = collections ?: emptyList(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    images = images?.map { it.toDomainModel() } ?: emptyList(),
    link = link,
    longitude = longitude,
    latitude = latitude,
    visits = visits?.map { it.toDomainModel() } ?: emptyList(),
    isVisited = isVisited,
    category = category?.toDomainModel(),
    attachments = attachments?.map { it.toDomainModel() } ?: emptyList(),
    user = user.toDomainModel(),
    city = city?.toDomainModel(),
    country = country?.toDomainModel(),
    region = region?.toDomainModel(),
    price = price,
    priceCurrency = priceCurrency,
    trails = trails?.map { it.toDomainModel() } ?: emptyList()
)
