package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.Item
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SwitchOption(
    item: Item,
    switchState: MutableStateFlow<Boolean>,
) {
    val switchValue by switchState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { switchState.value = !switchValue },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(item.icon, contentDescription = item.title)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = item.title, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(5.dp))
        Switch(
            checked = switchValue,
            onCheckedChange = { newValue ->
                switchState.value = newValue
            }
        )
    }
}

@Composable
fun SwitchOption(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) } // Permite hacer clic en cualquier parte de la fila
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}


