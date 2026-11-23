package com.desarrollodroide.adventurelog.feature.locations.ui.components.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel.CategoriesState
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The categories, one segmented block: each row its icon on a cookie, its name and how many places
 * use it. A tap on the row edits it; deleting asks first and says what it will touch.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ManageCategoriesDialog(
    categoriesState: CategoriesState,
    onDismiss: () -> Unit,
    onAddCategory: (name: String, icon: String) -> Unit,
    onEditCategory: (categoryId: String, name: String, icon: String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onRetryLoadCategories: () -> Unit
) {
    var showAddCategorySheet by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Categories", style = AdventureType.display(34.sp))
                (categoriesState as? CategoriesState.Success)?.categories?.let { categories ->
                    val places = categories.sumOf { it.numAdventures.toIntOrNull() ?: 0 }
                    Text(
                        text = "${plural(categories.size, "category", "categories")} · ${plural(places, "place", "places")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Box(Modifier.weight(1f, fill = false).fillMaxWidth()) {
                when (categoriesState) {
                    is CategoriesState.Loading -> CenteredBox { LoadingIndicator() }
                    is CategoriesState.Error -> CenteredBox {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                categoriesState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                            OutlinedButton(onClick = onRetryLoadCategories, shapes = ButtonDefaults.shapes()) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Retry", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                    is CategoriesState.Success -> if (categoriesState.categories.isEmpty()) {
                        CenteredBox {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CookieIcon("🗂️", size = 72)
                                Text("No categories yet", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    } else {
                        val categories = categoriesState.categories
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                            itemsIndexed(categories, key = { _, it -> it.id }) { index, category ->
                                CategoryRow(
                                    category = category,
                                    index = index,
                                    count = categories.size,
                                    onEdit = { categoryToEdit = category },
                                    onDelete = { categoryToDelete = category },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text("Close") }
                Button(
                    onClick = { showAddCategorySheet = true },
                    enabled = categoriesState is CategoriesState.Success,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("New category", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }

    if (showAddCategorySheet) {
        AddEditCategorySheet(
            category = null,
            onDismiss = { showAddCategorySheet = false },
            onSave = { name, icon ->
                onAddCategory(name, icon)
                showAddCategorySheet = false
            }
        )
    }

    categoryToEdit?.let { category ->
        AddEditCategorySheet(
            category = category,
            onDismiss = { categoryToEdit = null },
            onSave = { name, icon ->
                onEditCategory(category.id, name, icon)
                categoryToEdit = null
            }
        )
    }

    categoryToDelete?.let { category ->
        val places = category.numAdventures.toIntOrNull() ?: 0
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            icon = { CookieIcon(category.icon.ifEmpty { "📁" }, size = 56) },
            title = { Text("Delete “${category.displayName}”?", textAlign = TextAlign.Center) },
            text = {
                Text(
                    text = if (places > 0) {
                        "${plural(places, "place uses", "places use")} it. This can't be undone."
                    } else {
                        "No place uses it. This can't be undone."
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(category.id)
                        categoryToDelete = null
                    },
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }, shapes = ButtonDefaults.shapes()) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    index: Int,
    count: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val places = category.numAdventures.toIntOrNull() ?: 0
    Surface(
        shape = SegmentedListDefaults.shapeFor(index, count),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .clickable(onClickLabel = "Edit ${category.displayName}", onClick = onEdit)
                .heightIn(min = 72.dp)
                .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CookieIcon(category.icon.ifEmpty { "📁" }, size = 48)
            Column(Modifier.weight(1f)) {
                Text(
                    category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (places == 0) "Not used yet" else plural(places, "place", "places"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Outlined.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete ${category.displayName}",
                    // Quiet on every row; the red belongs to the question that follows.
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** An emoji on a four-lobed cookie in the tonal colour, the categories' badge. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CookieIcon(icon: String, size: Int, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer) {
    Box(
        Modifier
            .size(size.dp)
            .clip(MaterialShapes.Cookie4Sided.toShape())
            .background(color),
        contentAlignment = Alignment.Center,
    ) { Text(icon, fontSize = (size * 0.45f).sp) }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().heightIn(min = 200.dp), contentAlignment = Alignment.Center) { content() }
}

private fun plural(n: Int, one: String, many: String) = if (n == 1) "1 $one" else "$n $many"
