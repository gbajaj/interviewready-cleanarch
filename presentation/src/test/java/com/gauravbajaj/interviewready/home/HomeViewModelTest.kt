package com.gauravbajaj.interviewready.home

import android.content.Context
import com.gauravbajaj.interviewready.base.UIState
import com.gauravbajaj.interviewready.data.network.NetworkConnectivityChecker
import com.gauravbajaj.interviewready.data.repository.UserRepositoryImpl
import com.gauravbajaj.interviewready.test.fakes.FakeUserApi
import com.gauravbajaj.interviewready.usecase.GetUsersUserCase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.SocketTimeoutException

/**
 * Unit tests for HomeViewModel.
 *
 * These tests verify that the ViewModel correctly:
 * - Manages UI state transitions
 * - Converts ApiResult to appropriate UIState
 * - Handles loading, success, and error states
 * - Provides user-friendly error messages
 * - Manages retry logic and state
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var useCase: GetUsersUserCase
    private lateinit var repository: UserRepositoryImpl
    private lateinit var fakeUserApi: FakeUserApi
    private lateinit var mockContext: Context
    private lateinit var mockNetworkChecker: NetworkConnectivityChecker
    private lateinit var moshi: Moshi

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeUserApi = FakeUserApi()
        mockContext = mockk()
        mockNetworkChecker = mockk()
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        // Mock context assets for fake data
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

        useCase = GetUsersUserCase(repository)
        viewModel = HomeViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Initial`() {
        // Given & When - ViewModel is created

        // Then
        assertEquals("Initial state should be Initial", UIState.Initial, viewModel.uiState.value)
    }

    @Test
    fun `loadUsers should transition from Initial to Loading to Success`() = runTest {
        // Given
        fakeUserApi.simulateSuccess()

        // When
        viewModel.loadUsers()

        // Then - should start with Loading
        assertEquals("Should be in Initial state", UIState.Initial, viewModel.uiState.value)

        // Advance until idle to complete the coroutine
        advanceUntilIdle()

        // Then - should end with Success
        val finalState = viewModel.uiState.value
        assertTrue("Final state should be Success", finalState is UIState.Success)
        val successState = finalState as UIState.Success
        assertEquals("Should have correct number of users", 3, successState.data.size)
    }

    @Test
    fun `loadUsers should handle network error with user-friendly message`() = runTest {
        // Given
        fakeUserApi.simulateNoInternetConnection()

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is UIState.Error)
        val errorState = state as UIState.Error
        assertTrue("Should have user-friendly message",
            errorState.message.contains("Please check your internet connection"))
        assertTrue("Should be retryable", errorState.canRetry)
    }

    @Test
    fun `loadUsers should handle timeout error with appropriate message`() = runTest {
        // Given
        fakeUserApi.simulateConnectionTimeout()

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is UIState.Error)
        val errorState = state as UIState.Error
        assertTrue("Should have timeout error", errorState.throwable is SocketTimeoutException)
        assertTrue("Should have timeout message",
            errorState.message.contains("The request is taking too long"))
        assertTrue("Should be retryable", errorState.canRetry)
    }

    @Test
    fun `loadUsers should handle server error with appropriate message`() = runTest {
        // Given
        fakeUserApi.simulateServerError(500)

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is UIState.Error)
        val errorState = state as UIState.Error
        assertTrue("Should have server error message",
            errorState.message.contains("Server error. Please try again later"))
        assertTrue("Should be retryable", errorState.canRetry)
    }

    @Test
    fun `loadUsers should handle auth error as non-retryable`() = runTest {
        // Given
        fakeUserApi.simulateAuthenticationFailure()

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is UIState.Error)
        val errorState = state as UIState.Error
        assertTrue("Should have auth error message",
            errorState.message.contains("Authentication required"))
        assertTrue("Should not be retryable", !errorState.canRetry)
    }

    @Test
    fun `loadUsers should handle parse error with user-friendly message`() = runTest {
        // Given
        fakeUserApi.simulateJsonParseError()

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is UIState.Error)
        val errorState = state as UIState.Error
        assertTrue("Should have parse error message",
            errorState.message.contains("We're having trouble processing the data"))
        assertTrue("Should be retryable", errorState.canRetry)
    }

    @Test
    fun `loadUsers should handle empty response as success with empty list`() = runTest {
        // Given
        fakeUserApi.simulateEmptyResponse()

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is UIState.Success)
        val successState = state as UIState.Success
        assertTrue("Should have empty list", successState.data.isEmpty())
    }

    @Test
    fun `retry should call loadUsers again`() = runTest {
        // Given
        fakeUserApi.simulateNetworkError()
        viewModel.loadUsers()
        advanceUntilIdle()

        // Verify error state
        assertTrue("Should be in error state", viewModel.uiState.value is UIState.Error)

        // Configure API to succeed on retry
        fakeUserApi.simulateSuccess()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Success after retry", state is UIState.Success)
    }

    @Test
    fun `loadUsers should handle network disconnection`() = runTest {
        // Given
        every { mockNetworkChecker.isConnected() } returns false
        every { mockNetworkChecker.getConnectionStatusDescription() } returns "No internet connection"

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is UIState.Error)
        val errorState = state as UIState.Error
        assertTrue("Should mention internet connection",
            errorState.message.contains("Please check your internet connection"))
    }

    @Test
    fun `viewModel should handle different HTTP error codes appropriately`() = runTest {
        val testCases = listOf(
            400 to "Server returned error 400",
            403 to "You don't have permission",
            404 to "The requested data could not be found",
            429 to "Server returned error 429",
            502 to "Server error. Please try again later",
            503 to "Server error. Please try again later",
            504 to "Server error. Please try again later"
        )

        for ((code, expectedMessage) in testCases) {
            // Given
            fakeUserApi.reset()
            fakeUserApi.simulateHttpError(code, "Test error")

            // When
            viewModel.loadUsers()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue("State should be Error for code $code", state is UIState.Error)
            val errorState = state as UIState.Error
            assertTrue("Should have appropriate message for $code: ${errorState.message}",
                errorState.message.contains(expectedMessage, ignoreCase = true))
        }
    }

    @Test
    fun `viewModel should preserve error throwable information`() = runTest {
        // Given
        val customException = RuntimeException("Custom test exception")
        fakeUserApi.simulateCustomException(customException)

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is UIState.Error)
        val errorState = state as UIState.Error
        assertEquals("Should preserve original exception", customException, errorState.throwable)
    }
}