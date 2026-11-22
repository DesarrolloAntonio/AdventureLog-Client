package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Standard TextField to be used across the entire app for consistency.
 * 
 * This component ensures all text fields have:
 * - Rounded corners (30.dp)
 * - Consistent colors and styling
 * - Proper error handling
 * - Leading/trailing icon support
 * 
 * @param value The current text value
 * @param onValueChange Callback when text changes
 * @param modifier Modifier for the text field
 * @param label Label text (used as placeholder)
 * @param placeholder Placeholder text (if different from label)
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon or composable
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display when isError is true
 * @param singleLine Whether the field is single line
 * @param maxLines Maximum number of lines
 * @param keyboardOptions Keyboard configuration
 * @param keyboardActions Keyboard actions
 * @param visualTransformation Visual transformation (e.g., for passwords)
 * @param enabled Whether the field is enabled
 * @param readOnly Whether the field is read-only
 */
@Composable
fun StandardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { 
                { 
                    Text(
                        text = it,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                }
            } ?: label?.let {
                { 
                    Text(
                        text = it,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    ) 
                }
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            enabled = enabled,
            readOnly = readOnly,
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                focusedBorderColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = if (isError) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                } else {
                    Color.Transparent
                },
                disabledBorderColor = Color.Transparent,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        // Error message
        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Variant of StandardTextField specifically for search fields.
 * Includes search icon by default and clear button when there's text.
 */
@Composable
fun StandardSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: ((String) -> Unit)? = null,
    enabled: Boolean = true
) {
    StandardTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = Icons.Filled.Search,
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(
                    onClick = { onValueChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                        tint = Color.Gray
                    )
                }
            }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch?.invoke(value) }
        ),
        enabled = enabled
    )
}
