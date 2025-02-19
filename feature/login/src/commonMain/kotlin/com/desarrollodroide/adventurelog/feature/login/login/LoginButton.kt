package com.desarrollodroide.adventurelog.feature.login.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoginButton(
    onClickLoginButton: () -> Unit
) {
    Button(
        onClick = {
            onClickLoginButton()
        },
        modifier = Modifier.fillMaxWidth(),
        content = {
            Text("Login")
        },
    )
}