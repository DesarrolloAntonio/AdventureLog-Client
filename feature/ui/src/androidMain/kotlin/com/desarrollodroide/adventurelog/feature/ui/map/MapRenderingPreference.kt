package com.desarrollodroide.adventurelog.feature.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.MapRendering
import com.desarrollodroide.adventurelog.core.model.MapStyles
import org.koin.compose.koinInject

/**
 * The account's "Default map style" preference, reduced to the nearest thing a phone map can draw.
 *
 * The web says this style is used "as the default base map on every map", so every map in the app
 * reads it here rather than having each screen thread it down: it is ambient account state, not a
 * property of any one caller. It comes from the session, so changing it on the web changes it here
 * on the next refresh.
 */
@Composable
fun rememberMapRendering(): MapRendering {
    val userRepository = koinInject<UserRepository>()
    val user by userRepository.getUserSession().collectAsState(initial = userRepository.activeSession)
    return MapStyles.mapTypeFor(user?.mapStyle)
}
