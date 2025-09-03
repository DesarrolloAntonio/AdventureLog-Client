package com.desarrollodroide.adventurelog.core.model

/**
 * Factory for creating test data instances with default values.
 * This reduces duplication and makes tests more maintainable.
 */
object TestDataFactory {
    
    const val DEFAULT_DATE = "2025-09-03T15:00:00Z"
    const val DEFAULT_UUID = "550e8400-e29b-41d4-a716-446655440000"
    
    fun createUserDetails(
        uuid: String = DEFAULT_UUID,
        username: String = "testuser",
        firstName: String = "Test",
        lastName: String = "User",
        email: String? = "test@example.com",
        profilePic: String? = null,
        publicProfile: Boolean = true,
        dateJoined: String = DEFAULT_DATE,
        isStaff: Boolean = false,
        hasPassword: Boolean = true,
        sessionToken: String? = null,
        serverUrl: String? = null
    ) = UserDetails(
        uuid = uuid,
        username = username,
        firstName = firstName,
        lastName = lastName,
        email = email,
        profilePic = profilePic,
        publicProfile = publicProfile,
        measurementSystem = "metric",
        dateJoined = dateJoined,
        isStaff = isStaff,
        disablePassword = false,
        hasPassword = hasPassword,
        sessionToken = sessionToken,
        serverUrl = serverUrl
    )
    
    fun createCategory(
        id: String = "cat-1",
        name: String = "hotel",
        displayName: String = "Hotel",
        icon: String = "🏨",
        numAdventures: String = "5"
    ) = Category(
        id = id,
        name = name,
        displayName = displayName,
        icon = icon,
        numAdventures = numAdventures
    )
    
    fun createVisit(
        id: String = "visit-1",
        location: String = "loc-1",
        startDate: String? = "2024-06-01",
        endDate: String? = "2024-06-07",
        notes: String? = "Test notes",
        timezone: String? = "UTC",
        createdAt: String = DEFAULT_DATE,
        updatedAt: String = DEFAULT_DATE
    ) = Visit(
        id = id,
        location = location,
        startDate = startDate,
        endDate = endDate,
        timezone = timezone,
        notes = notes,
        activities = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun createAttachment(
        id: String = "att-1",
        file: String = "https://example.com/file.pdf",
        extension: String = "pdf",
        name: String = "Test Document",
        user: String = "user-1"
    ) = Attachment(
        id = id,
        file = file,
        extension = extension,
        name = name,
        user = user
    )
    
    fun createLocation(
        id: String = "loc-1",
        name: String = "Test Location",
        description: String = "Test description",
        user: UserDetails = createUserDetails(),
        rating: Double = 4.5,
        tags: List<String> = listOf("hiking"),
        isPublic: Boolean = true,
        isVisited: Boolean = false,
        category: Category? = null
    ) = Location(
        id = id,
        user = user,
        name = name,
        description = description,
        rating = rating,
        tags = tags,
        location = "Test Location",
        isPublic = isPublic,
        collections = emptyList(),
        createdAt = DEFAULT_DATE,
        updatedAt = DEFAULT_DATE,
        images = emptyList(),
        link = "https://test.com",
        longitude = "0.0",
        latitude = "0.0",
        visits = emptyList(),
        isVisited = isVisited,
        category = category,
        attachments = emptyList(),
        trails = emptyList()
    )
    
    fun createCollection(
        id: String = "col-1",
        name: String = "Test Collection",
        userId: String = "user-1",
        description: String = "Test collection description",
        isPublic: Boolean = true,
        isArchived: Boolean = false,
        locations: List<Location> = emptyList()
    ) = Collection(
        id = id,
        description = description,
        userId = userId,
        name = name,
        isPublic = isPublic,
        locations = locations,
        createdAt = DEFAULT_DATE,
        startDate = null,
        endDate = null,
        transportations = emptyList(),
        notes = emptyList(),
        updatedAt = DEFAULT_DATE,
        checklists = emptyList(),
        isArchived = isArchived,
        sharedWith = emptyList(),
        link = "",
        lodging = emptyList()
    )
}
