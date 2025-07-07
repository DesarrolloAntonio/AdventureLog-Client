package com.desarrollodroide.adventurelog.feature.collections.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.components.BasicInfoSection
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.components.DateSection
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.data.CollectionFormData
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton

@Composable
fun AddEditCollectionScreen(
    onNavigateBack: () -> Unit,
    onSave: (CollectionFormData) -> Unit,
    modifier: Modifier = Modifier,
    initialData: CollectionFormData? = null
) {
    var formData by remember {
        mutableStateOf(initialData ?: CollectionFormData())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        BasicInfoSection(
            formData = formData,
            onFormDataChange = { formData = it },
            onNavigateBack = onNavigateBack
        )

        DateSection(
            formData = formData,
            onFormDataChange = { formData = it }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(
                onClick = { onSave(formData) },
                text = if (initialData != null) "Update Collection" else "Create Collection",
                enabled = formData.name.isNotBlank()
            )

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}