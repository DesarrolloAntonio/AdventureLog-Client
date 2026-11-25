package com.desarrollodroide.adventurelog.feature.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import com.desarrollodroide.adventurelog.feature.login.ui.screen.LoginScreen
import org.junit.Assert.assertEquals
import org.junit.Test

/** The second step of signing in to an account with two-step verification. */
@OptIn(ExperimentalTestApi::class)
class LoginCodeScreenTest {

    private val form = LoginFormState(userName = "claude", password = "secret", serverUrl = "https://qa.test")

    @Test
    fun theCodeIsTypedAndVerified() = runComposeUiTest {
        var state by mutableStateOf(LoginUiState.CodeRequired())
        var verified = 0
        setContent {
            LoginScreen(
                loginUiState = state, loginFormState = form,
                onNavigateToHome = {}, onUserNameChange = {}, onPasswordChange = {}, onServerUrlChange = {},
                onCheckedRememberSessionChange = {}, onClickLoginButton = {}, clearErrors = {},
                onLoginCodeChange = { state = state.copy(code = it) },
                onVerifyLoginCode = { verified++ },
                onCancelLoginCode = {},
            )
        }

        onNodeWithText("step", substring = true).assertIsDisplayed()
        onNodeWithText("authenticator app", substring = true).assertIsDisplayed()
        // The field is focused on arrival, so the keyboard is already up for the code.
        onNode(hasSetTextAction()).assertIsFocused()
        onNode(hasSetTextAction()).performTextInput("123456")
        assertEquals("123456", state.code)
        onNodeWithText("Verify").performScrollTo().performClick()
        assertEquals(1, verified)
        onNodeWithText("recovery codes", substring = true).assertIsDisplayed()
    }

    @Test
    fun aWrongCodeIsSaidUnderTheField() = runComposeUiTest {
        setContent {
            LoginScreen(
                loginUiState = LoginUiState.CodeRequired(error = "That code isn't right."), loginFormState = form,
                onNavigateToHome = {}, onUserNameChange = {}, onPasswordChange = {}, onServerUrlChange = {},
                onCheckedRememberSessionChange = {}, onClickLoginButton = {}, clearErrors = {},
            )
        }

        onNodeWithText("That code isn't right.").assertIsDisplayed()
    }

    @Test
    fun theWayBackGoesToThePassword() = runComposeUiTest {
        var cancelled = 0
        setContent {
            LoginScreen(
                loginUiState = LoginUiState.CodeRequired(), loginFormState = form,
                onNavigateToHome = {}, onUserNameChange = {}, onPasswordChange = {}, onServerUrlChange = {},
                onCheckedRememberSessionChange = {}, onClickLoginButton = {}, clearErrors = {},
                onCancelLoginCode = { cancelled++ },
            )
        }

        onNodeWithContentDescription("Back to sign in").performClick()
        onNodeWithText("Back to sign in").performScrollTo().performClick()
        assertEquals(2, cancelled)
    }
}
