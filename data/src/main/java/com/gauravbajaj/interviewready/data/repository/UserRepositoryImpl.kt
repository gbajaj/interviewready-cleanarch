package com.gauravbajaj.interviewready.data.repository

import com.gauravbajaj.interviewready.data.api.UserApi
import com.gauravbajaj.interviewready.base.ApiResult
import com.gauravbajaj.interviewready.base.retry.RetryConfig
import com.gauravbajaj.interviewready.base.retry.flowWithRetry
import com.gauravbajaj.interviewready.base.getErrorMessage
import com.gauravbajaj.interviewready.base.retry.retryWithBackoff
import com.gauravbajaj.interviewready.data.di.DemoUserApiType
import com.gauravbajaj.interviewready.data.network.NetworkConnectivityChecker
import com.gauravbajaj.interviewready.model.User
import com.gauravbajaj.interviewready.repository.UserRepository
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [UserRepository] that provides a real data source for users.
 *
 * This implementation makes real network calls to the API to fetch user data.
 * It also implements retry logic with exponential backoff in case of network errors.
 *
 * @param userApi the API interface to make network calls to fetch users
 * @param context the Android context to get the application context
 * @param moshi the Moshi instance to parse JSON responses
 * @param networkChecker the network connectivity checker to check internet connectivity
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    @DemoUserApiType private val userApi: Lazy<UserApi>,
    private val networkChecker: Lazy<NetworkConnectivityChecker>
) : UserRepository {

    override fun getUsers(): Flow<ApiResult<List<User>>> =
        flowWithRetry(
            config = RetryConfig.DEFAULT,
            onRetry = { attempt, error ->
                // Log retry attempts (in a real app, you'd use proper logging)
                println("Retrying getUsers() - Attempt $attempt, Error: ${error.getErrorMessage()}")
            }, operation = {
                // Check network connectivity first
                if (!networkChecker.get().isConnected()) {
                    return@flowWithRetry ApiResult.NetworkError(
                        message = "No internet connection. ${networkChecker.get().getConnectionStatusDescription()}",
                        cause = null
                    )
                }

                safeApiCall {
                    // For now, using fake data. Will switch to real API later
                    userApi.get().getUsers()
                }
            }
        )

    override fun getUser(userId: String): Flow<ApiResult<User>> = flow {
        val result = retryWithBackoff(
            config = RetryConfig.DEFAULT,
            onRetry = { attempt, error ->
                println("Retrying getUser($userId) - Attempt $attempt, Error: ${error.getErrorMessage()}")
            }
        ) {
            // Check network connectivity first
            if (!networkChecker.get().isConnected()) {
                return@retryWithBackoff ApiResult.NetworkError(
                    message = "No internet connection. ${networkChecker.get().getConnectionStatusDescription()}",
                    cause = null
                )
            }

            val apiResult = safeApiCall {
                userApi.get().getUser(userId)
            }

            // Transform empty data error to more specific error for single user
            when (apiResult) {
                is ApiResult.EmptyDataError -> ApiResult.HttpError(
                    code = 404,
                    message = "User with ID '$userId' not found"
                )

                else -> apiResult
            }
        }

        emit(result)
    }

    /**
     * Safely executes an API call and converts exceptions to ApiResult
     */
    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
        return try {
            val result = apiCall()
            ApiResult.Success(result)
        } catch (e: Exception) {
            mapExceptionToApiResult(e)
        }
    }

    /**
     * Maps various exceptions to appropriate ApiResult error types
     */
    private fun <T> mapExceptionToApiResult(exception: Exception): ApiResult<T> {
        return when (exception) {
            // Network connectivity issues
            is UnknownHostException -> ApiResult.NetworkError(
                "No internet connection available",
                exception
            )

            is SocketTimeoutException -> ApiResult.NetworkError(
                "Connection timed out",
                exception
            )

            is JsonEncodingException -> ApiResult.ParseError(
                "Failed to parse server response",
                exception
            )

            is IOException -> ApiResult.NetworkError(
                "Network error occurred",
                exception
            )

            // HTTP errors
            is HttpException -> ApiResult.HttpError(
                code = exception.code(),
                message = "HTTP ${exception.code()}: ${exception.message()}",
                cause = exception
            )

            // JSON parsing errors
            is JsonDataException -> ApiResult.ParseError(
                "Invalid data format received",
                exception
            )

            // Any other error
            else -> ApiResult.UnknownError(
                "An unexpected error occurred: ${exception.message}",
                exception
            )
        }
    }
}