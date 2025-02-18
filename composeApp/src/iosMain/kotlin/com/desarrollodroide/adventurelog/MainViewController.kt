package com.desarrollodroide.adventurelog

import androidx.compose.ui.window.ComposeUIViewController
import com.desarrollodroide.adventurelog.di.appModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
            modules(appModule)
        }
    }
) { App() }