package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * A signed-in account, as most screens assume.
 *
 * The strict [UserRepositoryStub] is the right base for tests *about* the session. This one is
 * for the many that only need someone to be logged in: nearly every screen asks who the user is
 * on the way in, and making each test spell that out again says nothing.
 */
open class FakeUserRepository(
    private val session: UserDetails? = testUser,
    private val stats: Either<ApiResponse, UserStats> = Either.Right(UserStats())
) : UserRepositoryStub() {
    override suspend fun saveRememberMeCredentials(url: String, username: String) = Unit
    override fun getRememberMeCredentials(): Flow<com.desarrollodroide.adventurelog.core.model.Account?> = flowOf(null)
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
