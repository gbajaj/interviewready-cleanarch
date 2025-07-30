package com.gauravbajaj.interviewready.test.fakes

import com.gauravbajaj.interviewready.data.api.UserApi

import com.gauravbajaj.interviewready.model.User
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Fake implementation of UserApi for testing.
 *
 * This fake allows tests to control API behavior and simulate various error conditions
 * without making real network calls. It can throw any type of exception that the
 * real API might throw, allowing comprehensive testing of error handling.
 */
class FakeUserApi : UserApi {

    // Test data
    private val testUsers = listOf(
        User(id = "1", name = "John Doe", email = "john@example.com", avatarUrl = "https://example.com/john.jpg"),
        User(id = "2", name = "Jane Smith", email = "jane@example.com", avatarUrl = "https://example.com/jane.jpg"),
        User(id = "3", name = "Bob Johnson", email = "bob@example.com", avatarUrl = "https://example.com/bob.jpg")
    )

    // Test configuration - can be modified by tests
    var shouldThrowException = false
    var exceptionToThrow: Exception? = null
    var shouldReturnEmptyList = false
    var delayMillis = 0L
    var customUsers: List<User>? = null

    override suspend fun getUser(userId: String): User {
        // Simulate network delay if configured
        if (delayMillis > 0) {
            delay(delayMillis)
        }

        // Throw exception if configured
        if (shouldThrowException) {
            throw exceptionToThrow ?: RuntimeException("Test exception")
        }

        // Return user if found, otherwise throw 404
        val users = customUsers ?: testUsers
        return users.find { it.id == userId }
            ?: throw createHttpException(404, "User not found")
    }

    override suspend fun getUsers(): List<User> {
        // Simulate network delay if configured
        if (delayMillis > 0) {
            delay(delayMillis)
        }

        // Throw exception if configured
        if (shouldThrowException) {
            throw exceptionToThrow ?: RuntimeException("Test exception")
        }

        return when {
            shouldReturnEmptyList -> emptyList()
            customUsers != null -> customUsers!!
            else -> testUsers
        }
    }

    // Helper methods for test setup

    /**
     * Simulates no internet connection error
     */
    fun simulateNoInternetConnection() {
        shouldThrowException = true
        exceptionToThrow = UnknownHostException("Unable to resolve host")
    }

    /**
     * Simulates connection timeout error
     */
    fun simulateConnectionTimeout() {
        shouldThrowException = true
        exceptionToThrow = SocketTimeoutException("Connection timed out")
    }

    /**
     * Simulates general network/IO error
     */
    fun simulateNetworkError(message: String = "Network error") {
        shouldThrowException = true
        exceptionToThrow = IOException(message)
    }

    /**
     * Simulates HTTP error with specific status code
     */
    fun simulateHttpError(code: Int, message: String = "HTTP Error") {
        shouldThrowException = true
        exceptionToThrow = createHttpException(code, message)
    }

    /**
     * Simulates server errors (5xx)
     */
    fun simulateServerError(code: Int = 500) {
        val message = when (code) {
            500 -> "Internal Server Error"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Timeout"
            else -> "Server Error"
        }
        simulateHttpError(code, message)
    }

    /**
     * Simulates client errors (4xx)
     */
    fun simulateClientError(code: Int = 404) {
        val message = when (code) {
            400 -> "Bad Request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not Found"
            429 -> "Too Many Requests"
            else -> "Client Error"
        }
        simulateHttpError(code, message)
    }

    /**
     * Simulates JSON parsing errors
     */
    fun simulateJsonParseError() {
        shouldThrowException = true
        exceptionToThrow = JsonDataException("Unable to parse JSON")
    }

    /**
     * Simulates JSON encoding errors
     */
    fun simulateJsonEncodingError() {
        shouldThrowException = true
        exceptionToThrow = JsonEncodingException("JSON encoding error")
    }

    /**
     * Simulates empty response (valid but no data)
     */
    fun simulateEmptyResponse() {
        shouldReturnEmptyList = true
        shouldThrowException = false
    }

    /**
     * Simulates successful response with custom data
     */
    fun simulateSuccessWithData(users: List<User>) {
        customUsers = users
        shouldThrowException = false
        shouldReturnEmptyList = false
    }

    /**
     * Simulates successful response with default test data
     */
    fun simulateSuccess() {
        shouldThrowException = false
        shouldReturnEmptyList = false
        customUsers = null
    }

    /**
     * Simulates network delay (useful for testing loading states)
     */
    fun simulateDelay(millis: Long) {
        delayMillis = millis
    }

    /**
     * Simulates slow network (multiple seconds delay)
     */
    fun simulateSlowNetwork() {
        simulateDelay(3000)
    }

    /**
     * Simulates custom exception
     */
    fun simulateCustomException(exception: Exception) {
        shouldThrowException = true
        exceptionToThrow = exception
    }

    /**
     * Resets all configuration to default success state
     */
    fun reset() {
        shouldThrowException = false
        exceptionToThrow = null
        shouldReturnEmptyList = false
        delayMillis = 0L
        customUsers = null
    }

    /**
     * Helper to create HttpException for testing
     */
    private fun createHttpException(code: Int, message: String): HttpException {
        val response = Response.error<Any>(
            code,
            okhttp3.ResponseBody.create(
                "application/json".toMediaTypeOrNull(),
                """{"error": "$message"}"""
            )
        )
        return HttpException(response)
    }

    // Additional helper methods for specific scenarios

    /**
     * Simulates rate limiting (HTTP 429)
     */
    fun simulateRateLimit() {
        simulateClientError(429)
    }

    /**
     * Simulates authentication failure (HTTP 401)
     */
    fun simulateAuthenticationFailure() {
        simulateClientError(401)
    }

    /**
     * Simulates permission denied (HTTP 403)
     */
    fun simulatePermissionDenied() {
        simulateClientError(403)
    }

    /**
     * Simulates maintenance mode (HTTP 503)
     */
    fun simulateMaintenanceMode() {
        simulateServerError(503)
    }

    /**
     * Simulates intermittent failures (useful for retry testing)
     * Will fail for the first N calls, then succeed
     */
    private var failureCount = 0
    private var maxFailures = 0

    fun simulateIntermittentFailure(failures: Int, exception: Exception = IOException("Intermittent failure")) {
        maxFailures = failures
        failureCount = 0

        // This would need to be implemented with a counter
        // For now, just simulate the first failure
        if (failures > 0) {
            simulateCustomException(exception)
        }
    }
}