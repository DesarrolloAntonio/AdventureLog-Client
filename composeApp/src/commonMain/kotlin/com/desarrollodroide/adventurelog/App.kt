package com.desarrollodroide.adventurelog

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import com.desarrollodroide.adventurelog.feature.ui.di.ProvideImageDependencies
import com.desarrollodroide.adventurelog.feature.ui.di.SessionTokenManager
import com.desarrollodroide.adventurelog.navigation.AdventureLogNavGraph
import com.desarrollodroide.adventurelog.theme.AppTheme
import io.ktor.client.HttpClient
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
@Preview
fun App() {
    DevelopmentEntryPoint {
        AppTheme {
            KoinContext {
                // Inject dependencies once at top level
                val sessionTokenManager = koinInject<SessionTokenManager>()
                val imageClient = koinInject<HttpClient>(named("imageClient"))
                
                ProvideImageDependencies(
                    sessionTokenManager = sessionTokenManager,
                    imageClient = imageClient
                ) {
                    AdventureLogNavGraph(snackbarHostState = remember { SnackbarHostState() })
                }
            }
        }
    }
}