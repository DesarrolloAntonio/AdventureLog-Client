package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * The search field used on Locations, Collections and World.
 *
 * Two things it deliberately does not do any more. It no longer swaps its magnifying glass for a
 * filled circle once you have typed three characters - an icon that turns into a button under you
 * is a moving target, and below three characters nothing happened and nothing said why. And it no
 * longer empties itself when you search: the text you looked for stays in the field, in real ink
 * rather than as placeholder grey, so refining a search is editing it rather than retyping it.
 *
 * [onSearchSubmit] tells the two apart. Given one, the screen searches when you ask it to, and the
 * field offers Search while what you have typed differs from what is applied. Given none, the
 * screen filters as you type and the field is just a field.
 */
@Composable
fun SimpleSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    enabled: Boolean = true,
    onSearchSubmit: (() -> Unit)? = null,
    appliedQuery: String = ""
) {
    val focusManager = LocalFocusManager.current
    val trimmed = searchQuery.trim()
    val canSubmit = onSearchSubmit != null && trimmed.isNotEmpty() && trimmed != appliedQuery.trim()

    fun submit() {
        onSearchSubmit?.invoke()
        focusManager.clearFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(18.dp))
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))

        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                imeAction = if (onSearchSubmit != null) ImeAction.Search else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onSearch = { if (canSubmit) submit() },
                onDone = { focusManager.clearFocus() }
            ),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    field()
                }
            }
        )

        // Search while there is something new to look for, then the way to undo it. Only one of
        // the two can apply at a time, so the row never grows a third control.
        AnimatedVisibility(
            visible = canSubmit,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            TextButton(onClick = { submit() }) { Text("Search") }
        }
        AnimatedVisibility(
            visible = !canSubmit && searchQuery.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = {
                    onSearchQueryChange("")
                    // With a submitted search still applied, emptying the box is only half the
                    // job - the results have to come back too.
                    if (onSearchSubmit != null && appliedQuery.isNotEmpty()) onSearchSubmit()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear the search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(6.dp))
    }
}
