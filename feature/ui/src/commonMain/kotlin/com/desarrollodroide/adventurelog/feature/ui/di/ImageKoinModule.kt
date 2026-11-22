package com.desarrollodroide.adventurelog.feature.ui.di

import com.desarrollodroide.adventurelog.feature.ui.util.PlatformFiles
import com.desarrollodroide.adventurelog.core.domain.repository.LocalAccountCopies
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.feature.ui.util.isSameOrigin
import com.desarrollodroide.adventurelog.feature.ui.util.AuthenticatedFileDownloader
import com.desarrollodroide.adventurelog.feature.ui.util.createPlatformFiles
import com.desarrollodroide.adventurelog.feature.ui.util.createImageBytesProvider

// The Koin half of what was ImageLoaderModule.kt: shared, so iOS gets the same bindings. The
// CompositionLocals and ProvideImageDependencies stay with the Compose UI in androidMain.

class SessionTokenManager {
    private val _sessionToken = MutableStateFlow<String?>(null)
    val sessionToken: StateFlow<String?> = _sessionToken
    
    fun updateSessionToken(token: String?) {
        _sessionToken.value = token
    }
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
                        // Server 0.13.1 reads the session only from its cookie. GETs need no CSRF.
                        context.header(HttpHeaders.Cookie, "sessionid=$token")
                    }
                }
            }
        }
    }

    single { createImageBytesProvider() }

    single { createPlatformFiles() }

    // Sign-out deletes what the files above wrote (LogoutUseCase).
    single<LocalAccountCopies> { get<PlatformFiles>() }

    single { AuthenticatedFileDownloader(client = get(named("imageClient"))) }
}
