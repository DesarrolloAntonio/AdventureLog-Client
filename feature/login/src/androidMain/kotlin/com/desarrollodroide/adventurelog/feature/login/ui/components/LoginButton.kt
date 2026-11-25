package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

/** The form's one big action: a full-width pill, 60dp, with an arrow. */
@Composable
fun LoginButton(
    onClickLoginButton: () -> Unit,
    text: String = "Sign in",
) {
    Button(
        onClick = onClickLoginButton,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Preview
@Composable
private fun LoginButtonPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                LoginButton(onClickLoginButton = {})
            }
        }
    }
}
