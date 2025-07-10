package com.desarrollodroide.adventurelog.core.data.paging

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel

class CollectionsPagingSource(
    private val networkDataSource: AdventureLogNetworkDataSource,
    private val pageSize: Int = 30
) : PagingSource<Int, Collection>() {

    private var totalItemsLoaded = 0

    override suspend fun load(
        params: PagingSourceLoadParams<Int>
    ): PagingSourceLoadResult<Int, Collection> {
        val page = params.key ?: 1
        val size = pageSize

        println("🔍 CollectionsPagingSource - Requesting page: $page, pageSize: $size")

        return try {
            val collections = networkDataSource.getCollections(
                page = page,
                pageSize = size
            ).map { it.toDomainModel() }

            totalItemsLoaded += collections.size

            println("✅ CollectionsPagingSource - Received ${collections.size} collections for page $page (total loaded: $totalItemsLoaded)")
            if (collections.isNotEmpty()) {
                println("   First collection: ${collections.first().name}")
                println("   Last collection: ${collections.last().name}")
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
                data = collections,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            ) as PagingSourceLoadResult<Int, Collection>
        } catch (e: Exception) {
            println("❌ CollectionsPagingSource - Error loading page $page: ${e.message}")
            e.printStackTrace()
            PagingSourceLoadResultError<Int, Collection>(e) as PagingSourceLoadResult<Int, Collection>
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Collection>): Int? {
        totalItemsLoaded = 0

        val key = state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
        println("🔄 CollectionsPagingSource - getRefreshKey called, returning: $key")
        return key
    }
}
