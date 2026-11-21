package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RememberSessionSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // The whole row is the control, so the words are both what TalkBack reads and something to
    // tap; they were a separate, dead label next to a nameless box. 48dp, not 40: the box's own
    // touch target overflowed the row into the Login button above it.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
        Text(
            text = "Remember me",
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}

@Preview
@Composable
private fun RememberSessionSectionPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                RememberSessionSection(
                    checked = false,
                    onCheckedChange = {}
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                RememberSessionSection(
                    checked = true,
                    onCheckedChange = {}
                )
            }
        }
    }
}
