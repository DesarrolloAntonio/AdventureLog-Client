package com.desarrollodroide.adventurelog.feature.locations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.CategoryChips
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A category made from the place form is made for that place: once the server has it, it is the
 * one chosen, without a second tap on a chip that has just appeared.
 */
@OptIn(ExperimentalTestApi::class)
class CategoryChipsTest {

    private fun category(id: String, name: String) =
        Category(id = id, name = name.lowercase(), displayName = name, icon = "🌍", numAdventures = "0")

    @Test
    fun aCategoryCreatedHereIsChosenOnceItArrives() = runComposeUiTest {
        var categories by mutableStateOf(listOf(category("1", "General")))
        var chosen: Category? = null
        var created: String? = null
        setContent {
            CategoryChips(
                categories = categories,
                selectedCategory = chosen,
                onCategorySelected = { chosen = it },
                onAddCategory = { name, _ -> created = name },
            )
        }

        // "Category" is also the row's label; the dashed chip is the one that can be clicked.
        onNode(hasText("Category") and hasClickAction()).performClick()
        onNodeWithText("Category name").performTextInput("Ruins")
        waitForIdle()
        onNodeWithText("Create").assertIsEnabled().assertIsDisplayed().performClick()
        waitForIdle()
        assertEquals("Ruins", created)
        assertEquals(null, chosen)

        // The server answers and the list reloads with the new category in it.
        categories = categories + category("2", "Ruins")
        waitForIdle()

        assertEquals("2", chosen?.id)
    }
}
