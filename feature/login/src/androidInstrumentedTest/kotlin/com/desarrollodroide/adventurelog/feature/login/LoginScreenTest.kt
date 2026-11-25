package com.desarrollodroide.adventurelog.feature.login

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import com.desarrollodroide.adventurelog.feature.login.ui.screen.LoginScreen
import org.junit.Assert.assertEquals
import org.junit.Test

/** Sign-in as the canvas draws it: the headline, three labelled fields, one big button. */
@OptIn(ExperimentalTestApi::class)
class LoginScreenTest {

    private fun form(urlError: Boolean = false, passwordError: Boolean = false) = LoginFormState(
        userName = "claude", password = "secret", serverUrl = "https://example.test",
        rememberSession = false, userNameError = false, passwordError = passwordError, urlError = urlError,
    )

    @Test
    fun theFormIsLabelledAndSignsIn() = runComposeUiTest {
        var signIns = 0
        setContent {
            LoginScreen(
                loginUiState = LoginUiState.Empty, loginFormState = form(),
                onNavigateToHome = {}, onUserNameChange = {}, onPasswordChange = {}, onServerUrlChange = {},
                onCheckedRememberSessionChange = {}, onClickLoginButton = { signIns++ }, clearErrors = {},
            )
        }

        onNodeWithText("on your server", substring = true).assertIsDisplayed()
        onNodeWithText("Server").assertIsDisplayed()
        onNodeWithText("Username").assertIsDisplayed()
        onNodeWithText("Password").assertIsDisplayed()
        onNodeWithText("Sign in").performScrollTo().performClick()
        assertEquals(1, signIns)
        onNodeWithText("How do I set up a server?").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun thePasswordCanBeShown() = runComposeUiTest {
        setContent {
            LoginScreen(
                loginUiState = LoginUiState.Empty, loginFormState = form(),
                onNavigateToHome = {}, onUserNameChange = {}, onPasswordChange = {}, onServerUrlChange = {},
                onCheckedRememberSessionChange = {}, onClickLoginButton = {}, clearErrors = {},
            )
        }

        onNodeWithContentDescription("Show password").performScrollTo().performClick()
        onNodeWithText("secret").assertIsDisplayed()
        onNodeWithContentDescription("Hide password").assertIsDisplayed()
    }

    @Test
    fun anErrorIsSaidUnderItsField() = runComposeUiTest {
        setContent {
            LoginScreen(
                loginUiState = LoginUiState.Empty, loginFormState = form(urlError = true, passwordError = true),
                onNavigateToHome = {}, onUserNameChange = {}, onPasswordChange = {}, onServerUrlChange = {},
                onCheckedRememberSessionChange = {}, onClickLoginButton = {}, clearErrors = {},
            )
        }

        onNodeWithText("Invalid server url").assertIsDisplayed()
        onNodeWithText("Required").performScrollTo().assertIsDisplayed()
    }
}
