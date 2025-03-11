package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.Item

@Composable
fun ClickableOption(
    item: Item,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(item.icon, contentDescription = item.title)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 10.dp),
                text = item.title
            )
            if (item.subtitle.isNotEmpty()){
                Text(
                    modifier = Modifier
                        .padding(horizontal = 10.dp),
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ClickableOption(
    title: String,
    icon: ImageVector,
    subtitle: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(icon, contentDescription = title)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 10.dp),
                text = title
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 10.dp),
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
