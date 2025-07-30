package com.gauravbajaj.interviewready.data.repository

import android.content.Context
import com.gauravbajaj.interviewready.base.ApiResult
import com.gauravbajaj.interviewready.data.network.NetworkConnectivityChecker
import com.gauravbajaj.interviewready.model.User
import com.gauravbajaj.interviewready.test.fakes.FakeUserApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Unit tests for UserRepositoryImpl using FakeUserApi.
 *
 * These tests verify the real repository implementation handles various scenarios:
 * - Network connectivity checks
 * - API success responses
 * - Various error conditions (network, HTTP, parsing)
 * - Retry logic behavior
 * - Error mapping from exceptions to ApiResult
 */
class UserRepositoryImplTest {

    private lateinit var repository: UserRepositoryImpl
    private lateinit var fakeUserApi: FakeUserApi
    private lateinit var mockContext: Context
    private lateinit var mockNetworkChecker: NetworkConnectivityChecker
    private lateinit var moshi: Moshi

    @Before
    fun setup() {
        fakeUserApi = FakeUserApi()
        mockContext = mockk()
        mockNetworkChecker = mockk()
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        // Mock context assets for fake data (used as fallback)
        val testJson = """
            [
                {"id": "1", "name": "John Doe", "email": "john@example.com"},
                {"id": "2", "name": "Jane Smith", "email": "jane@example.com"}
            ]
        """.trimIndent()

        val inputStream = ByteArrayInputStream(testJson.toByteArray())
        every { mockContext.assets.open("users.json") } returns inputStream

        // Default network checker to connected
        every { mockNetworkChecker.isConnected() } returns true
        every { mockNetworkChecker.getConnectionStatusDescription() } returns "Connected via WiFi"

        repository = UserRepositoryImpl(
            userApi = fakeUserApi,
            context = mockContext,
            moshi = moshi,
            networkChecker = mockNetworkChecker
        )
    }

    @Test
    fun `getUsers should return success when network is connected and uses fake data`() = runTest {
        // Given
        fakeUserApi.simulateSuccess()

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be Success", result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertEquals("Should return users from fake data", 3, successResult.data.size)
        assertEquals("Second user should be John Doe", "John Doe", successResult.data[0].name)
        assertEquals("First user should be Jane Smith", "Jane Smith", successResult.data[1].name)
    }

    @Test
    fun `getUsers should return network error when network is disconnected`() = runTest {
        // Given
        every { mockNetworkChecker.isConnected() } returns false
        every { mockNetworkChecker.getConnectionStatusDescription() } returns "No internet connection"

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be NetworkError", result is ApiResult.NetworkError)
        val networkError = result as ApiResult.NetworkError
        assertTrue(
            "Error message should contain connection info",
            networkError.message.contains("No internet connection")
        )
    }

