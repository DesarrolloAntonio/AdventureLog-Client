package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditNote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditNoteViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.Notes
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Writing a note into a collection.
 *
 * The Notes tab could read notes and never make one, which is a tab that can only ever say it is
 * empty.
 */
@Composable
fun AddEditNoteScreen(
    collectionId: String,
    existingNote: Note?,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditNoteViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var detailsOpen by remember { mutableStateOf(true) }

    LaunchedEffect(existingNote?.id) { viewModel.prefill(existingNote) }
    LaunchedEffect(state.saved) { if (state.saved) onDone() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
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
                Text(
                    text = if (existingNote == null) "New note" else "Edit note",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                SectionCard(
                    title = "Note",
                    icon = Icons.AutoMirrored.Filled.Notes,
                    expanded = detailsOpen,
                    onExpandedChange = { detailsOpen = it }
                ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StyledTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Title",
                    icon = Icons.Default.Title,
                    modifier = Modifier.fillMaxWidth()
                )

                StyledTextField(
                    value = state.content,
                    onValueChange = viewModel::onContentChange,
                    label = "Note",
                    icon = Icons.AutoMirrored.Filled.Notes,
                    singleLine = false,
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                StyledTextField(
                    value = state.date,
                    onValueChange = viewModel::onDateChange,
                    label = "Date (YYYY-MM-DD, optional)",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.fillMaxWidth()
                )

                }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Public note", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Visible to anyone the collection is shared with",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.isPublic,
                            onCheckedChange = viewModel::onPublicChange
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

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
                    TextButton(onClick = onCancel) { Text("Cancel") }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
