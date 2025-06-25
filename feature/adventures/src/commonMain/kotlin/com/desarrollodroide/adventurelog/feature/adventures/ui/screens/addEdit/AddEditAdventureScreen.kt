package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit

import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.BasicInfoSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.DateSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.LocationSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.TagsSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.AdventureFormData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AddEditAdventureScreen(
    categories: List<Category>,
    onNavigateBack: () -> Unit,
    onSave: (adventureData: AdventureFormData) -> Unit,
    modifier: Modifier = Modifier,
    initialData: AdventureFormData? = null
) {
    var formData by remember {
        mutableStateOf(
            initialData ?: AdventureFormData(
                category = categories.firstOrNull()
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

            BasicInfoSection(
                formData = formData,
                categories = categories,
                onFormDataChange = { formData = it },
                onNavigateBack = onNavigateBack
            )

            LocationSection(
                formData = formData,
                onFormDataChange = { formData = it }
            )

            TagsSection(
                formData = formData,
                onFormDataChange = { formData = it }
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
                Button(
                    onClick = { onSave(formData) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (initialData != null) "Update Adventure" else "Create Adventure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

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

@Preview
@Composable
private fun AddEditAdventureScreenPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
                categories = listOf(
                    Category("1", "general", "General", "🌍", "0"),
                    Category("2", "hotel", "Hotel", "🏨", "0"),
                    Category("3", "museum", "Museum", "🏛️", "0")
                ),
                onNavigateBack = {},
                onSave = {}
            )
        }
    }
}

@Preview
@Composable
private fun AddEditAdventureScreenWithDataPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
                categories = listOf(
                    Category("1", "general", "General", "🌍", "0"),
                    Category("2", "hotel", "Hotel", "🏨", "0"),
                    Category("3", "museum", "Museum", "🏛️", "0")
                ),
                onNavigateBack = {},
                onSave = {},
                initialData = AdventureFormData(
                    name = "Visit to Prado Museum",
                    description = "An incredible experience visiting one of the most important art galleries in the world.",
                    category = Category("3", "museum", "Museum", "🏛️", "0"),
                    rating = 5,
                    link = "https://www.museodelprado.es",
                    location = "Madrid, Spain",
                    latitude = "40.4138",
                    longitude = "-3.6921",
                    isPublic = true,
                    tags = listOf("art", "culture", "madrid"),
                    date = "2024-01-15"
                )
            )
        }
    }
}

@Preview
@Composable
private fun AddEditAdventureScreenDarkPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
                categories = listOf(
                    Category("1", "general", "General", "🌍", "0"),
                    Category("2", "hotel", "Hotel", "🏨", "0"),
                    Category("3", "museum", "Museum", "🏛️", "0")
                ),
                onNavigateBack = {},
                onSave = {}
            )
        }
    }
}
