package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoginButton(
    onClickLoginButton: () -> Unit
) {
    Button(
        onClick = {
            onClickLoginButton()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp), // Set a fixed, compact height
        shape = RoundedCornerShape(25.dp), // Rounded corners for better appearance
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        content = {
            Text("Login")
        },
    )
}

@Preview
@Composable
private fun LoginButtonPreview() {
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