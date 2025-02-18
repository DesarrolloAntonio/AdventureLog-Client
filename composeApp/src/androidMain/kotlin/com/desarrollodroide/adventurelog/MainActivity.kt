package com.desarrollodroide.adventurelog

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.login.login.LoginContent
import com.desarrollodroide.adventurelog.feature.login.login.LoginScreen
import com.desarrollodroide.adventurelog.feature.login.model.LoginUiState
import com.desarrollodroide.adventurelog.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            //App()
            AppTheme {
                LoginScreen()
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        LoginContent(
            user = remember { mutableStateOf("User") },
            password = remember { mutableStateOf("Pass") },
            serverUrl = remember { mutableStateOf("ServerUrl") },
            checked = remember { mutableStateOf(true) },
            urlErrorState = remember { mutableStateOf(true) },
            userErrorState = remember { mutableStateOf(true) },
            passwordErrorState = remember { mutableStateOf(true) },
            //onSuccess = {},
            onClickLoginButton = {},
            onCheckedRememberSessionChange = {},
            onClearError = {},
            loginUiState = LoginUiState.Success,
            onClickTestButton = {},
            resetServerAvailabilityState = {}
        )
    }
}