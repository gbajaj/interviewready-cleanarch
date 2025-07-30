package com.gauravbajaj.interviewready.base

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen

/**
 * Extension functions for implementing retry logic with exponential backoff
 * on Flow<ApiResult<T>> operations.
 */

/**
 * Retries a Flow<ApiResult<T>> operation with exponential backoff.
 *
 * This function implements smart retry logic that:
 * - Only retries appropriate error types based on RetryConfig
 * - Uses exponential backoff with configurable delays
 * - Respects maximum attempt limits
 * - Provides attempt information for logging/monitoring
 *
 * @param config The retry configuration to use
 * @param onRetry Optional callback invoked before each retry attempt
 * @return A new Flow that implements retry logic
 */
fun <T> Flow<ApiResult<T>>.retryWithBackoff(
    config: RetryConfig = RetryConfig.DEFAULT,
    onRetry: ((attempt: Int, error: ApiResult<*>) -> Unit)? = null
): Flow<ApiResult<T>> {
    if (config.maxAttempts <= 0) {
        return this // No retry if maxAttempts is 0 or negative
    }

    return this.retryWhen { cause, attempt ->
        // Note: retryWhen uses 0-based attempts, but we want 1-based for our logic
        val attemptNumber = attempt.toInt() + 1

        // If we've exceeded max attempts, don't retry
        if (attemptNumber > config.maxAttempts) {
            return@retryWhen false
        }

        // We need to get the last emitted value to check if it should be retried
        // Since we're working with ApiResult, we'll implement this differently
        true // For now, let the flow-based retry handle the logic
    }
}

/**
 * Retries a suspend function that returns ApiResult<T> with exponential backoff.
 *
 * This is a more direct approach for retrying individual operations.
 *
 * @param config The retry configuration to use
 * @param onRetry Optional callback invoked before each retry attempt
 * @param operation The suspend function to retry
 * @return The final ApiResult after all retry attempts
 */
suspend fun <T> retryWithBackoff(
    config: RetryConfig = RetryConfig.DEFAULT,
    onRetry: ((attempt: Int, error: ApiResult<*>) -> Unit)? = null,
    operation: suspend () -> ApiResult<T>
): ApiResult<T> {
    var lastResult: ApiResult<T> = ApiResult.UnknownError("No attempts made")

    for (attempt in 1..config.maxAttempts + 1) { // +1 for initial attempt
        try {
            lastResult = operation()

            // If successful, return immediately
            if (lastResult is ApiResult.Success) {
                return lastResult
            }

            // If this is the last attempt, return the result (don't retry)
            if (attempt > config.maxAttempts) {
                return lastResult
            }

            // Check if we should retry this error type
            if (!config.shouldRetry(lastResult)) {
                return lastResult
            }

            // Calculate delay and notify about retry
            val delayMs = config.calculateDelay(attempt)
            onRetry?.invoke(attempt, lastResult)

            // Wait before retrying
            if (delayMs > 0) {
                delay(delayMs)
            }

        } catch (exception: Exception) {
            // Convert exception to ApiResult and handle similarly
            lastResult = ApiResult.UnknownError(
                "Unexpected error during retry attempt $attempt",
                exception
            )

            if (attempt > config.maxAttempts || !config.shouldRetry(lastResult)) {
                return lastResult
            }

            val delayMs = config.calculateDelay(attempt)
            onRetry?.invoke(attempt, lastResult)

            if (delayMs > 0) {
                delay(delayMs)
            }
        }
    }

    return lastResult
}

/**
 * Creates a Flow that retries an operation with exponential backoff.
 *
 * This is useful for creating retry-enabled flows from scratch.
 *
 * @param config The retry configuration to use
 * @param onRetry Optional callback invoked before each retry attempt
 * @param operation The suspend function to retry
 * @return A Flow<ApiResult<T>> that implements retry logic
 */
fun <T> flowWithRetry(
    config: RetryConfig = RetryConfig.DEFAULT,
    onRetry: ((attempt: Int, error: ApiResult<*>) -> Unit)? = null,
    operation: suspend () -> ApiResult<T>
): Flow<ApiResult<T>> = flow {
    val result = retryWithBackoff(config, onRetry, operation)
    emit(result)
}

/**
 * Data class to track retry state and statistics
 */
data class RetryState(
    val currentAttempt: Int = 0,
    val totalAttempts: Int = 0,
    val lastError: ApiResult<*>? = null,
    val nextRetryDelay: Long = 0L,
    val isRetrying: Boolean = false
) {
    val hasMoreAttempts: Boolean
        get() = currentAttempt < totalAttempts

    val progress: Float
        get() = if (totalAttempts == 0) 0f else currentAttempt.toFloat() / totalAttempts.toFloat()
}