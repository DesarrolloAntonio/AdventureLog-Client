package com.desarrollodroide.adventurelog.core.model.preview

import com.desarrollodroide.adventurelog.core.model.*

object PreviewData {
    
    // Sample users
    val sampleUser1 = UserDetails(
        pk = 1,
        uuid = "user1-uuid",
        username = "user1",
        firstName = "John",
        lastName = "Doe",
        email = "john@example.com",
        profilePic = null,
        publicProfile = true,
        measurementSystem = "metric",
        dateJoined = "2024-01-01",
        isStaff = false,
        disablePassword = false,
        hasPassword = true,
        sessionToken = null,
        serverUrl = null
    )
    
    val sampleUser2 = UserDetails(
        pk = 2,
        uuid = "user2-uuid",
        username = "user2",
        firstName = "Jane",
        lastName = "Smith",
        email = "jane@example.com",
        profilePic = null,
        publicProfile = true,
        measurementSystem = "metric",
        dateJoined = "2024-01-02",
        isStaff = false,
        disablePassword = false,
        hasPassword = true,
        sessionToken = null,
        serverUrl = null
    )
    
    val sampleUser3 = UserDetails(
        pk = 3,
        uuid = "user3-uuid",
        username = "user3",
        firstName = "Bob",
        lastName = "Johnson",
        email = "bob@example.com",
        profilePic = null,
        publicProfile = true,
        measurementSystem = "metric",
        dateJoined = "2024-01-03",
        isStaff = false,
        disablePassword = false,
        hasPassword = true,
        sessionToken = null,
        serverUrl = null
    )
    
    val sampleUserDefault = UserDetails(
        pk = 4,
        uuid = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
        username = "memnoch",
        firstName = "Antonio",
        lastName = "Corrales",
        email = "antonio@example.com",
        profilePic = null,
        publicProfile = true,
        measurementSystem = "metric",
        dateJoined = "2025-01-30T07:15:10.367579Z",
        isStaff = false,
        disablePassword = false,
        hasPassword = true,
        sessionToken = null,
        serverUrl = null
    )
    
    val categories = listOf(
        Category(
            id = "1",
            name = "hotel",
            displayName = "Hotel",
            icon = "🏨",
            numAdventures = "5"
        ),
        Category(
            id = "2",
            name = "restaurant",
            displayName = "Restaurant",
            icon = "🍽️",
            numAdventures = "3"
        ),
        Category(
            id = "3",
            name = "beach",
            displayName = "Beach",
            icon = "🏖️",
            numAdventures = "4"
        ),
        Category(
            id = "4",
            name = "ruta",
            displayName = "Ruta",
            icon = "🏞️",
            numAdventures = "2"
        )
    )

    // Images for the first adventure (Lake District Mountain Resort)
    val lakeMountainImages = listOf(
        ContentImage(
            id = "1-1",
            user = "user1",
            image = "https://images.unsplash.com/photo-1571896349842-33c89424de2d",
            immichId = null,
            isPrimary = true
        ),
        ContentImage(
            id = "1-2",
            user = "user1",
            image = "https://images.unsplash.com/photo-1554995207-c18c203602cb",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "1-3",
            user = "user1",
            image = "https://images.unsplash.com/photo-1551632811-561732d1e306",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "1-4",
            user = "user1",
            image = "https://images.unsplash.com/photo-1560624052-449f5ddf0c31",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "1-5",
            user = "user1",
            image = "https://images.unsplash.com/photo-1568084680786-a84f91d1153c",
            immichId = null,
            isPrimary = false
        ),
    )

    // Images for the second adventure (Coastal Beach Resort)
    val beachResortImages = listOf(
        ContentImage(
            id = "2-1",
            user = "user2",
            image = "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            immichId = null,
            isPrimary = true
        ),
        ContentImage(
            id = "2-2",
            user = "user2",
            image = "https://images.unsplash.com/photo-1573790387438-4da905039392",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "2-3",
            user = "user2",
            image = "https://images.unsplash.com/photo-1540541338287-41700207dee6",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "2-4",
            user = "user2",
            image = "https://images.unsplash.com/photo-1533760881669-80db4d7b4c15",
            immichId = null,
            isPrimary = false
        ),
    )

