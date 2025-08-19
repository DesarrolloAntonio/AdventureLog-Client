package com.desarrollodroide.adventurelog.feature.collections.model

import com.desarrollodroide.adventurelog.core.model.SortDirection

data class CollectionSortOptions(
    val sortField: CollectionSortField = CollectionSortField.UPDATED_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING
)

enum class CollectionSortField {
    NAME,
    START_DATE,
    UPDATED_AT
}
