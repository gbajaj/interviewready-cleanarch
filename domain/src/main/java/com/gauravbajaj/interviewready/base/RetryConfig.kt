package com.gauravbajaj.interviewready.base

/**
 * Configuration class for retry logic with exponential backoff.
 *
 * This class defines the parameters for retry behavior including
 * maximum attempts, delays, and which error types should be retried.
 */
data class RetryConfig(
    /**
     * Maximum number of retry attempts before giving up
     */
    val maxAttempts: Int = 3,

    /**
     * Initial delay before first retry in milliseconds
     */
    val initialDelay: Long = 1000L,

    /**
     * Maximum delay between retries in milliseconds
     */
    val maxDelay: Long = 10000L,

    /**
     * Multiplier for exponential backoff (each retry delay = previous delay * multiplier)
     */
    val backoffMultiplier: Double = 2.0,

    /**
     * Whether to retry on network errors
     */
    val retryOnNetworkError: Boolean = true,

    /**
     * Whether to retry on HTTP errors
     */
    val retryOnHttpError: Boolean = true,

    /**
     * Whether to retry on parse errors (usually not recommended)
     */
    val retryOnParseError: Boolean = false,

    /**
     * Whether to retry on unknown errors
     */
    val retryOnUnknownError: Boolean = true,

    /**
     * HTTP status codes that should be retried (5xx errors typically)
     */
    val retryableHttpCodes: Set<Int> = setOf(500, 502, 503, 504)
) {

    /**
     * Calculates the delay for a specific retry attempt using exponential backoff
     *
     * @param attempt The current attempt number (1-based)
     * @return The delay in milliseconds for this attempt
     */
    fun calculateDelay(attempt: Int): Long {
        if (attempt <= 0) return 0L

        val delay = (initialDelay * Math.pow(backoffMultiplier, (attempt - 1).toDouble())).toLong()
        return minOf(delay, maxDelay)
    }

    /**
     * Determines if a given ApiResult error should be retried
     *
     * @param error The ApiResult error to check
     * @return true if this error type should be retried
     */
    fun shouldRetry(error: ApiResult<*>): Boolean {
        return when (error) {
            is ApiResult.NetworkError -> retryOnNetworkError
            is ApiResult.HttpError -> retryOnHttpError && error.code in retryableHttpCodes
            is ApiResult.ParseError -> retryOnParseError
            is ApiResult.UnknownError -> retryOnUnknownError
            is ApiResult.EmptyDataError -> false // Don't retry empty data
            is ApiResult.Success -> false // Don't retry success
        }
    }

    companion object {
        /**
         * Default retry configuration for most use cases
         */
        val DEFAULT = RetryConfig()

        /**
         * Aggressive retry configuration for critical operations
         */
        val AGGRESSIVE = RetryConfig(
            maxAttempts = 5,
            initialDelay = 500L,
            maxDelay = 30000L,
            backoffMultiplier = 1.5
        )

        /**
         * Conservative retry configuration for non-critical operations
         */
        val CONSERVATIVE = RetryConfig(
            maxAttempts = 2,
            initialDelay = 2000L,
            maxDelay = 5000L,
            backoffMultiplier = 2.0
        )

        /**
         * No retry configuration - fails fast
         */
        val NO_RETRY = RetryConfig(
            maxAttempts = 0
        )
    }
}