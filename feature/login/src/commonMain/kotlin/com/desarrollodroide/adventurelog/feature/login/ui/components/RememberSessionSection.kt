package com.desarrollodroide.adventurelog.feature.login.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RememberSessionSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
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
