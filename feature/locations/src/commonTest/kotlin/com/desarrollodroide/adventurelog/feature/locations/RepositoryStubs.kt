package com.desarrollodroide.adventurelog.feature.locations

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CategoriesRepository
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * The default is to fail, not to return nothing.
 *
 * A stub that answers every call with an empty list lets a test pass while the view model asks
 * for something entirely different from what the test is about. Overriding exactly the calls
 * under test means the stub says so when the view model reaches past them.
 */
private fun unused(): Nothing =
    throw AssertionError("This test reached a repository call it does not override")

abstract class LocationsRepositoryStub : LocationsRepository {
    override var selectedLocation: Location? = null
    override fun getLocationsPagingData(): Flow<PagingData<Location>> = unused()
    override fun getLocationsPagingDataFiltered(
        categoryNames: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): Flow<PagingData<Location>> = unused()
    override suspend fun getLocations(page: Int, pageSize: Int): Either<ApiResponse, List<Location>> = unused()
    override suspend fun getAllLocations(): Either<ApiResponse, List<Location>> = unused()
    override suspend fun getLocation(objectId: String): Either<ApiResponse, Location> = unused()
    override suspend fun createLocation(
        name: String,
        description: String,
        category: Category,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        visits: List<VisitFormData>,
        price: Double?,
        priceCurrency: String?,
        activityTypes: List<String>
    ): Either<ApiResponse, Location> = unused()
    override suspend fun refreshLocations(): Either<ApiResponse, List<Location>> = unused()
    override suspend fun duplicateLocation(locationId: String): Either<ApiResponse, Location> = unused()
    override suspend fun getShareImage(locationId: String, aspect: String): Either<ApiResponse, ByteArray> = unused()
    override suspend fun generateDescription(name: String): Either<ApiResponse, String> = unused()
    override suspend fun deleteLocation(adventureId: String): Either<ApiResponse, Unit> = unused()
    override suspend fun updateLocation(
        adventureId: String,
        name: String,
        description: String,
        category: Category?,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        tags: List<String>,
        collections: List<String>,
        visits: List<VisitFormData>,
        price: Double?,
        priceCurrency: String?
    ): Either<ApiResponse, Location> = unused()
}

abstract class CategoriesRepositoryStub : CategoriesRepository {
    override suspend fun getCategories(): Either<ApiResponse, List<Category>> = unused()
    override suspend fun getCategoryById(categoryId: String): Either<ApiResponse, Category> = unused()
    override suspend fun createCategory(name: String, displayName: String, icon: String?): Either<ApiResponse, Category> = unused()
    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        displayName: String,
        icon: String?
    ): Either<ApiResponse, Category> = unused()
    override suspend fun deleteCategory(categoryId: String): Either<ApiResponse, Unit> = unused()
}

/**
 * A signed-in account with no stats, which is what the list header falls back to.
 */
open class UserRepositoryStub(
    private val session: UserDetails? = testUser,
    // Every screen that lists places asks for these on the way in, so an empty set of counts is
    // the honest default rather than something a test has to remember to provide.
    private val stats: Either<ApiResponse, UserStats> = Either.Right(UserStats())
) : UserRepository {
    override suspend fun saveRememberMeCredentials(url: String, username: String, password: String) = Unit
    override fun getRememberMeCredentials(): Flow<Account?> = flowOf(null)
    override suspend fun clearRememberMeCredentials() = Unit
    override suspend fun saveUserSession(userDetails: UserDetails) = Unit
    override fun setActiveSession(userDetails: UserDetails) = Unit
    override fun getUserSession(): Flow<UserDetails?> = flowOf(session)
    override suspend fun getUserSessionOnce(): UserDetails? = session
    override val activeSession: UserDetails? get() = session
    override suspend fun clearUserSession() = Unit
    override fun isLoggedIn(): Flow<Boolean> = flowOf(session != null)
    override suspend fun clearAllUserData() = Unit
    override suspend fun getUserStats(username: String): Either<ApiResponse, UserStats> = stats
    override fun getUserStatsFlow(): Flow<UserStats?> = emptyFlow()
}

val testUser = UserDetails(uuid = "u", username = "claude", dateJoined = "2024-01-01")
