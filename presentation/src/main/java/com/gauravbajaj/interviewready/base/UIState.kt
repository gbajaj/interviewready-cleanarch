package com.gauravbajaj.interviewready.base

/**
 * Represents the different states of a UI component.
 *
 * This sealed class is designed to be used with StateFlow to observe UI state changes
 * in ViewModels and update the UI accordingly in Composables.
 *
 * @param T The type of data associated with the [Success] state.
 */
sealed class UIState<out T> {
    /**
     * Initial state before any operation has started
     */
    data object Initial : UIState<Nothing>()

    /**
     * Loading state during data fetching or processing
     */
    data object Loading : UIState<Nothing>()

    /**
     * Successful state with data
     */
    data class Success<T>(val data: T) : UIState<T>()

    /**
     * Error state with the underlying throwable and user-friendly message
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
        val canRetry: Boolean = true
    ) : UIState<Nothing>()
}

/**
 * Extension functions for easier UIState handling
 */

/**
 * Returns true if the state represents a successful operation
 */
fun <T> UIState<T>.isSuccess(): Boolean = this is UIState.Success

/**
 * Returns true if the state represents any kind of error
 */
fun <T> UIState<T>.isError(): Boolean = this is UIState.Error

/**
 * Returns true if the state is loading
 */
fun <T> UIState<T>.isLoading(): Boolean = this is UIState.Loading

/**
 * Returns true if retry is possible for error states
 */
fun <T> UIState<T>.canRetry(): Boolean = when (this) {
    is UIState.Error -> canRetry
    else -> false
}

/**
 * Returns the data if in success state, null otherwise
 */
fun <T> UIState<T>.getDataOrNull(): T? = when (this) {
    is UIState.Success -> data
    else -> null
}

/**
 * Returns the error message for any error state
 */
fun <T> UIState<T>.getErrorMessage(): String? = when (this) {
    is UIState.Error -> message
    else -> null
}

/**
 * Returns the underlying throwable for error states
 */
fun <T> UIState<T>.getThrowable(): Throwable? = when (this) {
    is UIState.Error -> throwable
    else -> null
}

/**
 * Returns the error type based on the throwable
 */
fun <T> UIState<T>.getErrorType(): String? = when (this) {
    is UIState.Error -> throwable?.let { it::class.simpleName }
    else -> null
}

/**
 * Maps the success data to another type while preserving other states
 */
inline fun <T, R> UIState<T>.map(transform: (T) -> R): UIState<R> = when (this) {
    is UIState.Initial -> UIState.Initial
    is UIState.Loading -> UIState.Loading
    is UIState.Success -> UIState.Success(transform(data))
    is UIState.Error -> this
}

/**
 * Executes a block if the state is successful
 */
inline fun <T> UIState<T>.onSuccess(action: (T) -> Unit): UIState<T> {
    if (this is UIState.Success) {
        action(data)
    }
    return this
}

/**
 * Executes a block if the state is any error
 */
inline fun <T> UIState<T>.onError(action: (String) -> Unit): UIState<T> {
    getErrorMessage()?.let(action)
    return this
}

/**
 * Helper functions to create UIState instances
 */
object UIStateFactory {
    fun <T> success(data: T): UIState<T> = UIState.Success(data)
    fun <T> loading(): UIState<T> = UIState.Loading
    fun <T> initial(): UIState<T> = UIState.Initial
    fun <T> error(
        message: String,
        throwable: Throwable? = null,
        canRetry: Boolean = true
    ): UIState<T> = UIState.Error(message, throwable, canRetry)
}