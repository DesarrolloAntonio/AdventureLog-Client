package com.desarrollodroide.adventurelog.core.data.paging

import app.cash.paging.PagingSource
import app.cash.paging.PagingState
import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel

/**
 * PagingSource that uses the filtered endpoint for server-side filtering
 */
class AdventuresPagingSourceFiltered(
    private val networkDataSource: AdventureLogNetwork,
    private val pageSize: Int = 30,
    private val categoryNames: List<String>? = null,
    private val sortBy: String? = null,
    private val sortOrder: String? = null,
    private val isVisited: Boolean? = null,
    private val searchQuery: String? = null,
    private val includeCollections: Boolean = false,
    private val onLocationsLoaded: (List<Location>) -> Unit = {}
) : PagingSource<Int, Location>() {
    
    private val logger = Logger.withTag("AdventuresPagingSourceFiltered")
    
    override fun getRefreshKey(state: PagingState<Int, Location>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Location> {
        val currentPage = params.key ?: 1
        
        logger.d { 
            "Loading filtered page $currentPage with filters: " +
            "categories=$categoryNames, sortBy=$sortBy, sortOrder=$sortOrder, " +
            "isVisited=$isVisited, search=$searchQuery" 
        }
        
        return try {
            val adventures = networkDataSource.getAdventuresFiltered(
                page = currentPage,
                pageSize = pageSize,
                categoryIds = categoryNames, // API expects names in the types parameter
                sortBy = sortBy,
                sortOrder = sortOrder,
                isVisited = isVisited,
                searchQuery = searchQuery,
                includeCollections = includeCollections
            ).map { it.toDomainModel() }
            
            // Feed the cache with loaded locations
            onLocationsLoaded(adventures)
            
            logger.d { "Loaded ${adventures.size} filtered adventures for page $currentPage" }
            
            val nextKey = when {
                adventures.isEmpty() -> {
                    logger.d { "🏁 PagingSource - No adventures returned, last page" }
                    null
                }
                adventures.size < pageSize -> {
                    logger.d { "🏁 PagingSource - Last page reached (received ${adventures.size} < $pageSize)" }
                    null
                }
                else -> {
                    logger.d { "➡️ PagingSource - Full page received, nextKey = ${currentPage + 1}" }
                    currentPage + 1
                }
            }
            
            LoadResult.Page(
                data = adventures,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            logger.e(e) { "Error loading filtered adventures page $currentPage: ${e.message}" }
            LoadResult.Error(e)
        }
    }
}
