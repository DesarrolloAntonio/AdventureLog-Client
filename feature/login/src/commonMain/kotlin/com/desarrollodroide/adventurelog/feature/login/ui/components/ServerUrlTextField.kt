package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import isValidUrl
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ServerUrlTextField(
    serverUrl: String,
    serverErrorState: Boolean,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit,
    onNext: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Column {
        OutlinedTextField(
            value = serverUrl,
            onValueChange = {
                onValueChange(it)
            },
            leadingIcon = {
                Icon(
                    tint = Color.Gray,
                    imageVector = Icons.Filled.Link,
                    contentDescription = "Server URL"
                )
            },
            placeholder = {
                Text(
                    text = "Server URL",
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused && isValidUrl(serverUrl)) {
                        onClick()
                    }
                    isFocused = focusState.isFocused
                },
            singleLine = true,
            maxLines = 1,
            isError = serverErrorState,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colorScheme.surface,
                focusedContainerColor = colorScheme.surface,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colorScheme.primary
            ),
            shape = RoundedCornerShape(30.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() }
            )
        )

        Crossfade(
            targetState = serverErrorState,
            label = "Error Message"
        ) { isError ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                textAlign = TextAlign.End,
                color = colorScheme.error,
                text = if (isError) "Invalid server url" else ""
            )
        }
    }
}

@Preview
@Composable
private fun ServerUrlTextFieldPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                ServerUrlTextField(
                    serverUrl = "https://example-server.com",
                    serverErrorState = false,
                    onValueChange = {},
                    onClick = {},
                    onNext = {}
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ServerUrlTextField(
                    serverUrl = "invalid-url",
                    serverErrorState = true,
                    onValueChange = {},
                    onClick = {},
                    onNext = {}
                )
            }
        }
    }
}