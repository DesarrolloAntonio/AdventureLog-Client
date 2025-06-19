package com.desarrollodroide.adventurelog.feature.login.ui.screen

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import com.desarrollodroide.adventurelog.feature.login.ui.components.LoginButton
import com.desarrollodroide.adventurelog.feature.login.ui.components.PasswordTextField
import com.desarrollodroide.adventurelog.feature.login.ui.components.RememberSessionSection
import com.desarrollodroide.adventurelog.feature.login.ui.components.ServerUrlTextField
import com.desarrollodroide.adventurelog.feature.login.ui.components.UserTextField
import com.desarrollodroide.adventurelog.feature.login.viewmodel.LoginViewModel
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import com.desarrollodroide.adventurelog.feature.login.ui.navigation.LoginNavigator
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import kotlinx.coroutines.launch

@Composable
fun LoginScreenRoute(
    viewModel: LoginViewModel,
    navigator: LoginNavigator
) {
    val loginUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loginFormState by viewModel.loginFormState.collectAsStateWithLifecycle()

    LoginScreen(
        loginUiState = loginUiState,
        loginFormState = loginFormState,
        onNavigateToHome = { navigator.goToHome() },
        onUserNameChange = viewModel::updateUserName,
        onPasswordChange = viewModel::updatePassword,
        onServerUrlChange = viewModel::updateServerUrl,
        onCheckedRememberSessionChange = viewModel::updateRememberSession,
        onClickLoginButton = viewModel::login,
        clearErrors = viewModel::clearErrors
    )
}

@Composable
internal fun LoginScreen(
    loginUiState: LoginUiState,
    loginFormState: LoginFormState,
    onNavigateToHome: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onServerUrlChange: (String) -> Unit,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    onClickLoginButton: () -> Unit,
    clearErrors: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginUiState) {
        if (loginUiState is LoginUiState.Success) {
            onNavigateToHome.invoke()
        } else if (loginUiState is LoginUiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = loginUiState.message,
                    duration = SnackbarDuration.Short
                )
            }
            clearErrors()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        ContentViews(
            loginFormState = loginFormState,
            onUserNameChange = onUserNameChange,
            onPasswordChange = onPasswordChange,
            onServerUrlChange = onServerUrlChange,
            onCheckedRememberSessionChange = onCheckedRememberSessionChange,
            onClickLoginButton = onClickLoginButton,
            onClickTestButton = { }
        )

        // Use the reusable LoadingDialog component
        LoadingDialog(
            isLoading = loginUiState is LoginUiState.Loading,
            showMessage = false
        )

        if (loginUiState is LoginUiState.Error) {
            clearErrors()
        }

        // SnackbarHost positioned at the bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
        BubbleBackground(Modifier.fillMaxWidth(), maxHeightFactor = 0.45f)

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
                    .padding(bottom = 24.dp)
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
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    ServerUrlTextField(
                        serverUrl = loginFormState.serverUrl,
                        serverErrorState = loginFormState.urlError,
                        onValueChange = onServerUrlChange,
                        onClick = { }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UserTextField(
                        user = loginFormState.userName,
                        userError = loginFormState.userNameError,
                        onUserChange = onUserNameChange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordTextField(
                        password = loginFormState.password,
                        passwordError = loginFormState.passwordError,
                        onPasswordChange = onPasswordChange
                    )
                    Spacer(Modifier.size(10.dp))
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
                            .padding(vertical = 12.dp)
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(buildAnnotatedString {
                            append("View instructions guide in ")
                            withLink(LinkAnnotation.Url(url = "https://github.com/seanmorley15/AdventureLog")) {
                                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                                    append("website")
                                }
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

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun LoginScreenLightPreview() {
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

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun LoginScreenDarkPreview() {
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

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun LoginScreenWithErrorsPreview() {
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

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun LoginScreenLoadingPreview() {
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
