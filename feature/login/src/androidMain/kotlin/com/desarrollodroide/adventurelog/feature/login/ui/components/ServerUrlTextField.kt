package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import isValidUrl
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ServerUrlTextField(
    serverUrl: String,
    serverErrorState: Boolean,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    LoginField(
        value = serverUrl,
        onValueChange = onValueChange,
        label = "Server",
        icon = Icons.Outlined.Language,
        isError = serverErrorState,
        errorText = "Invalid server url",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
        modifier = Modifier.onFocusChanged { focusState ->
            if (isFocused && !focusState.isFocused && isValidUrl(serverUrl)) {
                onClick()
            }
            isFocused = focusState.isFocused
        },
    )
}

@Preview
@Composable
private fun ServerUrlTextFieldPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ServerUrlTextField(serverUrl = "https://example-server.com", serverErrorState = false, onValueChange = {}, onClick = {})
                ServerUrlTextField(serverUrl = "invalid-url", serverErrorState = true, onValueChange = {}, onClick = {})
            }
        }
    }
}
