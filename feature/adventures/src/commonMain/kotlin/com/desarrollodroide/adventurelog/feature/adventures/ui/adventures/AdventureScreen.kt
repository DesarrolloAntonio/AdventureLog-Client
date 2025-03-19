package com.desarrollodroide.adventurelog.feature.adventures.ui.adventures

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure

@Composable
fun AdventureListScreen(
    adventureItems: List<Adventure>,
    onOpenDetails: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(adventureItems) { item ->
            AdventureItem(
                adventure = item,
                onClick = { onOpenDetails(item.id) }
            )
        }
    }
}
