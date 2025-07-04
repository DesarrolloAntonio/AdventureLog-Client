package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun UserTextField(
    user: String,
    userError: Boolean,
    onUserChange: (String) -> Unit,
    onNext: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        OutlinedTextField(
            value = user,
            onValueChange = onUserChange,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            placeholder = {
                Text(
                    text = "User",
                    color = Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            isError = userError,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colorScheme.surface,
                focusedContainerColor = colorScheme.surface,
                unfocusedBorderColor = if (userError) colorScheme.error else Color.Transparent,
                focusedBorderColor = if (userError) colorScheme.error else colorScheme.primary
            ),
            shape = RoundedCornerShape(30.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() }
            )
        )
        Crossfade(
            targetState = userError,
            label = "Error Message"
        ) { isError ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                textAlign = TextAlign.End,
                color = colorScheme.error,
                text = if (isError) "Invalid username" else ""
            )
        }
    }
}

@Preview
@Composable
private fun UserTextFieldPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                UserTextField(
                    user = "user@example.com",
                    userError = false,
                    onUserChange = {},
                    onNext = {}
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                UserTextField(
                    user = "invalid",
                    userError = true,
                    onUserChange = {},
                    onNext = {}
                )
            }
        }
    }
}
