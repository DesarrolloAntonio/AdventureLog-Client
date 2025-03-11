package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewCompactAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeedSection(
    compactView: Boolean,
    onCompactViewChanged: (Boolean) -> Unit,
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 5.dp)
    ) {
        Text(
            text = "Adventure list",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(5.dp))
        SwitchOption(
            title = "Compact view",
            icon = Icons.Filled.ViewCompactAlt,
            checked = compactView,
            onCheckedChange = onCompactViewChanged
        )
    }
}