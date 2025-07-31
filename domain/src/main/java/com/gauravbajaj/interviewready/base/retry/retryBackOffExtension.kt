package com.gauravbajaj.interviewready.base.retry

import com.gauravbajaj.interviewready.base.ApiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Extension functions for implementing retry logic with exponential backoff
 * on Flow<ApiResult<T>> operations.
 */

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
    for (attempt in 1..config.maxAttempts + 1) {
        lastResult = try {
            operation()
        } catch (exception: Exception) {
            ApiResult.UnknownError("Unexpected error during retry attempt $attempt", exception)
        }
        // If success or shouldn't retry, return immediately
        if (lastResult is ApiResult.Success
            || attempt > config.maxAttempts
            || !config.shouldRetry(lastResult)) {
            return lastResult
        }
        // Notify retry and delay if configured
        val delayMs = config.calculateDelay(attempt)
        onRetry?.invoke(attempt, lastResult)
        if (delayMs > 0) delay(delayMs)
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