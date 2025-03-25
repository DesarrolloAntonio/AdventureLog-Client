package com.desarrollodroide.adventurelog.feature.adventures.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem

@Composable
fun AdventureListScreen(
    adventureItems: List<Adventure>,
    onOpenDetails: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(adventureItems) { item ->
            AdventureItem(
                adventure = item,
                onClick = { onOpenDetails(item.id) }
            )
        }
    }
}
