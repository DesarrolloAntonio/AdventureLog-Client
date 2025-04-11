package com.desarrollodroide.adventurelog.feature.adventures.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(adventures) { adventure ->
            AdventureItem(
                adventure = adventure,
                onClick = { onAdventureClick(adventure.id) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
