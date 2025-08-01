package com.gauravbajaj.interviewready.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gauravbajaj.interviewready.base.ApiResult
import com.gauravbajaj.interviewready.base.retry.RetryConfig
import com.gauravbajaj.interviewready.base.UIState
import com.gauravbajaj.interviewready.model.User
import com.gauravbajaj.interviewready.usecase.GetUsersUserCase
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen that manages user data and UI state.
 *
 * Converts ApiResult from the domain layer into UIState for the presentation layer,
 * providing user-friendly error messages and handling retry logic.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUsersUserCase: Lazy<GetUsersUserCase>
) : ViewModel() {
    private val _uiState = MutableStateFlow<UIState<List<User>>>(UIState.Initial)
    val uiState: StateFlow<UIState<List<User>>> = _uiState

    private val _retryState = MutableStateFlow<RetryInfo?>(null)
    val retryState: StateFlow<RetryInfo?> = _retryState

    /**
     * Loads users from the use case and updates the UI state accordingly.
     * Implements automatic retry with exponential backoff for appropriate errors.
     */
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            _retryState.value = null

            getUsersUserCase.get().invoke()
                .catch { throwable ->
                    // Handle any unexpected errors that escape the ApiResult system
                    _uiState.value = UIState.Error(
                        message = "An unexpected error occurred",
                        throwable = throwable,
                        canRetry = true
                    )
                    _retryState.value = null
                }
                .collect { apiResult ->
                    _uiState.value = mapApiResultToUIState(apiResult)
                    _retryState.value = null
                }
        }
    }

    /**
     * Loads users with custom retry configuration.
     * Useful for different retry strategies based on user action or context.
     */
    fun loadUsersWithRetry(retryConfig: RetryConfig = RetryConfig.DEFAULT) {
        viewModelScope.launch {
            _uiState.value = UIState.Loading
            _retryState.value = RetryInfo(
                currentAttempt = 0,
                maxAttempts = retryConfig.maxAttempts,
                isRetrying = false
            )

            getUsersUserCase.get().invoke()
                .catch { throwable ->
                    _uiState.value = UIState.Error(
                        message = "An unexpected error occurred",
                        throwable = throwable,
                        canRetry = true
                    )
                    _retryState.value = null
                }
                .collect { apiResult ->
                    _uiState.value = mapApiResultToUIState(apiResult)
                    _retryState.value = null
                }
        }
    }

    /**
     * Converts ApiResult to UIState with appropriate user-facing messages.
     */
    private fun mapApiResultToUIState(apiResult: ApiResult<List<User>>): UIState<List<User>> {
        return when (apiResult) {
            is ApiResult.Success -> {
                UIState.Success(apiResult.data)
            }

            is ApiResult.NetworkError -> {
                UIState.Error(
                    message = getUserFriendlyNetworkMessage(apiResult),
                    throwable = apiResult.cause,
                    canRetry = true
                )
            }

            is ApiResult.ParseError -> {
                UIState.Error(
                    message = "We're having trouble processing the data. Please try again.",
                    throwable = apiResult.cause,
                    canRetry = true
                )
            }

            is ApiResult.HttpError -> {
                UIState.Error(
                    message = getUserFriendlyHttpMessage(apiResult),
                    throwable = apiResult.cause,
                    canRetry = apiResult.code != 401 // Don't retry auth errors
                )
            }

            is ApiResult.EmptyDataError -> {
                // This shouldn't happen for getUsers() but handle it gracefully
                UIState.Success(emptyList())
            }

            is ApiResult.UnknownError -> {
                UIState.Error(
                    message = "Something unexpected happened. Please try again.",
                    throwable = apiResult.cause,
                    canRetry = true
                )
            }
        }
    }

    /**
     * Provides user-friendly messages for network errors.
     */
    private fun getUserFriendlyNetworkMessage(error: ApiResult.NetworkError): String {
        return when {
            error.message.contains("timed out", ignoreCase = true) -> {
                "The request is taking too long. Please try again"
            }
            error.message.contains("internet", ignoreCase = true) ||
                    error.message.contains("connection", ignoreCase = true) -> {
                "Please check your internet connection and try again"
            }
            else -> {
                "Network error occurred. Please try again"
            }
        }
    }

    /**
     * Provides user-friendly messages for HTTP errors.
     */
    private fun getUserFriendlyHttpMessage(error: ApiResult.HttpError): String {
        return when (error.code) {
            401 -> "Authentication required. Please log in again"
            403 -> "You don't have permission to access this data"
            404 -> "The requested data could not be found"
            500, 502, 503, 504 -> "Server error. Please try again later"
            else -> "Server returned error ${error.code}. Please try again"
        }
    }

    /**
     * Handles retry logic - simply calls loadUsers again.
     * Could be enhanced with exponential backoff in the future.
     */
    fun retry() {
        loadUsers()
    }
}

/**
 * Data class to represent retry information for UI display
 */
data class RetryInfo(
    val currentAttempt: Int,
    val maxAttempts: Int,
    val isRetrying: Boolean
) {
    val progress: Float
        get() = if (maxAttempts == 0) 0f else currentAttempt.toFloat() / maxAttempts.toFloat()

    val hasMoreAttempts: Boolean
        get() = currentAttempt < maxAttempts
}