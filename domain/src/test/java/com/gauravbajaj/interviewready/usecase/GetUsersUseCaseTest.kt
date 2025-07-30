package com.gauravbajaj.interviewready.usecase

import android.content.Context
import com.gauravbajaj.interviewready.base.ApiResult
import com.gauravbajaj.interviewready.data.network.NetworkConnectivityChecker
import com.gauravbajaj.interviewready.data.repository.UserRepositoryImpl
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
 * Unit tests for GetUsersUserCase.
 *
 * These tests verify that the use case correctly:
 * - Delegates to the repository
 * - Applies business logic (sorting, filtering, etc.)
 * - Handles different result types appropriately
 * - Maintains the ApiResult structure
 */
class GetUsersUseCaseTest {

    private lateinit var useCase: GetUsersUserCase
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

        // Mock context assets for fake data
        val testJson = """
            [
                {"id": "3", "name": "Charlie Brown", "email": "charlie@example.com"},
                {"id": "1", "name": "Alice Smith", "email": "alice@example.com"},
                {"id": "2", "name": "Bob Johnson", "email": "bob@example.com"}
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

        useCase = GetUsersUserCase(repository)
    }

    @Test
    fun `invoke should return success with sorted users when repository succeeds`() = runTest {
        // Given
        fakeUserApi.simulateSuccess()

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be Success", result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        val users = successResult.data

        // Verify business logic: users should be sorted by name
        assertEquals("Should have 3 users", 3, users.size)
        assertEquals("First user should be Bob Johnson", "Bob Johnson", users[0].name)
        assertEquals("Second user should be Jane Smith", "Jane Smith", users[1].name)
        assertEquals("Third user should be John Doe", "John Doe", users[2].name)
    }

    @Test
    fun `invoke should return empty list when repository returns empty`() = runTest {
        // Given
        fakeUserApi.simulateEmptyResponse()

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be Success", result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        assertTrue("Should return empty list", successResult.data.isEmpty())
    }

    @Test
    fun `invoke should pass through network errors unchanged`() = runTest {
        // Given
        fakeUserApi.simulateNoInternetConnection()

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be NetworkError", result is ApiResult.NetworkError)
        val networkError = result as ApiResult.NetworkError
        assertTrue(
            "Error message should mention internet connection",
            networkError.message.contains("No internet connection available")
        )
    }

    @Test
    fun `invoke should pass through HTTP errors unchanged`() = runTest {
        // Given
        fakeUserApi.simulateServerError(500)

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be HttpError", result is ApiResult.HttpError)
        val httpError = result as ApiResult.HttpError
        assertEquals("Error code should be preserved", 500, httpError.code)
        assertTrue(
            "Error message should mention HTTP 500",
            httpError.message.contains("HTTP 500")
        )
    }

    @Test
    fun `invoke should pass through parse errors unchanged`() = runTest {
        // Given
        fakeUserApi.simulateJsonParseError()

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be ParseError", result is ApiResult.ParseError)
        val parseError = result as ApiResult.ParseError
        assertTrue(
            "Error message should mention data format",
            parseError.message.contains("Invalid data format received")
        )
    }

    @Test
    fun `invoke should pass through unknown errors unchanged`() = runTest {
        // Given
        val customException = IllegalStateException("Test unknown error")
        fakeUserApi.simulateCustomException(customException)

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be UnknownError", result is ApiResult.UnknownError)
        val unknownError = result as ApiResult.UnknownError
        assertTrue(
            "Error message should mention unexpected error",
            unknownError.message.contains("An unexpected error occurred")
        )
        assertEquals("Should preserve original exception", customException, unknownError.cause)
    }

    @Test
    fun `business logic should sort users alphabetically by name`() = runTest {
        // Given - repository returns users in random order
        val unsortedUsers = listOf(
            User(id = "3", name = "Zebra User", email = "zebra@example.com"),
            User(id = "1", name = "Alpha User", email = "alpha@example.com"),
            User(id = "2", name = "Beta User", email = "beta@example.com")
        )
        fakeUserApi.simulateSuccessWithData(unsortedUsers)

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be Success", result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        val users = successResult.data

        // Verify sorting: should be alphabetical by name
        assertEquals("First user should be Alpha User", "Alpha User", users[0].name)
        assertEquals("Second user should be Beta User", "Beta User", users[1].name)
        assertEquals("Third user should be Zebra User", "Zebra User", users[2].name)

        // Verify all users are sorted
        for (i in 0 until users.size - 1) {
            assertTrue(
                "Users should be sorted alphabetically: ${users[i].name} should come before ${users[i + 1].name}",
                users[i].name <= users[i + 1].name
            )
        }
    }

    @Test
    fun `business logic should preserve user data integrity`() = runTest {
        // Given
        val originalUsers = listOf(
            User(id = "1", name = "John Doe", email = "john@example.com", avatarUrl = "john.jpg"),
            User(id = "2", name = "Jane Smith", email = "jane@example.com", avatarUrl = "jane.jpg"),
            User(id = "3", name = "Bob Johnson", email = "bob@example.com", avatarUrl = "bob.jpg")
        )
        fakeUserApi.simulateSuccessWithData(originalUsers)

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be Success", result is ApiResult.Success)
        val successResult = result as ApiResult.Success
        val users = successResult.data

        // Verify data integrity - no user data should be modified, only sorted
        assertEquals("Should have correct number of users", 3, users.size)

        // Find each original user in the sorted list
        for (originalUser in originalUsers) {
            val foundUser = users.find { it.id == originalUser.id }
            assertEquals("User data should be preserved", originalUser, foundUser)
        }
    }

    @Test
    fun `invoke should handle network disconnection properly`() = runTest {
        // Given
        every { mockNetworkChecker.isConnected() } returns false
        every { mockNetworkChecker.getConnectionStatusDescription() } returns "No internet connection"

        // When
        val result = useCase.invoke().first()

        // Then
        assertTrue("Result should be NetworkError", result is ApiResult.NetworkError)
        val networkError = result as ApiResult.NetworkError
        assertTrue(
            "Error message should contain connection info",
            networkError.message.contains("No internet connection")
        )
    }
}