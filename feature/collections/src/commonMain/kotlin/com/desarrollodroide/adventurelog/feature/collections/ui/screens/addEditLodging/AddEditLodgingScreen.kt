package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditLodging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import com.desarrollodroide.adventurelog.core.model.Lodging
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditLodgingViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.LodgingTypes
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn
import com.desarrollodroide.adventurelog.feature.ui.components.StyledTextField
import org.koin.compose.viewmodel.koinViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Hotel

/** Somewhere to sleep: the dates, the booking, and where it is. */
@Composable
fun AddEditLodgingScreen(
    collectionId: String,
    existingLodging: Lodging?,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditLodgingViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var stayOpen by remember { mutableStateOf(true) }

    LaunchedEffect(existingLodging?.id) { viewModel.load(collectionId, existingLodging?.id) }
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
                    text = if (existingLodging == null) "New lodging" else "Edit lodging",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                SectionCard(
                    title = "The stay",
                    icon = Icons.Default.Hotel,
                    expanded = stayOpen,
                    onExpandedChange = { stayOpen = it }
                ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StyledTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Name",
                    icon = Icons.Default.Title,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Type", style = MaterialTheme.typography.labelLarge)

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LodgingTypes.forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { viewModel.onTypeChange(type) },
                            label = { Text(type.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyledTextField(
                        value = state.checkIn,
                        onValueChange = viewModel::onCheckInChange,
                        label = "Check in (YYYY-MM-DD)",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                    StyledTextField(
                        value = state.checkOut,
                        onValueChange = viewModel::onCheckOutChange,
                        label = "Check out",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                }

                StyledTextField(
                    value = state.location,
                    onValueChange = viewModel::onLocationChange,
                    label = "Where it is",
                    icon = Icons.Default.Place,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyledTextField(
                        value = state.reservationNumber,
                        onValueChange = viewModel::onReservationChange,
                        label = "Reservation",
                        icon = Icons.Default.ConfirmationNumber,
                        modifier = Modifier.weight(1f)
                    )
                    StyledTextField(
                        value = state.price,
                        onValueChange = viewModel::onPriceChange,
                        label = "Price",
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )
                }

                StyledTextField(
                    value = state.link,
                    onValueChange = viewModel::onLinkChange,
                    label = "Booking link",
                    icon = Icons.Default.Link,
                    modifier = Modifier.fillMaxWidth()
                )

                }
                }

                StyledTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = "Notes",
                    icon = Icons.Default.Notes,
                    singleLine = false,
                    minLines = 4,
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Public", style = MaterialTheme.typography.bodyLarge)
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
