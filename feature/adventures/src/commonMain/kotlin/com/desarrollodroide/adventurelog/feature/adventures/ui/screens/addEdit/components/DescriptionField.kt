package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun DescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        placeholder = { 
            Text(
                text = "Write your adventure description...",
                color = Color.Gray
            ) 
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Default
        ),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}
