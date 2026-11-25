package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * A date as a torn-off calendar sheet: the month small and tracked over the day, condensed.
 * [highlighted] inks it in the primary colour (the next thing coming up); otherwise tertiary.
 */
@Composable
fun DateTile(
    month: String,
    day: Int,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val container = if (highlighted) colors.primary else colors.tertiaryContainer
    val ink = if (highlighted) colors.onPrimary else colors.onTertiaryContainer
    Column(
        modifier = modifier
            .size(width = 48.dp, height = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .clearAndSetSemantics { contentDescription = "$day $month" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // An explicit line height: left to the theme's body style it is 24sp, which pushed the day out.
        Text(month, color = ink, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, maxLines = 1)
        Text(day.toString(), color = ink, style = AdventureType.display(26.sp), maxLines = 1)
    }
}
