package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditChecklist

import com.desarrollodroide.adventurelog.feature.ui.components.date.DatePickerField
import com.desarrollodroide.adventurelog.feature.collections.ui.components.PublicSwitchRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditChecklistViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField
import org.koin.compose.viewmodel.koinViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.rememberDiscardGuard
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Checklist

/**
 * A checklist and its lines.
 *
 * Lines are added, ticked and removed here and saved together: the server takes the items inside
 * the checklist body, so there is no per-item call to make.
 */
@Composable
fun AddEditChecklistScreen(
    collectionId: String,
    existingChecklist: Checklist?,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditChecklistViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var listOpen by remember { mutableStateOf(true) }

    LaunchedEffect(existingChecklist?.id) { viewModel.load(collectionId, existingChecklist?.id) }

    val leave = rememberDiscardGuard(
        hasChanges = !state.isLoading && viewModel.hasChanges(state),
        thing = "checklist",
        isEditing = existingChecklist != null,
        onLeave = onCancel
    )
    LaunchedEffect(state.saved) { if (state.saved) onDone() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }


    if (state.isLoading || state.loadError != null) {
        // No form until the record is in hand: an empty one could be saved over it.
        Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            if (state.loadError == null) {
                CircularProgressIndicator()
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = state.loadError!!, style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = onCancel) { Text("Cancel") }
                        Button(onClick = { viewModel.retryLoad(collectionId) }) { Text("Try again") }
                    }
                }
            }
        }
        return
    }
    Box(modifier = modifier.fillMaxSize()) {
        ContentColumn {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (existingChecklist == null) "New checklist" else "Edit checklist",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.lines.isNotEmpty()) {
                        Text(
                            text = "${state.doneCount} / ${state.lines.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                SectionCard(
                    title = "The list",
                    icon = Icons.Default.Checklist,
                    expanded = listOpen,
                    onExpandedChange = { listOpen = it }
                ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StyledTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Title",
                    icon = Icons.Default.Title,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        state.lines.forEachIndexed { index, line ->
                            ChecklistLineRow(
                                name = line.name,
                                checked = line.checked,
                                onToggle = { viewModel.toggleLine(index) },
                                onRemove = { viewModel.removeLine(index) }
                            )
                        }

                        if (state.lines.isEmpty()) {
                            Text(
                                text = "Nothing on the list yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StyledTextField(
                        value = state.draft,
                        onValueChange = viewModel::onDraftChange,
                        label = "Add an item",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = viewModel::addLine,
                        enabled = state.draft.isNotBlank(),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Add")
                    }
                }

                }
                }

                DatePickerField(
                    label = "Date (optional)",
                    value = state.date,
                    onValueChange = viewModel::onDateChange,
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    PublicSwitchRow(
                        title = "Public checklist",
                        checked = state.isPublic,
                        onCheckedChange = viewModel::onPublicChange
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.save(collectionId) },
                        enabled = state.canSave,
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                    TextButton(onClick = leave) { Text("Cancel") }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * One line of the list. The checkbox carried no name of its own, so a screen reader said
 * "checkbox, not checked" without saying which item (QA RL-06): the whole row is now the toggle,
 * read with the item's name, and Remove says what it removes.
 */
@Composable
internal fun ChecklistLineRow(
    name: String,
    checked: Boolean,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            color = if (checked) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove $name",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

