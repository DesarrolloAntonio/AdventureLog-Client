package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.io.IOException

class CategoriesRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : CategoriesRepository {

    override suspend fun getCategories(): Either<ApiResponse, List<Category>> {
        return try {
            val categories = networkDataSource.getCategories().map { it.toDomainModel() }
            Either.Right(categories)
        } catch (e: HttpException) {
            println("HTTP Error during getCategories: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getCategories: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getCategories: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun getCategoryById(categoryId: String): Either<ApiResponse, Category> {
        return try {
            val category = networkDataSource.getCategoryById(categoryId).toDomainModel()
            Either.Right(category)
        } catch (e: HttpException) {
            println("HTTP Error during getCategoryById: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                404 -> Either.Left(ApiResponse.HttpError)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getCategoryById: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getCategoryById: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun createCategory(
        name: String,
        displayName: String,
        icon: String?
    ): Either<ApiResponse, Category> {
        return try {
            val category = networkDataSource.createCategory(
                name = name,
                displayName = displayName,
                icon = icon
            ).toDomainModel()
            Either.Right(category)
        } catch (e: HttpException) {
            println("HTTP Error during createCategory: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                400 -> Either.Left(ApiResponse.HttpError)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during createCategory: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during createCategory: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        displayName: String,
        icon: String?
    ): Either<ApiResponse, Category> {
        return try {
            val category = networkDataSource.updateCategory(
                categoryId = categoryId,
                name = name,
                displayName = displayName,
                icon = icon
            ).toDomainModel()
            Either.Right(category)
        } catch (e: HttpException) {
            println("HTTP Error during updateCategory: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                404 -> Either.Left(ApiResponse.HttpError)
                400 -> Either.Left(ApiResponse.HttpError)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during updateCategory: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during updateCategory: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun deleteCategory(categoryId: String): Either<ApiResponse, Unit> {
        return try {
            networkDataSource.deleteCategory(categoryId)
            Either.Right(Unit)
        } catch (e: HttpException) {
            println("HTTP Error during deleteCategory: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                404 -> Either.Left(ApiResponse.HttpError)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during deleteCategory: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during deleteCategory: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
}
