package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Account
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import kotlinx.coroutines.flow.Flow
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a UserRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class UserRepositoryStub : UserRepository {
    override suspend fun saveRememberMeCredentials(
        url: String,
        username: String
    ): Unit = unused()
    override fun getRememberMeCredentials(): Flow<Account?> = unused()
    override suspend fun clearRememberMeCredentials(): Unit = unused()
    override suspend fun saveUserSession(userDetails: UserDetails): Unit = unused()
    override fun setActiveSession(userDetails: UserDetails): Unit = unused()
    override fun getUserSession(): Flow<UserDetails?> = unused()
    override suspend fun getUserSessionOnce(): UserDetails? = unused()
    override val activeSession: UserDetails? get() = unused()
    override suspend fun clearUserSession(): Unit = unused()
    override fun isLoggedIn(): Flow<Boolean> = unused()
    override suspend fun clearAllUserData(): Unit = unused()
    override suspend fun getUserStats(username: String): Either<ApiResponse, UserStats> = unused()
    override fun getUserStatsFlow(): Flow<UserStats?> = unused()
}
