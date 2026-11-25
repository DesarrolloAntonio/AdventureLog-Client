package com.desarrollodroide.adventurelog.feature.login.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.feature.login.ui.components.KeyboardDismissible
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
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

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

    LaunchedEffect(loginUiState) {
        if (loginUiState is LoginUiState.Success) {
            onNavigateToHome.invoke()
        } else if (loginUiState is LoginUiState.Error) {
            clearErrors()
            // A login error is the reason you are still on this screen, so it waits to be read
            // rather than timing out: four seconds is not enough for a sentence that explains
            // which address to use. Shown from the effect's own scope so pressing Login again
            // cancels it - the previous complaint should not outlive the attempt that caused it.
            snackbarHostState.showSnackbar(
                message = loginUiState.message,
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
        }
    }
    
    // No form while the stored session is checked or once login has succeeded - but a spinner for
    // the check: it can take as long as the server takes to answer, and a blank screen for that
    // long reads as a frozen app. A login the user started keeps its form, under the LoadingDialog
    // below, which this early return used to make unreachable (measured: 72 s of blank screen).
    if (loginUiState is LoginUiState.CheckingSession) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (loginUiState is LoginUiState.Success) {
        return
    }

    KeyboardDismissible(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            ContentViews(
                loginFormState = loginFormState,
                onUserNameChange = onUserNameChange,
                onPasswordChange = onPasswordChange,
                onServerUrlChange = onServerUrlChange,
                onCheckedRememberSessionChange = onCheckedRememberSessionChange,
                onClickLoginButton = onClickLoginButton
            )

            // Use the reusable LoadingDialog component for login action
            LoadingDialog(
                isLoading = loginUiState is LoginUiState.Loading,
                showMessage = false
            )

            // SnackbarHost positioned at the bottom
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * The sign-in page as the canvas draws it: a fjord at sunset in a rounded frame with the app's
 * mark, "Your travels, on your server" large, then the form on the page itself - no card.
 */
@Composable
fun ContentViews(
    loginFormState: LoginFormState,
    onUserNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onServerUrlChange: (String) -> Unit,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    onClickLoginButton: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current

    // A Surface, not a painted Box: it also sets the content colour, or the headline came out
    // black on the dark page.
    Surface(Modifier.fillMaxSize(), color = colors.background) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        // A third of the window at most: in landscape the whole picture left no room for the
        // form above the keyboard.
        val heroHeight = minOf(300.dp, maxHeight * 0.36f)
        // Scrolls and keeps clear of the bars and the keyboard: in landscape the button sat below
        // the bottom of the screen with no way to reach it (measured).
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            FjordHero(
                Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .height(heroHeight)
            )
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Your travels,\non ")
                        withStyle(SpanStyle(color = colors.primary)) { append("your") }
                        append(" server")
                    },
                    style = AdventureType.display(50.sp),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Sign in to the AdventureLog instance you host.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ServerUrlTextField(
                        serverUrl = loginFormState.serverUrl,
                        serverErrorState = loginFormState.urlError,
                        onValueChange = onServerUrlChange,
                        onClick = { }
                    )
                    UserTextField(
                        user = loginFormState.userName,
                        userError = loginFormState.userNameError,
                        onUserChange = onUserNameChange
                    )
                    PasswordTextField(
                        password = loginFormState.password,
                        passwordError = loginFormState.passwordError,
                        onPasswordChange = onPasswordChange,
                        onDone = onClickLoginButton
                    )
                }
                RememberSessionSection(
                    checked = loginFormState.rememberSession,
                    onCheckedChange = onCheckedRememberSessionChange
                )
                Spacer(Modifier.height(6.dp))
                LoginButton(onClickLoginButton = onClickLoginButton)
                TextButton(
                    onClick = { uriHandler.openUri("https://github.com/seanmorley15/AdventureLog") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text("How do I set up a server?", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    }
}

/**
 * A fjord at sunset, drawn rather than shipped as an image: the canvas's illustration, its
 * coordinates on a 390 x 300 board scaled to fill the frame. The same in both themes - it is a
 * picture, not chrome.
 */
@Composable
private fun FjordHero(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val chip = if (colors.background.luminance() < 0.5f) colors.surfaceContainer else colors.surfaceContainerLowest
    Box(modifier.clip(RoundedCornerShape(36.dp))) {
        Canvas(
            Modifier
                .matchParentSize()
                .semantics { contentDescription = "Illustration of a fjord at sunset" }
        ) {
            // "slice": scale to cover, centred.
            val k = maxOf(size.width / 390f, size.height / 300f)
            val dx = (size.width - 390f * k) / 2f
            val dy = (size.height - 300f * k) / 2f
            fun p(x: Float, y: Float) = Offset(dx + x * k, dy + y * k)
            fun poly(vararg xy: Float) = Path().apply {
                moveTo(p(xy[0], xy[1]).x, p(xy[0], xy[1]).y)
                var i = 2
                while (i < xy.size) { val o = p(xy[i], xy[i + 1]); lineTo(o.x, o.y); i += 2 }
                close()
            }
            drawRect(
                Brush.verticalGradient(
                    0f to Color(0xFF7F9BC4), 0.6f to Color(0xFFEDB59B), 1f to Color(0xFFF8D9B4),
                    startY = dy, endY = dy + 300f * k,
                )
            )
            drawCircle(Color(0xFFFFF3DE), radius = 18f * k, center = p(290f, 150f))
            drawPath(poly(0f, 170f, 50f, 140f, 104f, 160f, 170f, 124f, 236f, 156f, 300f, 130f, 352f, 150f, 390f, 136f, 390f, 300f, 0f, 300f), Color(0xFFB49AA6).copy(alpha = 0.8f))
            drawPath(poly(0f, 200f, 70f, 168f, 140f, 192f, 214f, 160f, 282f, 188f, 340f, 172f, 390f, 182f, 390f, 300f, 0f, 300f), Color(0xFF7A7389))
            drawRect(Color(0xFF3E5A72), topLeft = p(0f, 214f), size = Size(390f * k, 86f * k))
            drawPath(poly(0f, 300f, 0f, 210f, 140f, 194f, 258f, 186f, 270f, 196f, 168f, 212f, 104f, 300f), Color(0xFF2F2A2A))
            drawCircle(Color(0xFF141414), radius = 3f * k, center = p(250f, 176f))
        }
        Row(
            Modifier
                .padding(16.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(chip)
                .padding(start = 6.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Terrain, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
            }
            Text("AdventureLog", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
        }
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