    // Images for the third adventure (Mountain View Hotel)
    val mountainHotelImages = listOf(
        ContentImage(
            id = "3-1",
            user = "user3",
            image = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
            immichId = null,
            isPrimary = true
        ),
        ContentImage(
            id = "3-2",
            user = "user3",
            image = "https://images.unsplash.com/photo-1519944518895-f08a12d6dfd5",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "3-3",
            user = "user3",
            image = "https://images.unsplash.com/photo-1548704606-c65a61e6afe7",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "3-4",
            user = "user3",
            image = "https://images.unsplash.com/photo-1443385434562-3aded81268b4",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "3-5",
            user = "user3",
            image = "https://images.unsplash.com/photo-1509023464722-18d996393ca8",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "3-6",
            user = "user3",
            image = "https://images.unsplash.com/photo-1483777979751-03fa468d266c",
            immichId = null,
            isPrimary = false
        ),
    )

    // Images for specific examples
    val balnearioImages = listOf(
        ContentImage(
            id = "b-1",
            user = "user123",
            image = "https://images.unsplash.com/photo-1566073771259-6a8506099945",
            immichId = null,
            isPrimary = true
        ),
        ContentImage(
            id = "b-2",
            user = "user123",
            image = "https://images.unsplash.com/photo-1584132915807-fd1f5fbc078f",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "b-3",
            user = "user123",
            image = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "b-4",
            user = "user123",
            image = "https://images.unsplash.com/photo-1519449556851-5720b33024e7",
            immichId = null,
            isPrimary = false
        ),
    )

    val navalagamellaImages = listOf(
        ContentImage(
            id = "n-1",
            user = "user123",
            image = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4",
            immichId = null,
            isPrimary = true
        ),
        ContentImage(
            id = "n-2",
            user = "user123",
            image = "https://images.unsplash.com/photo-1551632811-561732d1e306",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "n-3",
            user = "user123",
            image = "https://images.unsplash.com/photo-1551849630-3c2969e08b74",
            immichId = null,
            isPrimary = false
        ),
        ContentImage(
            id = "n-4",
            user = "user123",
            image = "https://images.unsplash.com/photo-1547125696-1d32a98e3d36",
            immichId = null,
            isPrimary = false
        ),
    )

    val visits = listOf(
        Visit(
            id = "1",
            location = "location-1",
            startDate = "2024-01-15",
            endDate = "2024-01-20",
            notes = "Amazing experience",
            timezone = "Europe/Madrid",
            activities = emptyList(),
            createdAt = "2024-01-15",
            updatedAt = "2024-01-20"
        ),
        Visit(
            id = "2",
            location = "location-2",
            startDate = "2024-02-01",
            endDate = "2024-02-05",
            notes = "Great weekend getaway",
            timezone = "UTC",
            activities = emptyList(),
            createdAt = "2024-02-01",
            updatedAt = "2024-02-05"
        )
    )

    val locations = listOf(
        Location(
            id = "1",
            name = "Lake District Mountain Resort (Pending)",
            description = "Beautiful mountain resort with scenic views and spa facilities",
            rating = 4.5,
            tags = listOf("Spa", "Swimming", "Hiking"),
            location = "4h 28min (445 km)",
            isPublic = false,
            collections = listOf("planned-collection-id"),
            createdAt = "2024-02-06",
            updatedAt = "2024-02-06",
            images = lakeMountainImages,
            link = "https://example.com/resort",
            longitude = "-2.3522",
            latitude = "48.8566",
            visits = listOf(visits[0]),
            isVisited = false,
            category = categories[0],
            attachments = listOf(),
            user = sampleUser1,
            city = null,
            country = null,
            region = null,
            trails = emptyList()
        ),
        Location(
            id = "2",
            name = "Coastal Beach Resort & Spa",
            description = "Luxurious beachfront resort with private beach access",
            rating = 4.8,
            tags = listOf("Beach", "Spa", "Water Sports"),
            location = "2h 15min (180 km)",
            isPublic = true,
            collections = listOf("summer-2024-collection-id"),
            createdAt = "2024-02-05",
            updatedAt = "2024-02-05",
            images = beachResortImages,
            link = "https://example.com/beach-resort",
            longitude = "-3.1234",
            latitude = "47.5678",
            visits = listOf(visits[1]),
            isVisited = true,
            category = categories[2],
            attachments = listOf(),
            user = sampleUser2,
            city = null,
            country = null,
            region = null,
            trails = emptyList()
        ),
        Location(
            id = "3",
            name = "Mountain View Hotel",
            description = "Cozy mountain hotel with panoramic views",
            rating = 4.2,
            tags = listOf("Hiking", "Skiing"),
            location = "3h 45min (320 km)",
            isPublic = true,
            collections = listOf("winter-2024-collection-id"),
            createdAt = "2024-02-04",
            updatedAt = "2024-02-04",
            images = mountainHotelImages,
            link = "https://example.com/mountain-hotel",
            longitude = "-1.9876",
            latitude = "46.4321",
            visits = emptyList(),
            isVisited = false,
            category = categories[0],
            attachments = listOf(),
            user = sampleUser3,
            city = null,
            country = null,
            region = null,
            trails = emptyList()
        ),
    )
    
