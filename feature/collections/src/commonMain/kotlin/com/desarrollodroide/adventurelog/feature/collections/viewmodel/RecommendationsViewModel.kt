package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetRecommendationsUseCase
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Which place in the collection to look around, or a name typed in instead.
 *
 * Both at once is allowed and the coordinates win, because a place already in the collection has
 * exact ones and a typed name only asks the server to guess.
 */
data class RecommendationsUiState(
    val anchorLocationId: String? = null,
    val query: String = "",
    val category: RecommendationCategory = RecommendationCategory.TOURISM,
    val radiusMetres: Int = 5000,
    val results: List<Recommendation> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
    /** The ids of results already turned into places, so a second tap does not add a duplicate. */
    val added: Set<String> = emptySet(),
    val addingId: String? = null
)

class RecommendationsViewModel(
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val createLocationUseCase: CreateLocationUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecommendationsUiState())
    val uiState: StateFlow<RecommendationsUiState> = _uiState.asStateFlow()

    fun onAnchorSelected(locationId: String?) {
        _uiState.update { it.copy(anchorLocationId = locationId) }
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onCategorySelected(category: RecommendationCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun onRadiusSelected(radiusMetres: Int) {
        _uiState.update { it.copy(radiusMetres = radiusMetres) }
    }

    /**
     * [anchor] is the chosen place's coordinates, resolved by the screen - the view model does not
     * hold the collection, and passing the two numbers is smaller than passing the whole of it.
     */
    fun search(anchor: Pair<Double, Double>?) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null) }
            val result = getRecommendationsUseCase(
                latitude = anchor?.first,
                longitude = anchor?.second,
                place = state.query.takeIf { it.isNotBlank() },
                category = state.category,
                radiusMetres = state.radiusMetres
            )
            _uiState.update { current ->
                when (result) {
                    is Either.Right -> current.copy(
                        isSearching = false,
                        hasSearched = true,
                        results = result.value,
                        errorMessage = null
                    )
                    is Either.Left -> current.copy(
                        isSearching = false,
                        hasSearched = true,
                        results = emptyList(),
                        errorMessage = result.value
                    )
                }
            }
        }
    }

    /**
     * Turns a suggestion into a real place in this collection.
     *
     * The category is the account's own list rather than the source's word for it: the server
     * takes a category it knows, and "restaurant" from OpenStreetMap is not one. Falling back to
     * whatever the account has first is better than inventing a category the user did not ask for.
     */
    fun addAsPlace(
        recommendation: Recommendation,
        collectionId: String,
        onAdded: (Location) -> Unit
    ) {
        if (recommendation.id in _uiState.value.added) return
        viewModelScope.launch {
            _uiState.update { it.copy(addingId = recommendation.id, errorMessage = null) }
            val category = categoryFor(recommendation)
            val result = createLocationUseCase(
                name = recommendation.name,
                description = recommendation.description.orEmpty(),
                category = category,
                rating = 0.0,
                link = recommendation.website?.let { asUrl(it) }.orEmpty(),
                location = recommendation.address.orEmpty(),
                latitude = recommendation.latitude.toString(),
                longitude = recommendation.longitude.toString(),
                isPublic = false,
                tags = emptyList(),
                collectionIds = listOf(collectionId)
            )
            when (result) {
                is Either.Right -> {
                    _uiState.update {
                        it.copy(addingId = null, added = it.added + recommendation.id)
                    }
                    onAdded(result.value)
                }
                is Either.Left -> _uiState.update {
                    it.copy(addingId = null, errorMessage = result.value)
                }
            }
        }
    }

    /**
     * The account's category whose name matches the source's word for the thing, or the first one
     * the account has. Never a new category: creating one as a side effect of adding a place would
     * fill the account's category list with OpenStreetMap's vocabulary.
     */
    private suspend fun categoryFor(recommendation: Recommendation): Category {
        val categories = when (val result = getCategoriesUseCase()) {
            is Either.Right -> result.value
            is Either.Left -> emptyList()
        }
        val wanted = (listOfNotNull(recommendation.primaryType) + recommendation.types)
            .map { it.lowercase() }
        return categories.firstOrNull { category ->
            category.name.lowercase() in wanted || category.displayName.lowercase() in wanted
        } ?: categories.firstOrNull() ?: Category(
            id = "",
            name = "general",
            displayName = "General",
            icon = "🌍",
            numAdventures = "0"
        )
    }
}

/** OpenStreetMap stores a bare host as often as a full URL, and a bare host is not a link. */
private fun asUrl(website: String): String =
    if (website.startsWith("http://") || website.startsWith("https://")) website
    else "https://$website"
