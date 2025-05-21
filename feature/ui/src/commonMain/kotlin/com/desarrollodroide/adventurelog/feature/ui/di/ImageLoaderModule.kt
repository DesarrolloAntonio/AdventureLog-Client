package com.desarrollodroide.adventurelog.feature.ui.di

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Manages the session token for image authentication
 * Allows dynamic token updates that can be observed throughout the app
 */
class SessionTokenManager {
    private val _sessionToken = MutableStateFlow<String?>(null)
    val sessionToken: StateFlow<String?> = _sessionToken
    
    fun updateSessionToken(token: String?) {
        _sessionToken.value = token
    }
}

val LocalSessionTokenManager = staticCompositionLocalOf<SessionTokenManager> {
    error("LocalSessionTokenManager not initialized")
}

val LocalImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("LocalImageLoader not initialized")
}

/**
 * Koin module for ImageLoader management
 */
val imageLoaderModule = module {
    single { SessionTokenManager() }
    
    single(named("imageClient")) { 
        val sessionTokenManager = get<SessionTokenManager>()
        
        HttpClient {
            defaultRequest {
                val token = sessionTokenManager.sessionToken.value
                if (!token.isNullOrEmpty()) {
                    header("X-Session-Token", token)
                }
            }
        }
    }
}

/**
 * Provides ImageLoader and SessionTokenManager through CompositionLocals
 * Should be called from a top-level composable (e.g. App.kt)
 */
@Composable
fun ProvideImageDependencies(
    sessionTokenManager: SessionTokenManager,
    imageClient: HttpClient,
    content: @Composable () -> Unit
) {
    val platformContext = LocalPlatformContext.current
    
    val imageLoader = remember(platformContext, imageClient) {
        ImageLoader.Builder(platformContext)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = { imageClient }))
            }
            .build()
    }
    
    CompositionLocalProvider(
        LocalImageLoader provides imageLoader,
        LocalSessionTokenManager provides sessionTokenManager,
        content = content
    )
}