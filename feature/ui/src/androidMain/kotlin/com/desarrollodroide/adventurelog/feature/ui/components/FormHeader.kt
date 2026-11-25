package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The top of every item form, as the canvas draws them: Close, then what the form is making -
 * "New place", "Edit lodging" - large and condensed. [onClose] is the form's guarded leave, the
 * same one Cancel and Back use, so typed work is never dropped without asking.
 */
@Composable
fun FormHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        FilledIconButton(
            onClick = onClose,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
        Text(
            text = title,
            style = AdventureType.display(52.sp),
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
                .semantics { heading() },
        )
    }
}
