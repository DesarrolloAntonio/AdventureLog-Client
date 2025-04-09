package com.desarrollodroide.adventurelog.core.model.preview

import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.AdventureImage
import com.desarrollodroide.adventurelog.core.model.Attachment
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Collection
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

    // Imágenes para la primera aventura (Lake District Mountain Resort)
    val lakeMountainImages = listOf(
        AdventureImage(
            id = "1-1",
            image = "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
            adventure = "1",
            isPrimary = true,
            userId = "1"
        ),
        AdventureImage(
            id = "1-2",
            image = "https://images.unsplash.com/photo-1554995207-c18c203602cb",
            adventure = "1",
            isPrimary = false,
            userId = "1"
        ),
        AdventureImage(
            id = "1-3",
            image = "https://images.unsplash.com/photo-1551632811-561732d1e306",
            adventure = "1",
            isPrimary = false,
            userId = "1"
        ),
        AdventureImage(
            id = "1-4",
            image = "https://images.unsplash.com/photo-1560624052-449f5ddf0c31",
            adventure = "1",
            isPrimary = false,
            userId = "1"
        ),
        AdventureImage(
            id = "1-5",
            image = "https://images.unsplash.com/photo-1568084680786-a84f91d1153c",
            adventure = "1",
            isPrimary = false,
            userId = "1"
        ),
    )

    // Imágenes para la segunda aventura (Coastal Beach Resort)
    val beachResortImages = listOf(
        AdventureImage(
            id = "2-1",
            image = "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            adventure = "2",
            isPrimary = true,
            userId = "2"
        ),
        AdventureImage(
            id = "2-2",
            image = "https://images.unsplash.com/photo-1573790387438-4da905039392",
            adventure = "2",
            isPrimary = false,
            userId = "2"
        ),
        AdventureImage(
            id = "2-3",
            image = "https://images.unsplash.com/photo-1540541338287-41700207dee6",
            adventure = "2",
            isPrimary = false,
            userId = "2"
        ),
        AdventureImage(
            id = "2-4",
            image = "https://images.unsplash.com/photo-1533760881669-80db4d7b4c15",
            adventure = "2",
            isPrimary = false,
            userId = "2"
        ),
    )

    // Imágenes para la tercera aventura (Mountain View Hotel)
    val mountainHotelImages = listOf(
        AdventureImage(
            id = "3-1",
            image = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
            adventure = "3",
            isPrimary = true,
            userId = "3"
        ),
        AdventureImage(
            id = "3-2",
            image = "https://images.unsplash.com/photo-1519944518895-f08a12d6dfd5",
            adventure = "3",
            isPrimary = false,
            userId = "3"
        ),
        AdventureImage(
            id = "3-3",
            image = "https://images.unsplash.com/photo-1548704606-c65a61e6afe7",
            adventure = "3",
            isPrimary = false,
            userId = "3"
        ),
        AdventureImage(
            id = "3-4",
            image = "https://images.unsplash.com/photo-1443385434562-3aded81268b4",
            adventure = "3",
            isPrimary = false,
            userId = "3"
        ),
        AdventureImage(
            id = "3-5",
            image = "https://images.unsplash.com/photo-1509023464722-18d996393ca8",
            adventure = "3",
            isPrimary = false,
            userId = "3"
        ),
        AdventureImage(
            id = "3-6",
            image = "https://images.unsplash.com/photo-1483777979751-03fa468d266c",
            adventure = "3",
            isPrimary = false,
            userId = "3"
        ),
    )

    // Imágenes para ejemplos específicos
    val balnearioImages = listOf(
        AdventureImage(
            id = "b-1",
            image = "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            adventure = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
            isPrimary = true,
            userId = "user123"
        ),
        AdventureImage(
            id = "b-2",
            image = "https://images.unsplash.com/photo-1584132915807-fd1f5fbc078f",
            adventure = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
            isPrimary = false,
            userId = "user123"
        ),
        AdventureImage(
            id = "b-3",
            image = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7",
            adventure = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
            isPrimary = false,
            userId = "user123"
        ),
        AdventureImage(
            id = "b-4",
            image = "https://images.unsplash.com/photo-1519449556851-5720b33024e7",
            adventure = "c9cfb44c-536a-492c-87ff-8c3bb5d3eec5",
            isPrimary = false,
            userId = "user123"
        ),
    )

    val navalagamellaImages = listOf(
        AdventureImage(
            id = "n-1",
            image = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
            adventure = "2ac911dd-8742-45e6-b105-5c04779e8bea",
            isPrimary = true,
            userId = "user123"
        ),
        AdventureImage(
            id = "n-2",
            image = "https://images.unsplash.com/photo-1551632811-561732d1e306",
            adventure = "2ac911dd-8742-45e6-b105-5c04779e8bea",
            isPrimary = false,
            userId = "user123"
        ),
        AdventureImage(
            id = "n-3",
            image = "https://images.unsplash.com/photo-1551849630-3c2969e08b74",
            adventure = "2ac911dd-8742-45e6-b105-5c04779e8bea",
            isPrimary = false,
            userId = "user123"
        ),
        AdventureImage(
            id = "n-4",
            image = "https://images.unsplash.com/photo-1547125696-1d32a98e3d36",
            adventure = "2ac911dd-8742-45e6-b105-5c04779e8bea",
            isPrimary = false,
            userId = "user123"
        ),
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
            images = lakeMountainImages,
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
            images = beachResortImages,
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
            images = mountainHotelImages,
            link = "https://example.com/mountain-hotel",
            longitude = "-1.9876",
            latitude = "46.4321",
            visits = emptyList(),
            isVisited = false,
            category = categories[0],
            attachments = listOf()
        ),
    )
    
    val collections = listOf(
        Collection(
            id = "c1",
            description = "A collection of summer adventures in Spain",
            userId = "1",
            name = "Álava",
            isPublic = false,
            adventures = listOf(adventures[0]),
            createdAt = "2025-01-30T07:21:07.230845Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2025-01-30T07:21:07.230885Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        ),
        Collection(
            id = "c2",
            description = "Winter adventures in the mountains",
            userId = "1",
            name = "Albacete",
            isPublic = false,
            adventures = listOf(adventures[1], adventures[2]),
            createdAt = "2025-01-30T15:57:27.605536Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2025-01-30T15:57:27.605575Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        ),
        Collection(
            id = "c3",
            description = "Beach destinations for 2025",
            userId = "1",
            name = "Alicante",
            isPublic = false,
            adventures = emptyList(),
            createdAt = "2025-02-09T12:21:01.829885Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2025-02-09T12:21:01.829925Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        )
    )
}