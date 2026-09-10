package com.desarrollodroide.adventurelog.feature.map

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/** Reaching a call a test did not set up is the test's mistake, and should say so. */
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

open class CountriesRepositoryStub(
    private val regions: Either<ApiResponse, List<VisitedRegion>> = Either.Right(emptyList()),
    private val cities: Either<ApiResponse, List<VisitedCity>> = Either.Right(emptyList())
) : CountriesRepository {
    override val countriesFlow: StateFlow<List<Country>> = MutableStateFlow(emptyList())
    override val visitedRegionsFlow: StateFlow<List<VisitedRegion>> = MutableStateFlow(emptyList())
    override val visitedCitiesFlow: StateFlow<List<VisitedCity>> = MutableStateFlow(emptyList())
    override suspend fun getCountries(): Either<ApiResponse, List<Country>> = unused()
    override suspend fun getRegions(countryCode: String): Either<ApiResponse, List<Region>> = unused()
    override suspend fun getVisitedRegions(): Either<ApiResponse, List<VisitedRegion>> = regions
    override suspend fun getVisitedCities(): Either<ApiResponse, List<VisitedCity>> = cities
    override suspend fun refreshVisitedRegions(): Either<ApiResponse, Pair<Int, Int>> = unused()
    override suspend fun markRegionVisited(regionId: String): Either<ApiResponse, VisitedRegion> = unused()
    override suspend fun unmarkRegionVisited(regionId: String): Either<ApiResponse, Unit> = unused()
    override suspend fun refreshCountries(): Either<ApiResponse, List<Country>> = unused()
}

open class UserRepositoryStub(
    private val session: UserDetails? = testUser,
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
