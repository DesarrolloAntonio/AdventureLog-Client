package com.desarrollodroide.adventurelog.feature.adventures.adventures

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData

@Composable
fun AdventureListScreen(
    adventureItems: List<Adventure>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(adventureItems) { item ->
            AdventureItem(item)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdventureListScreenPreview() {
    AdventureListScreen(
        adventureItems = PreviewData.adventures,
        modifier = Modifier.background(Color.White)
    )
}

