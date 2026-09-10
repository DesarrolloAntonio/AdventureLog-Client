package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.core.model.LoginCredentials
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.flow.StateFlow
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a SettingsRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class SettingsRepositoryStub : SettingsRepository {
    override suspend fun saveUserDetails(userDetails: UserDetails): Unit = unused()
    override suspend fun getUserDetails(): UserDetails? = unused()
    override suspend fun saveLoginCredentials(credentials: LoginCredentials): Unit = unused()
    override suspend fun getLoginCredentials(): LoginCredentials? = unused()
    override suspend fun clearLoginCredentials(): Unit = unused()
    override suspend fun clearAll(): Unit = unused()
    override fun getThemeMode(): StateFlow<ThemeMode> = unused()
    override suspend fun setThemeMode(themeMode: ThemeMode): Unit = unused()
    override fun getUseDynamicColors(): StateFlow<Boolean> = unused()
    override suspend fun setUseDynamicColors(useDynamicColors: Boolean): Unit = unused()
    override fun getCompactView(): StateFlow<Boolean> = unused()
    override suspend fun setCompactView(compactView: Boolean): Unit = unused()
}