    val collections = listOf(
        Collection(
            id = "c1",
            description = "A collection of summer adventures in Spain",
            userId = "user1",
            name = "Summer Locations",
            isPublic = false,
            locations = listOf(locations[0]),
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
            userId = "user1",
            name = "Winter Mountains",
            isPublic = false,
            locations = listOf(locations[1], locations[2]),
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
            userId = "user1",
            name = "Beach Destinations",
            isPublic = false,
            locations = emptyList(),
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
    
    // UltraSlimCollections for testing UI components
    val sampleUltraSlimCollections = listOf(
        UltraSlimCollection(
            id = "1",
            name = "Summer 2025",
            description = "Summer adventures and trips",
            isPublic = true,
            isArchived = false,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            startDate = "2025-06-01T00:00:00Z",
            endDate = "2025-08-31T00:00:00Z",
            adventureCount = 5,
            featuredImage = "https://images.unsplash.com/photo-1506905925346-21bda4d32df4",
            link = null
        ),
        UltraSlimCollection(
            id = "2",
            name = "Mountain Hikes",
            description = "Collection of mountain hiking adventures",
            isPublic = false,
            isArchived = false,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            startDate = null,
            endDate = null,
            adventureCount = 3,
            featuredImage = "https://images.unsplash.com/photo-1551632811-561732d1e306",
            link = null
        ),
        UltraSlimCollection(
            id = "3",
            name = "City Breaks",
            description = "Urban explorations and city adventures",
            isPublic = true,
            isArchived = false,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            startDate = null,
            endDate = null,
            adventureCount = 8,
            featuredImage = "https://images.unsplash.com/photo-1477959858617-67f85cf4f1df",
            link = null
        ),
        UltraSlimCollection(
            id = "4",
            name = "Favorite Places",
            description = "My all-time favorite locations",
            isPublic = true,
            isArchived = false,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
            startDate = null,
            endDate = null,
            adventureCount = 12,
            featuredImage = null,
            link = null
        )
    )
    
    val spainRegionsCollections = listOf(
        Collection(
            id = "cdcd3ecc-215f-4fdf-a748-94b95e8956a4",
            description = "Rutas y lugares de interés en Álava",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Álava",
            isPublic = false,
            locations = locations.take(2),
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
            id = "1fa47722-b98b-4c58-ae45-a0a10f78e162",
            description = "Destinos populares en la sierra del Segura",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Albacete",
            isPublic = false,
            locations = locations.take(3),
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
            id = "d64ccfe8-918a-4e3c-8199-82ede3ec3e57",
            description = "Playas y destinos costeros",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Alicante",
            isPublic = false,
            locations = emptyList(),
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
        ),
        Collection(
            id = "239c3a34-3b6c-46f8-b06e-764f0b5dac53",
            description = "Pueblos pintorescos de Teruel",
            userId = "e0c8df01-2bf8-403f-a4da-a0d09ef32353",
            name = "Teruel",
            isPublic = false,
            locations = locations.take(1),
            createdAt = "2025-02-15T12:41:12.529110Z",
            startDate = null,
            endDate = null,
            transportations = emptyList(),
            notes = emptyList(),
            updatedAt = "2025-02-15T12:41:12.529149Z",
            checklists = emptyList(),
            isArchived = false,
            sharedWith = emptyList(),
            link = "",
            lodging = emptyList()
        )
    )
}
