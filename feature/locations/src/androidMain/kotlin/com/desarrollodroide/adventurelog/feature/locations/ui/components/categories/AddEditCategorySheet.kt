package com.desarrollodroide.adventurelog.feature.locations.ui.components.categories

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.ui.components.StandardTextField
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/** The icons a travel category usually wants, one tap each; the full picker is behind "More". */
private val SuggestedIcons = listOf("🌍", "🏔️", "🏖️", "🏛️", "🏰", "🌋", "🏕️", "🍜", "🍷", "🎢", "🛍️")

/**
 * Creating or renaming a category. The chosen icon sits in a cookie stamp that turns whenever it
 * changes, next to the chip the category will be on a place; the icons a trip usually wants are
 * one tap away and the full emoji picker opens only when asked for.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditCategorySheet(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String) -> Unit
) {
    var categoryName by remember { mutableStateOf(category?.displayName ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.icon ?: "🌍") }
    var allEmoji by remember { mutableStateOf(false) }
    val save = { if (categoryName.isNotBlank()) onSave(categoryName.trim(), selectedIcon) }

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss,
    ) {
        // The sheet is its own window: its focus and keyboard are these, not the form's behind it.
        val focus = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconStamp(selectedIcon)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (category == null) "New category" else "Edit category",
                        style = AdventureType.display(30.sp),
                    )
                    ChipPreview(icon = selectedIcon, name = categoryName)
                }
            }

            StandardTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                placeholder = "Category name",
                leadingIcon = Icons.Default.Category,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                // The keyboard's ✓ creates it: with the keyboard up, it is the one button in reach.
                keyboardActions = KeyboardActions(onDone = { save() }),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Icon",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // What is chosen is always on show, even when it came from the full picker.
                val icons = (if (selectedIcon in SuggestedIcons) SuggestedIcons else listOf(selectedIcon) + SuggestedIcons)
                    .take(SuggestedIcons.size)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    maxItemsInEachRow = TilesPerRow,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    icons.forEach { icon ->
                        IconTile(selected = icon == selectedIcon, onClick = { selectedIcon = icon }) {
                            Text(icon, fontSize = 22.sp)
                        }
                    }
                    IconTile(
                        selected = allEmoji,
                        onClick = {
                            allEmoji = !allEmoji
                            // The picker needs the room the keyboard takes.
                            if (allEmoji) { focus.clearFocus(); keyboard?.hide() }
                        },
                        label = if (allEmoji) "Hide all emoji" else "All emoji",
                        role = Role.Button,
                    ) {
                        Icon(Icons.Outlined.AddReaction, contentDescription = null)
                    }
                }
            }

            // The picker gives way first: with the keyboard up it shrinks so Cancel and Create stay
            // on screen instead of being pushed off the bottom of the sheet.
            AnimatedVisibility(
                visible = allEmoji,
                enter = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) + fadeIn(),
                exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeOut(),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                EmojiPicker(
                    modifier = Modifier.fillMaxWidth(),
                    onEmojiSelected = { selectedIcon = it },
                    onDismiss = { allEmoji = false },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text("Cancel") }
                Button(
                    onClick = save,
                    enabled = categoryName.isNotBlank(),
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f).height(56.dp),
                ) { Text(if (category == null) "Create" else "Save") }
            }
        }
    }
}

/** The icon on a cookie stamp that turns a ninth of a revolution each time it changes. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IconStamp(icon: String) {
    val turn = remember { Animatable(0f) }
    var first by remember { mutableStateOf(true) }
    val spin = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val pop = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    LaunchedEffect(icon) {
        if (first) first = false else turn.animateTo(turn.targetValue + 40f, spin)
    }
    Box(Modifier.size(84.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = turn.value }
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        AnimatedContent(
            targetState = icon,
            transitionSpec = {
                (scaleIn(pop, initialScale = 0.4f) + fadeIn()) togetherWith
                    (scaleOut(targetScale = 0.4f) + fadeOut())
            },
            label = "icon",
        ) { Text(it, fontSize = 38.sp) }
    }
}

/** The category as it will read on a place's chip, updating as it is typed. */
@Composable
private fun ChipPreview(icon: String, name: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = "$icon ${name.ifBlank { "Name" }}",
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

private const val TilesPerRow = 6

/** An icon choice sharing the row's width: a rounded square at rest, a circle once chosen. */
@Composable
private fun androidx.compose.foundation.layout.FlowRowScope.IconTile(
    selected: Boolean,
    onClick: () -> Unit,
    label: String? = null,
    role: Role = Role.RadioButton,
    content: @Composable () -> Unit,
) {
    val corner by animateFloatAsState(
        if (selected) 50f else 30f,
        MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "corner",
    )
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "container",
    )
    Box(
        Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(corner.toInt()))
            .background(container)
            .selectable(selected = selected, onClick = onClick, role = role)
            .then(if (label != null) Modifier.semantics { contentDescription = label } else Modifier),
        contentAlignment = Alignment.Center,
    ) { content() }
}
