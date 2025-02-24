package com.desarrollodroide.adventurelog.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
/**
 * A sealed interface representing the result of an operation.
 * @param T The type of data contained in a successful result.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable? = null) : Result<Nothing>
}

/**
 * A sealed class representing a value that can be either a success or a failure.
 * [Either.Left] represents a failure case.
 * [Either.Right] represents a success case.
 * @param A The type of the failure.
 * @param B The type of the success.
 */
sealed class Either<out A, out B> {
    data class Left<out A>(val value: A) : Either<A, Nothing>()
    data class Right<out B>(val value: B) : Either<Nothing, B>()
}

/**
 * A sealed interface representing different types of API responses.
 * - [ApiResponse.HttpError] indicates an HTTP error.
 * - [ApiResponse.IOException] indicates an input/output exception.
 */
sealed interface ApiResponse {
    data object HttpError : ApiResponse
    data object IOException : ApiResponse
    data object InvalidCredentials : ApiResponse
    data object InvalidCsrfToken : ApiResponse
}

/**
 * Extension function to transform a [Flow] of type [T] into a [Flow] of [Result<T>].
 * It wraps successful emissions in [Result.Success] and catches errors,
 * wrapping them in [Result.Error].
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .catch { emit(Result.Error(it)) }
}