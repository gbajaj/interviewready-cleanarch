package com.gauravbajaj.interviewready.repository

import com.gauravbajaj.interviewready.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user-related operations.
 *
 * This interface defines the contract for accessing user data.
 * The implementation is responsible for handling all data source complexities
 * including network errors, parsing errors, and data transformation.
 *
 * The repository implementation should handle errors gracefully and throw
 * meaningful exceptions that can be caught and handled by the use cases.
 */
interface UserRepository {
    /**
     * Retrieves a list of all users.
     *
     * @return Flow<List<User>> A flow that emits the list of users.
     *         Returns an empty list if no users are available (this is a valid state).
     * @throws UserRepositoryException when data cannot be retrieved
     * @throws NetworkException when network connectivity issues occur
     * @throws DataParsingException when response data cannot be parsed
     */
    fun getUsers(): Flow<List<User>>

    /**
     * Retrieves a specific user by their ID.
     *
     * @param userId The unique identifier of the user
     * @return Flow<User> A flow that emits the requested user
     * @throws UserRepositoryException when data cannot be retrieved
     * @throws NetworkException when network connectivity issues occur
     * @throws DataParsingException when response data cannot be parsed
     * @throws UserNotFoundException when the user with given ID doesn't exist
     */
    fun getUser(userId: String): Flow<User>
}

/**
 * Base exception for all repository-related errors
 */
sealed class UserRepositoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when network connectivity issues occur
 */
class NetworkException(
    message: String = "Network connection error",
    cause: Throwable? = null
) : UserRepositoryException(message, cause)

/**
 * Thrown when response data cannot be parsed
 */
class DataParsingException(
    message: String = "Failed to parse response data",
    cause: Throwable? = null
) : UserRepositoryException(message, cause)

/**
 * Thrown when a specific user cannot be found
 */
class UserNotFoundException(
    userId: String,
    message: String = "User with ID '$userId' not found"
) : UserRepositoryException(message)

/**
 * Thrown for any other unexpected repository errors
 */
class UnknownRepositoryException(
    message: String = "An unexpected error occurred",
    cause: Throwable? = null
) : UserRepositoryException(message, cause)