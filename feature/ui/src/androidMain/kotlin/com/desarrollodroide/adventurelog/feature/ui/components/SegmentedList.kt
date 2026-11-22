package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object SegmentedListDefaults {
    private val Outer = 24.dp
    private val Inner = 6.dp

    val single: Shape = RoundedCornerShape(Outer)
    val first: Shape = RoundedCornerShape(topStart = Outer, topEnd = Outer, bottomStart = Inner, bottomEnd = Inner)
    val middle: Shape = RoundedCornerShape(Inner)
    val last: Shape = RoundedCornerShape(topStart = Inner, topEnd = Inner, bottomStart = Outer, bottomEnd = Outer)

    /** The gap between rows: the ground shows through, which is what separates them. */
    val Gap = 3.dp

    fun shapeFor(index: Int, count: Int): Shape = when {
        count == 1 -> single
        index == 0 -> first
        index == count - 1 -> last
        else -> middle
    }
}

/**
 * Rows grouped as one rounded block, split by a thin gap instead of a divider: the outer corners
 * round, the inner ones barely. For short, fixed lists (settings, a country's regions); a long or
 * paged list uses [SegmentedListDefaults.shapeFor] on its own lazy items.
 */
@Composable
fun <T> SegmentedList(
    items: List<T>,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    row: @Composable (T) -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
        items.forEachIndexed { index, item ->
            Surface(
                shape = SegmentedListDefaults.shapeFor(index, items.size),
                color = containerColor,
                modifier = Modifier.fillMaxWidth(),
            ) { row(item) }
        }
    }
}
