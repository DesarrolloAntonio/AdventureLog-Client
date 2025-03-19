package com.desarrollodroide.adventurelog.core.model.preview

import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.AdventureImage
import com.desarrollodroide.adventurelog.core.model.Attachment
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit

object PreviewData {
    val categories = listOf(
        Category(
            id = "1",
            name = "hotel",
            displayName = "Hotel",
            icon = "🏨",
            numAdventures = 5
        ),
        Category(
            id = "2",
            name = "restaurant",
            displayName = "Restaurant",
            icon = "🍽️",
            numAdventures = 3
        ),
        Category(
            id = "3",
            name = "beach",
            displayName = "Beach",
            icon = "🏖️",
            numAdventures = 4
        ),
        Category(
            id = "4",
            name = "ruta",
            displayName = "Ruta",
            icon = "🏞️",
            numAdventures = 2
        )
    )

    val images = listOf(
        AdventureImage(
            id = "1",
            image = "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
            adventure = "1",
            isPrimary = true,
            userId = "1"
        ),
        AdventureImage(
            id = "2",
            image = "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            adventure = "2",
            isPrimary = true,
            userId = "2"
        ),
        AdventureImage(
            id = "3",
            image = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
            adventure = "3",
            isPrimary = true,
            userId = "3"
        ),
        AdventureImage(
            id = "4",
            image = "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            adventure = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
            isPrimary = true,
            userId = "user123"
        ),
        AdventureImage(
            id = "5",
            image = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
            adventure = "2ac911dd-8742-45e6-b105-5c04779e8bea",
            isPrimary = true,
            userId = "user123"
        )
    )

    val visits = listOf(
        Visit(
            id = "1",
            startDate = "2024-01-15",
            endDate = "2024-01-20",
            notes = "Amazing experience"
        ),
        Visit(
            id = "2",
            startDate = "2024-02-01",
            endDate = "2024-02-05",
            notes = "Great weekend getaway"
        )
    )

    val adventures = listOf(
        Adventure(
            id = "1",
            userId = "1",
            name = "Lake District Mountain Resort (Pending)",
            description = "Beautiful mountain resort with scenic views and spa facilities",
            rating = 4.5,
            activityTypes = listOf("Spa", "Swimming", "Hiking"),
            location = "4h 28min (445 km)",
            isPublic = false,
            collection = "Planned",
            createdAt = "2024-02-06",
            updatedAt = "2024-02-06",
            images = listOf(images[0]),
            link = "https://example.com/resort",
            longitude = "-2.3522",
            latitude = "48.8566",
            visits = listOf(visits[0]),
            isVisited = false,
            category = categories[0],
            attachments = listOf()
        ),
        Adventure(
            id = "2",
            userId = "2",
            name = "Coastal Beach Resort & Spa",
            description = "Luxurious beachfront resort with private beach access",
            rating = 4.8,
            activityTypes = listOf("Beach", "Spa", "Water Sports"),
            location = "2h 15min (180 km)",
            isPublic = true,
            collection = "Summer 2024",
            createdAt = "2024-02-05",
            updatedAt = "2024-02-05",
            images = listOf(images[1]),
            link = "https://example.com/beach-resort",
            longitude = "-3.1234",
            latitude = "47.5678",
            visits = listOf(visits[1]),
            isVisited = true,
            category = categories[2],
            attachments = listOf()
        ),
        Adventure(
            id = "3",
            userId = "3",
            name = "Mountain View Hotel",
            description = "Cozy mountain hotel with panoramic views",
            rating = 4.2,
            activityTypes = listOf("Hiking", "Skiing"),
            location = "3h 45min (320 km)",
            isPublic = true,
            collection = "Winter 2024",
            createdAt = "2024-02-04",
            updatedAt = "2024-02-04",
            images = listOf(images[2]),
            link = "https://example.com/mountain-hotel",
            longitude = "-1.9876",
            latitude = "46.4321",
            visits = emptyList(),
            isVisited = false,
            category = categories[0],
            attachments = listOf()
        ),
    )
}