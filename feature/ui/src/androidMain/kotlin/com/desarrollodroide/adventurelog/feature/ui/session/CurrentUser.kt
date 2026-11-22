package com.desarrollodroide.adventurelog.feature.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import org.koin.compose.koinInject

/**
 * The signed-in account's uuid, or null before the session has been read.
 *
 * Ambient account state, read the way [com.desarrollodroide.adventurelog.feature.ui.map.rememberMapRendering]
 * reads the map style: screens that show someone else's records next to your own (a collection
 * shared with you) need it to tell which is which.
 */
@Composable
fun rememberCurrentUserId(): String? {
    val userRepository = koinInject<UserRepository>()
    val user by userRepository.getUserSession().collectAsState(initial = userRepository.activeSession)
    return user?.uuid
}
