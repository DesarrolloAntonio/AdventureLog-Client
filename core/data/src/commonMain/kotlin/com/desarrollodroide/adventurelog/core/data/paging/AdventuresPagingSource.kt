package com.desarrollodroide.adventurelog.core.data.paging

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("AdventuresPagingSource")

class AdventuresPagingSource(
    private val networkDataSource: AdventureLogNetwork,
    private val pageSize: Int = 30,
    private val onLocationsLoaded: (List<Location>) -> Unit = {}
) : PagingSource<Int, Location>() {
    
    private var totalItemsLoaded = 0
    
    override suspend fun load(
        params: PagingSourceLoadParams<Int>
    ): PagingSourceLoadResult<Int, Location> {
        val page = params.key ?: 1
        // Force our pageSize instead of using params.loadSize
        val size = pageSize
        
        logger.d { "🔍 PagingSource - Requesting page: $page, pageSize: $size (params.loadSize was ${params.loadSize})" }
        
        return try {
            val adventures = networkDataSource.getAdventures(
                page = page,
                pageSize = size
            ).map { it.toDomainModel() }
            
            // Feed the cache with loaded locations
            onLocationsLoaded(adventures)
            
            totalItemsLoaded += adventures.size
            
            logger.d { "✅ PagingSource - Received ${adventures.size} adventures for page $page (total loaded: $totalItemsLoaded)" }
            if (adventures.isNotEmpty()) {
                logger.d { "   First adventure: ${adventures.first().name}" }
                logger.d { "   Last adventure: ${adventures.last().name}" }
            }
            
            val nextKey = when {
                adventures.isEmpty() -> {
                    logger.d { "🏁 PagingSource - No adventures returned, last page" }
                    null
                }
                adventures.size < size -> {
                    logger.d { "🏁 PagingSource - Last page reached (received ${adventures.size} < $size)" }
                    null
                }
                else -> {
                    logger.d { "➡️ PagingSource - Full page received, nextKey = ${page + 1}" }
                    page + 1
                }
            }
            
            PagingSourceLoadResultPage(
                data = adventures,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            ) as PagingSourceLoadResult<Int, Location>
        } catch (e: Exception) {
            logger.e { "❌ PagingSource - Error loading page $page: ${e.message}" }
            e.printStackTrace()
            PagingSourceLoadResultError<Int, Location>(e) as PagingSourceLoadResult<Int, Location>
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, Location>): Int? {
        // Reset counter on refresh
        totalItemsLoaded = 0
        
        val key = state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
        logger.d { "🔄 PagingSource - getRefreshKey called, returning: $key" }
        return key
    }
}