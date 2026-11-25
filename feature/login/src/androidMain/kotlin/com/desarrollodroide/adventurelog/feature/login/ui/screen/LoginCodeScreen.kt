package com.desarrollodroide.adventurelog.feature.login.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import com.desarrollodroide.adventurelog.feature.login.ui.components.KeyboardDismissible
import com.desarrollodroide.adventurelog.feature.login.ui.components.LoginButton
import com.desarrollodroide.adventurelog.feature.login.ui.components.LoginField
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import com.desarrollodroide.adventurelog.feature.ui.platform.PlatformBackHandler
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The second step of signing in to an account with two-step verification, on the sign-in page's
 * own ground: the code from the authenticator app (or a recovery code), one big button, and the
 * way back to the password. A wrong code is said under the field and the screen stays; a sign-in
 * that has timed out goes back to the form, which says why.
 */
@Composable
internal fun LoginCodeScreen(
    state: LoginUiState.CodeRequired,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val focus = remember { FocusRequester() }
    // The keyboard comes up with the screen: the code is the only thing to do here, and the
    // authenticator's code only lives 30 seconds.
    LaunchedEffect(Unit) { focus.requestFocus() }
    PlatformBackHandler(onBack = onBack)

    KeyboardDismissible(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Surface(Modifier.fillMaxSize(), color = colors.background) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(top = 8.dp, bottom = 24.dp)
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to sign in")
                        }
                        Column(Modifier.padding(horizontal = 20.dp)) {
                            Spacer(Modifier.height(12.dp))
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(colors.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = colors.onPrimaryContainer,
                                    modifier = Modifier.size(30.dp),
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("One more\n")
                                    withStyle(SpanStyle(color = colors.primary)) { append("step") }
                                },
                                style = AdventureType.display(50.sp),
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Your account has two-step verification. Enter the 6-digit code " +
                                    "from your authenticator app.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(18.dp))
                            LoginField(
                                value = state.code,
                                onValueChange = onCodeChange,
                                label = "Code",
                                icon = Icons.Outlined.Pin,
                                isError = state.error != null,
                                errorText = state.error.orEmpty(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = { onVerify() }),
                                modifier = Modifier.focusRequester(focus),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No access to the app? One of your 8-digit recovery codes works here too.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(18.dp))
                            LoginButton(onClickLoginButton = onVerify, text = "Verify")
                            TextButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Text("Back to sign in", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            LoadingDialog(isLoading = state.verifying, showMessage = false)
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun LoginCodeScreenPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        LoginCodeScreen(LoginUiState.CodeRequired(code = "1234"), onCodeChange = {}, onVerify = {}, onBack = {})
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun LoginCodeScreenErrorDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        LoginCodeScreen(
            LoginUiState.CodeRequired(code = "123456", error = "That code isn't right."),
            onCodeChange = {}, onVerify = {}, onBack = {},
        )
    }
}
