package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.SearchRepository
import com.desarrollodroide.adventurelog.core.model.SearchHit
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.io.IOException

class SearchRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : SearchRepository {

    override suspend fun search(query: String, limit: Int): Either<ApiResponse, List<SearchHit>> {
        return try {
            val hits = networkDataSource.globalSearch(query, limit)
                .results
                .map { it.toDomainModel() }
            Either.Right(hits)
        } catch (e: HttpException) {
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            Either.Left(ApiResponse.HttpError)
        }
    }
}
