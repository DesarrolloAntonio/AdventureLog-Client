package com.desarrollodroide.adventurelog.feature.login.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import isValidUrl

@Composable
fun ServerUrlTextField(
    serverUrl: MutableState<String>,
    serverErrorState: MutableState<Boolean>,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Column {
        OutlinedTextField(
            value = serverUrl.value,
            leadingIcon = {
                Icon(
                    tint = Color.Gray,
                    imageVector = Icons.Filled.Link,
                    contentDescription = null
                )
            },
            placeholder = {
                Text(
                    text = "Server URL",
                    color = Color.Gray
                )
            },
            onValueChange = {
                serverErrorState.value = !isValidUrl(it)
                serverUrl.value = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (isFocused && !focusState.isFocused && isValidUrl(serverUrl.value)) {
                        onClick()
                    }
                    isFocused = focusState.isFocused
                },
            singleLine = true,
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colorScheme.surface,
                focusedContainerColor = colorScheme.surface,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colorScheme.primary
            ),
            shape = RoundedCornerShape(30.dp)
        )

        Crossfade(
            targetState = serverErrorState.value,
            label = "Error Message"
        ) { isError ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End,
                color = colorScheme.error,
                text = if (isError) "Invalid server url" else ""
            )
        }
    }
}