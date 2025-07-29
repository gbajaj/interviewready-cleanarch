package com.gauravbajaj.interviewready.base

/**
 * A sealed class that represents the result of an API operation.
 * This provides a type-safe way to handle success and various error scenarios.
 *
 * @param T The type of data returned on success
 */
sealed class ApiResult<out T> {
    /**
     * Represents a successful API response with data
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * Represents a network connectivity error (no internet, timeout, etc.)
     */
    data class NetworkError(
        val message: String = "Network connection error",
        val cause: Throwable? = null
    ) : ApiResult<Nothing>()

    /**
     * Represents an error parsing the API response (malformed JSON, etc.)
     */
    data class ParseError(
        val message: String = "Failed to parse response",
        val cause: Throwable? = null
    ) : ApiResult<Nothing>()

    /**
     * Represents an empty data response when data was expected
     */
    data class EmptyDataError(
        val message: String = "No data available"
    ) : ApiResult<Nothing>()

    /**
     * Represents an HTTP error response (4xx, 5xx status codes)
     */
    data class HttpError(
        val code: Int,
        val message: String = "HTTP error occurred",
        val cause: Throwable? = null
    ) : ApiResult<Nothing>()

    /**
     * Represents any other unexpected error
     */
    data class UnknownError(
        val message: String = "An unexpected error occurred",
        val cause: Throwable? = null
    ) : ApiResult<Nothing>()
}

/**
 * Extension functions for easier handling of ApiResult
 */

/**
 * Returns true if the result is a success
 */
fun <T> ApiResult<T>.isSuccess(): Boolean = this is ApiResult.Success

/**
 * Returns true if the result is any kind of error
 */
fun <T> ApiResult<T>.isError(): Boolean = this !is ApiResult.Success

/**
 * Returns the data if success, null otherwise
 */
fun <T> ApiResult<T>.getDataOrNull(): T? = when (this) {
    is ApiResult.Success -> data
    else -> null
}

/**
 * Returns the error message for any error type
 */
fun <T> ApiResult<T>.getErrorMessage(): String? = when (this) {
    is ApiResult.Success -> null
    is ApiResult.NetworkError -> message
    is ApiResult.ParseError -> message
    is ApiResult.EmptyDataError -> message
    is ApiResult.HttpError -> message
    is ApiResult.UnknownError -> message
}

/**
 * Maps the success data to another type while preserving error states
 */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.NetworkError -> this
    is ApiResult.ParseError -> this
    is ApiResult.EmptyDataError -> this
    is ApiResult.HttpError -> this
    is ApiResult.UnknownError -> this
}

/**
 * Executes a block if the result is successful
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) {
        action(data)
    }
    return this
}

/**
 * Executes a block if the result is an error
 */
inline fun <T> ApiResult<T>.onError(action: (String) -> Unit): ApiResult<T> {
    if (this.isError()) {
        getErrorMessage()?.let(action)
    }
    return this
}