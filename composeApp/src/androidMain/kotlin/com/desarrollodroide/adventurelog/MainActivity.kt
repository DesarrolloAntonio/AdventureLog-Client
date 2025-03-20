package com.desarrollodroide.adventurelog

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.login.ui.screen.ContentViews
import com.desarrollodroide.adventurelog.feature.login.model.LoginFormState
import com.desarrollodroide.adventurelog.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val previewLoginFormState = LoginFormState(
        userName = "User",
        password = "Pass",
        serverUrl = "ServerUrl",
        rememberSession = true,
        userNameError = false,
        passwordError = false,
        urlError = false
    )

    AppTheme {
        ContentViews(
            loginFormState = previewLoginFormState,
            onUserNameChange = {},
            onPasswordChange = {},
            onServerUrlChange = {},
            onCheckedRememberSessionChange = {},
            onClickLoginButton = {},
            onClickTestButton = {},
        )
    }
}