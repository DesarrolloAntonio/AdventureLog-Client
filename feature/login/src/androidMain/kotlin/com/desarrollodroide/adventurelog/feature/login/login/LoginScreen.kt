package com.desarrollodroide.adventurelog.feature.login.login

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.login.LoginViewModel
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState


@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onSuccess: (UserDetails) -> Unit,
) {
    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LoginContent(
            loginUiState = loginUiState,
            checked = loginViewModel.rememberSession,
            userErrorState = loginViewModel.userNameError,
            passwordErrorState = loginViewModel.passwordError,
            urlErrorState = loginViewModel.urlError,
            onClickLoginButton = {

            },
            onCheckedRememberSessionChange = {
                loginViewModel.rememberSession.value = it
            },
            onSuccess = {
                //loginViewModel.clearState()
                onSuccess.invoke(it)
            },
            user = loginViewModel.userName,
            password = loginViewModel.password,
            serverUrl = loginViewModel.serverUrl,
            onClearError = {
                //loginViewModel.clearState()
            },
            onClickTestButton = {
                //loginViewModel.checkServerAvailability()
            },
            resetServerAvailabilityState = {
               //loginViewModel.resetServerAvailabilityUiState()
            }
        )
    }
}

@Composable
fun LoginContent(
    user: MutableState<String>,
    password: MutableState<String>,
    serverUrl: MutableState<String>,
    checked: MutableState<Boolean>,
    urlErrorState: MutableState<Boolean>,
    userErrorState: MutableState<Boolean>,
    passwordErrorState: MutableState<Boolean>,
    onSuccess: (UserDetails) -> Unit,
    onClickLoginButton: () -> Unit,
    onClickTestButton: () -> Unit,
    onClearError: () -> Unit,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
    loginUiState: LoginUiState,
    resetServerAvailabilityState: () -> Unit
) {
    when (loginUiState) {
        is LoginUiState.Error -> {
            onClearError.invoke()
        }

        LoginUiState.Empty -> {

        }
        LoginUiState.Loading -> {
        }
        LoginUiState.Success -> {
            ContentViews(
                serverUrl = serverUrl,
                urlErrorState = urlErrorState,
                user = user,
                userErrorState = userErrorState,
                password = password,
                passwordErrorState = passwordErrorState,
                onClickLoginButton = onClickLoginButton,
                checked = checked,
                onCheckedRememberSessionChange = onCheckedRememberSessionChange,
                onClickTestButton = onClickTestButton,
            )
        }
    }
}

@Composable
private fun ContentViews(
    serverUrl: MutableState<String>,
    urlErrorState: MutableState<Boolean>,
    user: MutableState<String>,
    userErrorState: MutableState<Boolean>,
    password: MutableState<String>,
    passwordErrorState: MutableState<Boolean>,
    onClickLoginButton: () -> Unit,
    onClickTestButton: () -> Unit,
    checked: MutableState<Boolean>,
    onCheckedRememberSessionChange: (Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
//        Image(
//            painter = painterResource(id = R.drawable.ic_logo),
//            contentDescription = null,
//            contentScale = ContentScale.FillHeight,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 20.dp)
//                .height(120.dp)
//        )
//        Image(
//            painter = painterResource(id = R.drawable.curved_wave_bottom),
//            contentDescription = null,
//            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 10.dp)
//                .height(150.dp)
//                .align(Alignment.BottomCenter)
//        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Bottom,
        ) {
            ServerUrlTextField(
                modifier = Modifier,
                serverUrl = serverUrl,
                serverErrorState = urlErrorState,
                onClick = onClickTestButton,
            )
            Spacer(modifier = Modifier.height(10.dp))
            UserTextField(
                user = user,
                userErrorState = userErrorState
            )
            Spacer(modifier = Modifier.height(10.dp))
            PasswordTextField(
                password = password,
                passwordErrorState = passwordErrorState
            )
            Spacer(Modifier.size(14.dp))
            LoginButton(
                user = user,
                userErrorState = userErrorState,
                password = password,
                passwordErrorState = passwordErrorState,
                onClickLoginButton = onClickLoginButton,
                serverErrorState = urlErrorState
            )
            RememberSessionSection(
                checked = checked,
                onCheckedChange = onCheckedRememberSessionChange
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DefaultPreview() {
    MaterialTheme(
    ) {
        LoginContent(
            user = remember { mutableStateOf("User") },
            password = remember { mutableStateOf("Pass") },
            serverUrl = remember { mutableStateOf("ServerUrl") },
            checked = remember { mutableStateOf(true) },
            urlErrorState = remember { mutableStateOf(true) },
            userErrorState = remember { mutableStateOf(true) },
            passwordErrorState = remember { mutableStateOf(true) },
            onSuccess = {},
            onClickLoginButton = {},
            onCheckedRememberSessionChange = {},
            onClearError = {},
            loginUiState = LoginUiState.Success,
            onClickTestButton = {},
            resetServerAvailabilityState = {}
        )
    }
}