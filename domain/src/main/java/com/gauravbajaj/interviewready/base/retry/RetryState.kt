package com.gauravbajaj.interviewready.base.retry

import com.gauravbajaj.interviewready.base.ApiResult
import kotlinx.coroutines.delay

/**
 * Simple retry state for communicating retry progress
 */
data class RetryState(
    val currentAttempt: Int = 0,
    val totalAttempts: Int = 0,
    val isRetrying: Boolean = false
) {
    val progress: Float
        get() = if (totalAttempts == 0) 0f else currentAttempt.toFloat() / totalAttempts.toFloat()
}

/**
 * Simple retry with state updates
 */
suspend fun <T> retryWithState(
    config: RetryConfig = RetryConfig.DEFAULT,
    onRetryState: ((RetryState) -> Unit)? = null,
    operation: suspend () -> ApiResult<T>
): ApiResult<T> {
    var lastResult: ApiResult<T> = ApiResult.UnknownError("No attempts made")

    for (attempt in 1..config.maxAttempts + 1) {
        // Update state
        onRetryState?.invoke(
            RetryState(
                currentAttempt = attempt - 1,
                totalAttempts = config.maxAttempts,
                isRetrying = attempt > 1
            )
        )
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
        if (delayMs > 0) delay(delayMs)
    }
    return lastResult
}