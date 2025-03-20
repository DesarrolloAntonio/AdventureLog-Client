package com.desarrollodroide.adventurelog.feature.login.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.login.ui.components.LoginButton
import com.desarrollodroide.adventurelog.feature.login.ui.components.PasswordTextField
import com.desarrollodroide.adventurelog.feature.login.ui.components.RememberSessionSection
import com.desarrollodroide.adventurelog.feature.login.ui.components.ServerUrlTextField
import com.desarrollodroide.adventurelog.feature.login.ui.components.UserTextField

/**
 * Provides previews for individual login components in Android Studio.
 */
@Preview(
    name = "Server URL TextField",
    showBackground = true,
    widthDp = 360
)
@Composable
fun ServerUrlTextFieldPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                ServerUrlTextField(
                    serverUrl = "https://example-server.com",
                    serverErrorState = false,
                    onValueChange = {},
                    onClick = {}
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ServerUrlTextField(
                    serverUrl = "invalid-url",
                    serverErrorState = true,
                    onValueChange = {},
                    onClick = {}
                )
            }
        }
    }
}

@Preview(
    name = "User TextField",
    showBackground = true,
    widthDp = 360
)
@Composable
fun UserTextFieldPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                UserTextField(
                    user = "user@example.com",
                    userError = false,
                    onUserChange = {}
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                UserTextField(
                    user = "invalid",
                    userError = true,
                    onUserChange = {}
                )
            }
        }
    }
}

@Preview(
    name = "Password TextField",
    showBackground = true,
    widthDp = 360
)
@Composable
fun PasswordTextFieldPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                PasswordTextField(
                    password = "password123",
                    passwordError = false,
                    onPasswordChange = {}
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PasswordTextField(
                    password = "",
                    passwordError = true,
                    onPasswordChange = {}
                )
            }
        }
    }
}

@Preview(
    name = "Login Button",
    showBackground = true,
    widthDp = 360
)
@Composable
fun LoginButtonPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                LoginButton(
                    onClickLoginButton = {}
                )
            }
        }
    }
}

@Preview(
    name = "Remember Session Section",
    showBackground = true,
    widthDp = 360
)
@Composable
fun RememberSessionSectionPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                RememberSessionSection(
                    checked = false,
                    onCheckedChange = {}
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RememberSessionSection(
                    checked = true,
                    onCheckedChange = {}
                )
            }
        }
    }
}