package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PasswordTextField(
    password: String,
    passwordError: Boolean,
    onPasswordChange: (String) -> Unit,
    onDone: () -> Unit = {}
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    LoginField(
        value = password,
        onValueChange = onPasswordChange,
        label = "Password",
        icon = Icons.Outlined.Lock,
        isError = passwordError,
        errorText = "Required",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        // The keyboard's Done key is the last field's way of saying "log in"; without an
        // action it only closed the keyboard and sent nothing (measured: 0 login requests).
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password",
                )
            }
        },
    )
}

@Preview
@Composable
private fun PasswordTextFieldPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PasswordTextField(password = "password123", passwordError = false, onPasswordChange = {})
                PasswordTextField(password = "", passwordError = true, onPasswordChange = {})
            }
        }
    }
}
