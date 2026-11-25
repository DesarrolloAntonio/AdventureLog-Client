package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.locations.ui.components.categories.CookieIcon
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel.CategoriesState
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The Category chip's own sheet: every category a row with its icon on a cookie and how many
 * places it holds, any number of them on, each tap applied at once so the list behind follows.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PlacesCategorySheet(
    categoriesState: CategoriesState,
    selected: List<String>,
    onChange: (List<String>) -> Unit,
    onManage: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Categories", style = AdventureType.display(34.sp))
                    Text(
                        categorySummary(categoriesState, selected),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    )
                }
                if (selected.isNotEmpty()) {
                    TextButton(onClick = { onChange(emptyList()) }, shapes = ButtonDefaults.shapes()) { Text("Clear") }
                }
            }

            Box(Modifier.weight(1f, fill = false).fillMaxWidth()) {
                when (categoriesState) {
                    is CategoriesState.Loading -> Box(Modifier.fillMaxWidth().heightIn(min = 160.dp), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                    is CategoriesState.Error -> Column(
                        Modifier.fillMaxWidth().heightIn(min = 160.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    ) {
                        Text(
                            categoriesState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        OutlinedButton(onClick = onRetry, shapes = ButtonDefaults.shapes()) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Retry", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    is CategoriesState.Success -> {
                        val categories = categoriesState.categories
                        if (categories.isEmpty()) {
                            Text(
                                "No categories yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 24.dp),
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                                itemsIndexed(categories, key = { _, it -> it.id }) { index, category ->
                                    val on = category.name in selected
                                    CategoryToggleRow(
                                        category = category,
                                        on = on,
                                        index = index,
                                        count = categories.size,
                                        onToggle = { onChange(if (on) selected - category.name else selected + category.name) },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onManage,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Manage", modifier = Modifier.padding(start = 8.dp))
                }
                Button(
                    onClick = onDismiss,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text("Done") }
            }
        }
    }
}

@Composable
private fun CategoryToggleRow(category: Category, on: Boolean, index: Int, count: Int, onToggle: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val effects = MaterialTheme.motionScheme.fastEffectsSpec<Color>()
    val container by animateColorAsState(if (on) colors.secondaryContainer else colors.surfaceContainerHigh, effects, label = "row")
    val places = category.numAdventures.toIntOrNull() ?: 0
    Surface(shape = SegmentedListDefaults.shapeFor(index, count), color = container, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .toggleable(value = on, onValueChange = { onToggle() }, role = Role.Checkbox)
                .heightIn(min = 68.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // On a chosen (tonal) row the cookie turns to the surface, or it would vanish into it.
            CookieIcon(category.icon.ifBlank { "📁" }, size = 44, color = if (on) colors.surfaceContainerLowest else colors.secondaryContainer)
            Column(Modifier.weight(1f)) {
                Text(
                    category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (on) colors.onSecondaryContainer else colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (places == 1) "1 place" else "$places places",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            // A filled circle that pops a check in when on; an empty ring when off.
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (on) colors.primary else Color.Transparent,
                border = if (on) null else androidx.compose.foundation.BorderStroke(2.dp, colors.outline),
                modifier = Modifier.size(28.dp),
            ) {
                AnimatedVisibility(
                    visible = on,
                    enter = scaleIn(MaterialTheme.motionScheme.fastSpatialSpec(), initialScale = 0.3f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.3f) + fadeOut(),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/** "All categories", or "Hiking · 7 places", or "2 chosen · 9 places". */
internal fun categorySummary(state: CategoriesState, selected: List<String>): String {
    if (selected.isEmpty()) return "All categories"
    val chosen = (state as? CategoriesState.Success)?.categories?.filter { it.name in selected }.orEmpty()
    val places = chosen.sumOf { it.numAdventures.toIntOrNull() ?: 0 }
    val head = if (selected.size == 1) chosen.firstOrNull()?.displayName ?: "1 chosen" else "${selected.size} chosen"
    return if (chosen.isEmpty()) head else "$head · ${if (places == 1) "1 place" else "$places places"}"
}
