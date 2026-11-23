package com.desarrollodroide.adventurelog.feature.locations

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.locations.ui.components.categories.ManageCategoriesDialog
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel.CategoriesState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Deleting a category cannot be undone, so the trash only asks: nothing reaches the server until
 * the question is answered, and the question says how many places use it.
 */
@OptIn(ExperimentalTestApi::class)
class ManageCategoriesTest {

    private val categories = CategoriesState.Success(
        listOf(
            Category(id = "1", name = "nature", displayName = "Nature", icon = "🏔️", numAdventures = "7"),
            Category(id = "2", name = "qa", displayName = "QA", icon = "🌍", numAdventures = "0"),
        )
    )

    @Test
    fun theTrashAsksBeforeDeletingAndCancelKeepsIt() = runComposeUiTest {
        val deleted = mutableListOf<String>()
        setContent {
            ManageCategoriesDialog(
                categoriesState = categories,
                onDismiss = {},
                onAddCategory = { _, _ -> },
                onEditCategory = { _, _, _ -> },
                onDeleteCategory = { deleted += it },
                onRetryLoadCategories = {},
            )
        }

        onNodeWithContentDescription("Delete Nature").performClick()
        onNodeWithText("7 places use it. This can't be undone.").assertIsDisplayed()
        assertEquals(emptyList<String>(), deleted)

        onNodeWithText("Cancel").performClick()
        waitForIdle()
        assertEquals(emptyList<String>(), deleted)

        onNodeWithContentDescription("Delete QA").performClick()
        onNodeWithText("Delete").performClick()
        waitForIdle()
        assertEquals(listOf("2"), deleted)
    }
}
