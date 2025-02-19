package com.desarrollodroide.adventurelog.feature.login.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import com.desarrollodroide.adventurelog.feature.login.LoginViewModel
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    navigateToHome: (() -> Unit)? = null
) {
    val loginViewModel = koinViewModel<LoginViewModel>()

    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    val loginFormState by loginViewModel.loginFormState.collectAsStateWithLifecycle()

    LaunchedEffect(loginUiState) {
        if (loginUiState is LoginUiState.Success) {
            navigateToHome?.invoke()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ContentViews(
            loginFormState = loginFormState,
            onUserNameChange = loginViewModel::updateUserName,
            onPasswordChange = loginViewModel::updatePassword,
            onServerUrlChange = loginViewModel::updateServerUrl,
            onCheckedRememberSessionChange = loginViewModel::updateRememberSession,
            onClickLoginButton = loginViewModel::login,
            onClickTestButton = { }
        )

        if (loginUiState is LoginUiState.Loading) {

        }

        if (loginUiState is LoginUiState.Error) {
            loginViewModel.clearErrors()
        }
    }
}

@Composable
fun ContentViews(
    loginFormState: LoginFormState,
    onUserNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onServerUrlChange: (String) -> Unit,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    onClickLoginButton: () -> Unit,
    onClickTestButton: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundPrimary = getAdjustedPrimary()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundPrimary)
    ) {
        BubbleBackground(Modifier.fillMaxWidth(), maxHeightFactor = 0.5f)

        Column(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 30.dp),
                text = "Hello!",
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Welcome to AdventureLog",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .padding(horizontal = 30.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 40.dp,
                    topEnd = 40.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 30.dp)
                    )
                    ServerUrlTextField(
                        serverUrl = loginFormState.serverUrl,
                        serverErrorState = loginFormState.urlError,
                        onValueChange = onServerUrlChange,
                        onClick = {  }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    UserTextField(
                        user = loginFormState.userName,
                        userError = loginFormState.userNameError,
                        onUserChange = onUserNameChange
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PasswordTextField(
                        password = loginFormState.password,
                        passwordError = loginFormState.passwordError,
                        onPasswordChange = onPasswordChange
                    )
                    Spacer(Modifier.size(14.dp))
                    LoginButton(
                        onClickLoginButton = onClickLoginButton
                    )
                    RememberSessionSection(
                        checked = loginFormState.rememberSession,
                        onCheckedChange = onCheckedRememberSessionChange
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(buildAnnotatedString {
                            append("View instructions guide in ")
                            withLink(LinkAnnotation.Url(url = "https://github.com/seanmorley15/AdventureLog")) {
                                append("website")
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun getAdjustedPrimary(): Color {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()

    return if (isDarkTheme) {
        // Darken the primary color slightly in dark mode
        colorScheme.primary.copy(
            red = colorScheme.primary.red * 0.6f,
            green = colorScheme.primary.green * 0.6f,
            blue = colorScheme.primary.blue * 0.6f
        )
    } else {
        colorScheme.primary
    }
}

@Composable
fun BubbleBackground(
    modifier: Modifier = Modifier,
    maxHeightFactor: Float = 0.5f
) {
    val colorScheme = MaterialTheme.colorScheme

    val lighterPrimary = lerp(colorScheme.primary, Color.White, 0.3f)
    val lighterSecondary = lerp(colorScheme.secondary, Color.White, 0.3f)
    val lighterTertiary = lerp(colorScheme.tertiary, Color.White, 0.3f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height * maxHeightFactor

        drawCircle(
            color = lighterPrimary.copy(alpha = 0.6f),
            radius = width * 0.25f,
            center = Offset(x = width * 0.2f, y = height * 0.2f)
        )
        drawCircle(
            color = lighterPrimary.copy(alpha = 0.2f),
            radius = width * 0.3f,
            center = Offset(x = width * 0.2f, y = height * 0.2f)
        )

        drawCircle(
            color = lighterSecondary.copy(alpha = 0.5f),
            radius = width * 0.2f,
            center = Offset(x = width * 0.8f, y = height * 0.15f)
        )
        drawCircle(
            color = lighterSecondary.copy(alpha = 0.2f),
            radius = width * 0.25f,
            center = Offset(x = width * 0.8f, y = height * 0.15f)
        )

        drawCircle(
            color = lighterTertiary.copy(alpha = 0.4f),
            radius = width * 0.18f,
            center = Offset(x = width * 0.5f, y = height * 0.35f)
        )
        drawCircle(
            color = lighterTertiary.copy(alpha = 0.15f),
            radius = width * 0.22f,
            center = Offset(x = width * 0.5f, y = height * 0.35f)
        )
    }
}
