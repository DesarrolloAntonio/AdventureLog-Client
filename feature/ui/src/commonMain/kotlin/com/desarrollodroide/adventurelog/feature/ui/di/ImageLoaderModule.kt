package com.desarrollodroide.adventurelog.feature.ui.di

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestPipeline
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
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.feature.ui.util.ImageBytesProvider
import com.desarrollodroide.adventurelog.feature.ui.util.isSameOrigin
import com.desarrollodroide.adventurelog.feature.ui.util.AuthenticatedFileDownloader
import com.desarrollodroide.adventurelog.feature.ui.util.createAttachmentOpener
import com.desarrollodroide.adventurelog.feature.ui.util.createImageBytesProvider

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
        val userRepository = get<UserRepository>()
        val sessionTokenManager = get<SessionTokenManager>()

        HttpClient {
            install("AttachSessionTokenToOwnServer") {
                requestPipeline.intercept(HttpRequestPipeline.State) {
                    // Media is served behind an auth check: anything belonging to a non-public
                    // location answers 403 without a session and the image renders blank. Reading
                    // the session here, as the request is sent, means the very first image request
                    // is already authenticated - pushing the token in from a composable raced it.
                    val session = userRepository.activeSession
                    val token = session?.sessionToken ?: sessionTokenManager.sessionToken.value

                    // The same loader also fetches Wikipedia thumbnails and user-pasted URLs, so
                    // the token only goes to the server the user is signed in to.
                    if (!token.isNullOrEmpty() &&
                        isSameOrigin(context.url.buildString(), session?.serverUrl)
                    ) {
                        context.header("X-Session-Token", token)
                    }
                }
            }
        }
    }

    single { createImageBytesProvider(get()) }

    single { createAttachmentOpener(get()) }

    single { AuthenticatedFileDownloader(client = get(named("imageClient"))) }
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