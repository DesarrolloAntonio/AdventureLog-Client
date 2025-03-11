package com.desarrollodroide.adventurelog.feature.home

import androidx.compose.runtime.Composable
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.home.ui.components.adventures.AdventureCard
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun AdventureCardPreview() {
    AdventureCard(adventure = PreviewData.adventures.first(), onClick = { })
}