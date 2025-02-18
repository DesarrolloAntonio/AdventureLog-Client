package com.desarrollodroide.adventurelog

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.reload.DevelopmentEntryPoint
import com.desarrollodroide.adventurelog.feature.login.login.LoginScreen
import com.desarrollodroide.adventurelog.theme.AppTheme
import org.koin.compose.KoinContext

@Composable
@Preview
fun App() {
    DevelopmentEntryPoint {
        AppTheme {
            KoinContext {
                LoginScreen()
            }
        }
    }
}

