package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditNote

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
import com.desarrollodroide.adventurelog.feature.ui.components.rememberDiscardGuard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.Notes
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
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

    LaunchedEffect(existingNote?.id) { viewModel.load(collectionId, existingNote?.id) }
    val leave = rememberDiscardGuard(
        hasChanges = !state.isLoading && viewModel.hasChanges(state),
        thing = "note",
        isEditing = existingNote != null,
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
                Text(
                    text = if (existingNote == null) "New note" else "Edit note",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                SectionCard(
                    // The rows below it already sit inside the form's 16dp; the card added its own and
                    // came out narrower than everything else on the page (QA CO-19).
                    inset = 0.dp,
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

                DatePickerField(
                    label = "Date (optional)",
                    value = state.date,
                    onValueChange = viewModel::onDateChange,
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
                    PublicSwitchRow(
                        title = "Public note",
                        checked = state.isPublic,
                        onCheckedChange = viewModel::onPublicChange
                    )
                }

                Spacer(Modifier.height(4.dp))

                // The same pair as the collection and transport forms: Save across the form,
                // Cancel under it. These three had a small Save and Cancel side by side on the
                // left (QA CO-19).
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PrimaryButton(
                        onClick = { viewModel.save(collectionId) },
                        text = if (state.isSaving) "Saving…" else "Save",
                        enabled = state.canSave
                    )
                    TextButton(onClick = leave, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
