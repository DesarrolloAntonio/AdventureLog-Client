package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun UserTextField(
    user: String,
    userError: Boolean,
    onUserChange: (String) -> Unit
) {
    LoginField(
        value = user,
        onValueChange = onUserChange,
        label = "Username",
        icon = Icons.Outlined.Person,
        isError = userError,
        errorText = "Invalid username",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
    )
}

@Preview
@Composable
private fun UserTextFieldPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                UserTextField(user = "user@example.com", userError = false, onUserChange = {})
                UserTextField(user = "invalid", userError = true, onUserChange = {})
            }
        }
    }
}
