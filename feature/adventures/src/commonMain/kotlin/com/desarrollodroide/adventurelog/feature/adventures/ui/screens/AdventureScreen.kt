package com.desarrollodroide.adventurelog.feature.adventures.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem

@Composable
fun AdventureListScreen(
    adventures: List<Adventure> = PreviewData.adventures,
    onAdventureClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        adventures.forEach { adventure ->
            AdventureItem(
                adventure = adventure,
                onClick = { onAdventureClick(adventure.id) }
            )
        }
    }
}