    @Test
    fun `getUser should return success when user exists`() = runTest {
        // Given
        fakeUserApi.simulateSuccess()

        // When
        val result = repository.getUser("1").first()

        // Then
        assertTrue("Result should be Success", result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertEquals("Should return correct user", "1", successResult.data.id)
        assertEquals("User name should be correct", "John Doe", successResult.data.name)
    }

    @Test
    fun `getUser should return 404 error when user not found`() = runTest {
        // Given
        fakeUserApi.simulateClientError(404)

        // When
        val result = repository.getUser("999").first()

        // Then
        assertTrue("Result should be HttpError", result is ApiResult.HttpError)
        val httpError = result as ApiResult.HttpError
        assertEquals("Should be 404 error", 404, httpError.code)
    }

    @Test
    fun `getUsers should handle UnknownHostException as NetworkError`() = runTest {
        // Given
        fakeUserApi.simulateNoInternetConnection()

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be NetworkError", result is ApiResult.NetworkError)
        val networkError = result as ApiResult.NetworkError
        assertTrue(
            "Error message should mention internet connection",
            networkError.message.contains("No internet connection available")
        )
    }

    @Test
    fun `getUsers should handle SocketTimeoutException as NetworkError`() = runTest {
        // Given
        fakeUserApi.simulateConnectionTimeout()

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be NetworkError", result is ApiResult.NetworkError)
        val networkError = result as ApiResult.NetworkError
        assertTrue(
            "Error message should mention timeout",
            networkError.message.contains("Connection timed out")
        )
    }

    @Test
    fun `getUsers should handle IOException as NetworkError`() = runTest {
        // Given
        fakeUserApi.simulateNetworkError("General network error")

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be NetworkError", result is ApiResult.NetworkError)
        val networkError = result as ApiResult.NetworkError
        assertTrue(
            "Error message should mention network error",
            networkError.message.contains("Network error occurred")
        )
    }

    @Test
    fun `getUsers should handle JsonDataException as ParseError`() = runTest {
        // Given
        fakeUserApi.simulateJsonParseError()

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be ParseError", result is ApiResult.ParseError)
        val parseError = result as ApiResult.ParseError
        assertTrue(
            "Error message should mention data format",
            parseError.message.contains("Invalid data format received")
        )
    }

    @Test
    fun `getUsers should handle JsonEncodingException as ParseError`() = runTest {
        // Given
        fakeUserApi.simulateJsonEncodingError()

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be ParseError", result is ApiResult.ParseError)
        val parseError = result as ApiResult.ParseError
        assertTrue(
            "Error message should mention parsing failure",
            parseError.message.contains("Failed to parse server response")
        )
    }

    @Test
    fun `getUsers should handle HTTP 500 as HttpError`() = runTest {
        // Given
        fakeUserApi.simulateServerError(500)

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be HttpError", result is ApiResult.HttpError)
        val httpError = result as ApiResult.HttpError
        assertEquals("Should be 500 error", 500, httpError.code)
        assertTrue(
            "Error message should mention HTTP 500",
            httpError.message.contains("HTTP 500")
        )
    }

    @Test
    fun `getUsers should handle HTTP 401 as HttpError`() = runTest {
        // Given
        fakeUserApi.simulateAuthenticationFailure()

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be HttpError", result is ApiResult.HttpError)
        val httpError = result as ApiResult.HttpError
        assertEquals("Should be 401 error", 401, httpError.code)
    }

    @Test
    fun `getUsers should handle HTTP 429 as HttpError`() = runTest {
        // Given
        fakeUserApi.simulateRateLimit()

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be HttpError", result is ApiResult.HttpError)
        val httpError = result as ApiResult.HttpError
        assertEquals("Should be 429 error", 429, httpError.code)
    }

    @Test
    fun `getUsers should return empty list when API returns empty response`() = runTest {
        // Given
        fakeUserApi.simulateEmptyResponse()

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be Success", result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertTrue("Should return empty list", successResult.data.isEmpty())
    }

    @Test
    fun `getUsers should handle custom users data`() = runTest {
        // Given
        val customUsers = listOf(
            User(id = "100", name = "Custom User", email = "custom@example.com")
        )
        fakeUserApi.simulateSuccessWithData(customUsers)

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be Success", result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertEquals("Should return custom users", 1, successResult.data.size)
        assertEquals("Should return custom user", "Custom User", successResult.data[0].name)
    }

    @Test
    fun `getUsers should handle unexpected exceptions as UnknownError`() = runTest {
        // Given
        val customException = IllegalStateException("Unexpected error")
        fakeUserApi.simulateCustomException(customException)

        // When
        val result = repository.getUsers().first()

        // Then
        assertTrue("Result should be UnknownError", result is ApiResult.UnknownError)
        val unknownError = result as ApiResult.UnknownError
        assertTrue(
            "Error message should mention unexpected error",
            unknownError.message.contains("An unexpected error occurred")
        )
        assertEquals("Should preserve original exception", customException, unknownError.cause)
    }
}