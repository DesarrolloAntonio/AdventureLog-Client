package com.desarrollodroide.adventurelog.core.data.paging

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel

class CollectionsPagingSource(
    private val networkDataSource: AdventureLogNetwork,
    private val pageSize: Int = 30,
    private val sortField: String? = null,
    private val sortDirection: String? = null
) : PagingSource<Int, UltraSlimCollection>() {

    private val logger = Logger.withTag("CollectionsPagingSource")
    
    private var totalItemsLoaded = 0
    private val allLoadedCollections = mutableListOf<UltraSlimCollection>()

    override suspend fun load(
        params: PagingSourceLoadParams<Int>
    ): PagingSourceLoadResult<Int, UltraSlimCollection> {
        val page = params.key ?: 1
        val size = pageSize

        logger.d { "🔍 CollectionsPagingSource - Requesting page: $page, pageSize: $size" }
        logger.d { "   Sort options: field=$sortField, direction=$sortDirection" }

        return try {
            val collections = networkDataSource.getCollections(
                page = page,
                pageSize = size
            ).map { it.toDomainModel() }

            // Add new collections to our accumulated list
            allLoadedCollections.addAll(collections)
            totalItemsLoaded = allLoadedCollections.size

            // Sort all loaded collections based on current sort options
            val sortedCollections = sortCollections(allLoadedCollections, sortField, sortDirection)

            logger.d { "✅ CollectionsPagingSource - Received ${collections.size} new collections for page $page" }
            logger.d { "   Total loaded and sorted: ${sortedCollections.size}" }
            if (sortedCollections.isNotEmpty()) {
                logger.d { "   First collection after sort: ${sortedCollections.first().name}" }
                logger.d { "   Last collection after sort: ${sortedCollections.last().name}" }
            }

            val nextKey = when {
                collections.isEmpty() -> {
                    logger.d { "🏁 CollectionsPagingSource - No collections returned, last page" }
                    null
                }

                collections.size < size -> {
                    logger.d { "🏁 CollectionsPagingSource - Last page reached (received ${collections.size} < $size)" }
                    null
                }

                else -> {
                    logger.d { "➡️ CollectionsPagingSource - Full page received, nextKey = ${page + 1}" }
                    page + 1
                }
            }

            PagingSourceLoadResultPage(
                data = sortedCollections,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            ) as PagingSourceLoadResult<Int, UltraSlimCollection>
        } catch (e: Exception) {
            logger.e(e) { "❌ CollectionsPagingSource - Error loading page $page: ${e.message}" }
            PagingSourceLoadResultError<Int, UltraSlimCollection>(e) as PagingSourceLoadResult<Int, UltraSlimCollection>
        }
    }

    private fun sortCollections(
        collections: List<UltraSlimCollection>,
        sortField: String?,
        sortDirection: String?
    ): List<UltraSlimCollection> {
        val sorted = when (sortField) {
            "NAME" -> {
                collections.sortedBy { it.name.lowercase() }
            }
            "START_DATE" -> {
                collections.sortedBy { collection ->
                    // Simple string comparison for dates in ISO format (YYYY-MM-DD)
                    // Null dates go to the end
                    collection.startDate ?: "9999-12-31"
                }
            }
            "UPDATED_AT" -> {
                collections.sortedBy { it.updatedAt }
            }
            else -> {
                // Default: sort by updated date descending
                collections.sortedByDescending { it.updatedAt }
            }
        }

        return if (sortDirection == "ASCENDING") {
            sorted
        } else {
            sorted.reversed()
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UltraSlimCollection>): Int? {
        totalItemsLoaded = 0
        allLoadedCollections.clear()

        val key = state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
        logger.d { "🔄 CollectionsPagingSource - getRefreshKey called, returning: $key" }
        return key
    }
}
