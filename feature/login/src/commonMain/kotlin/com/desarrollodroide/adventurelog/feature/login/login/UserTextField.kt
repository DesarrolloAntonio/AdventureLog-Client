package com.desarrollodroide.adventurelog.feature.login.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign

@Composable
fun UserTextField(
    user: MutableState<String>,
    userErrorState: MutableState<Boolean>
) {
    val colorScheme = MaterialTheme.colorScheme

    Column {
        OutlinedTextField(
            value = user.value,
            onValueChange = {
                if (userErrorState.value) {
                    userErrorState.value = false
                }
                user.value = it
            },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
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
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = colorScheme.surface,
                focusedContainerColor = colorScheme.surface,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = colorScheme.primary
            ),
            shape = RoundedCornerShape(30.dp)
        )
        Crossfade(
            targetState = userErrorState.value,
            label = "Error Message"
        ) { isError ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End,
                color = colorScheme.error,
                text = if (isError) "Invalid username" else ""
            )
        }
    }
}



