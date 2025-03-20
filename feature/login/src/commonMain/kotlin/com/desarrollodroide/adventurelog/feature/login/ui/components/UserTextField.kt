package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign

@Composable
fun UserTextField(
    user: String,
    userError: Boolean,
    onUserChange: (String) -> Unit
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
                .height(55.dp), // Set a fixed, compact height
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
            shape = RoundedCornerShape(30.dp)
        )
        Crossfade(
            targetState = userError,
            label = "Error Message"
        ) { isError ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp), // Reduced top padding
                textAlign = TextAlign.End,
                color = colorScheme.error,
                text = if (isError) "Invalid username" else ""
            )
        }
    }
}
