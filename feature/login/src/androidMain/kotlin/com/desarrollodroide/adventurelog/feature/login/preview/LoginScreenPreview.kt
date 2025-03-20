package com.desarrollodroide.adventurelog.feature.login.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.login.ui.screen.LoginScreen
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState

/**
 * Provides previews for the Login Screen in Android Studio.
 */
@Preview(
    name = "Login Screen - Light Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun LoginScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                loginUiState = LoginUiState.Empty,
                loginFormState = LoginFormState(
                    userName = "user@example.com",
                    password = "password123",
                    serverUrl = "https://example-server.com",
                    rememberSession = false,
                    userNameError = false,
                    passwordError = false,
                    urlError = false
                ),
                onNavigateToHome = {},
                onUserNameChange = {},
                onPasswordChange = {},
                onServerUrlChange = {},
                onCheckedRememberSessionChange = {},
                onClickLoginButton = {},
                clearErrors = {}
            )
        }
    }
}

/**
 * Preview of Login Screen with dark theme
 */
@Preview(
    name = "Login Screen - Dark Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun LoginScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                loginUiState = LoginUiState.Empty,
                loginFormState = LoginFormState(
                    userName = "user@example.com",
                    password = "password123",
                    serverUrl = "https://example-server.com",
                    rememberSession = true,
                    userNameError = false,
                    passwordError = false,
                    urlError = false
                ),
                onNavigateToHome = {},
                onUserNameChange = {},
                onPasswordChange = {},
                onServerUrlChange = {},
                onCheckedRememberSessionChange = {},
                onClickLoginButton = {},
                clearErrors = {}
            )
        }
    }
}

/**
 * Preview of Login Screen with validation errors
 */
@Preview(
    name = "Login Screen - With Errors",
    showBackground = true,
    heightDp = 800
)
@Composable
fun LoginScreenWithErrorsPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                loginUiState = LoginUiState.Error("Invalid credentials"),
                loginFormState = LoginFormState(
                    userName = "invalid",
                    password = "",
                    serverUrl = "invalid-url",
                    rememberSession = false,
                    userNameError = true,
                    passwordError = true,
                    urlError = true
                ),
                onNavigateToHome = {},
                onUserNameChange = {},
                onPasswordChange = {},
                onServerUrlChange = {},
                onCheckedRememberSessionChange = {},
                onClickLoginButton = {},
                clearErrors = {}
            )
        }
    }
}

/**
 * Preview of Login Screen with loading state
 */
@Preview(
    name = "Login Screen - Loading",
    showBackground = true,
    heightDp = 800
)
@Composable
fun LoginScreenLoadingPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            LoginScreen(
                loginUiState = LoginUiState.Loading,
                loginFormState = LoginFormState(
                    userName = "user@example.com",
                    password = "password123",
                    serverUrl = "https://example-server.com",
                    rememberSession = false,
                    userNameError = false,
                    passwordError = false,
                    urlError = false
                ),
                onNavigateToHome = {},
                onUserNameChange = {},
                onPasswordChange = {},
                onServerUrlChange = {},
                onCheckedRememberSessionChange = {},
                onClickLoginButton = {},
                clearErrors = {}
            )
        }
    }
}