package com.desarrollodroide.adventurelog.feature.login.login

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PasswordTextField(
    password: String,
    passwordError: Boolean,
    onPasswordChange: (String) -> Unit
) {
    val passwordVisibility = remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Column {
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Password",
                    color = Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility.value = !passwordVisibility.value
                }) {
                    Icon(
                        imageVector = if (passwordVisibility.value) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = Color.Gray
                    )
                }
            },
            isError = passwordError,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colorScheme.surface,
                focusedContainerColor = colorScheme.surface,
                unfocusedBorderColor = if (passwordError) colorScheme.error else Color.Transparent,
                focusedBorderColor = if (passwordError) colorScheme.error else colorScheme.primary
            ),
            shape = RoundedCornerShape(30.dp),
            visualTransformation = if (passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation()
        )
        Crossfade(
            targetState = passwordError,
            label = "Error Message"
        ) { isError ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End,
                color = colorScheme.error,
                text = if (isError) "Required" else ""
            )
        }
    }
}