package com.desarrollodroide.adventurelog.core.data.paging

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel

class CollectionsPagingSource(
    private val networkDataSource: AdventureLogNetwork,
    private val pageSize: Int = 30,
    private val sortField: String? = null,
    private val sortDirection: String? = null
) : PagingSource<Int, Collection>() {

    private var totalItemsLoaded = 0
    private val allLoadedCollections = mutableListOf<Collection>()

    override suspend fun load(
        params: PagingSourceLoadParams<Int>
    ): PagingSourceLoadResult<Int, Collection> {
        val page = params.key ?: 1
        val size = pageSize

        println("🔍 CollectionsPagingSource - Requesting page: $page, pageSize: $size")
        println("   Sort options: field=$sortField, direction=$sortDirection")

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

            println("✅ CollectionsPagingSource - Received ${collections.size} new collections for page $page")
            println("   Total loaded and sorted: ${sortedCollections.size}")
            if (sortedCollections.isNotEmpty()) {
                println("   First collection after sort: ${sortedCollections.first().name}")
                println("   Last collection after sort: ${sortedCollections.last().name}")
            }

            val nextKey = when {
                collections.isEmpty() -> {
                    println("🏁 CollectionsPagingSource - No collections returned, last page")
                    null
                }

                collections.size < size -> {
                    println("🏁 CollectionsPagingSource - Last page reached (received ${collections.size} < $size)")
                    null
                }

                else -> {
                    println("➡️ CollectionsPagingSource - Full page received, nextKey = ${page + 1}")
                    page + 1
                }
            }

            PagingSourceLoadResultPage(
                data = sortedCollections,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            ) as PagingSourceLoadResult<Int, Collection>
        } catch (e: Exception) {
            println("❌ CollectionsPagingSource - Error loading page $page: ${e.message}")
            e.printStackTrace()
            PagingSourceLoadResultError<Int, Collection>(e) as PagingSourceLoadResult<Int, Collection>
        }
    }

    private fun sortCollections(
        collections: List<Collection>,
        sortField: String?,
        sortDirection: String?
    ): List<Collection> {
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

    override fun getRefreshKey(state: PagingState<Int, Collection>): Int? {
        totalItemsLoaded = 0
        allLoadedCollections.clear()

        val key = state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
        println("🔄 CollectionsPagingSource - getRefreshKey called, returning: $key")
        return key
    }
}
