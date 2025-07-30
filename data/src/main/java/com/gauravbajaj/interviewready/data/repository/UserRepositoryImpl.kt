package com.gauravbajaj.interviewready.data.repository

import android.content.Context
import com.gauravbajaj.interviewready.data.api.UserApi
import com.gauravbajaj.interviewready.base.ApiResult
import com.gauravbajaj.interviewready.data.network.NetworkConnectivityChecker
import com.gauravbajaj.interviewready.model.User
import com.gauravbajaj.interviewready.repository.UserRepository
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for fetching user data.
 *
 * This class handles all the complexity of network operations and error handling,
 * returning ApiResult types that provide rich error information to the domain layer.
 *
 * @property userApi The API service for user-related network calls.
 * @property context The application context, used to access resources.
 * @property moshi The Moshi instance for JSON serialization and deserialization.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    @ApplicationContext
    private val context: Context,
    private val moshi: Moshi,
    private val networkChecker: NetworkConnectivityChecker
) : UserRepository {
    override fun getUsers(): Flow<ApiResult<List<User>>> = flow {
        // Check network connectivity first
        if (!networkChecker.isConnected()) {
            emit(
                ApiResult.NetworkError(
                    message = "No internet connection. ${networkChecker.getConnectionStatusDescription()}",
                    cause = null
                )
            )
            return@flow
        }
        // Simulate network delay for better UX testing
        delay(1000)

        val apiResult = safeApiCall {
            // For now, using fake data. Will switch to real API later
            // userApi.getUsers()
            getFakeUsers()
        }

        emit(apiResult)
    }

    override fun getUser(userId: String): Flow<ApiResult<User>> = flow {
        // Check network connectivity first
        if (!networkChecker.isConnected()) {
            emit(
                ApiResult.NetworkError(
                    message = "No internet connection. ${networkChecker.getConnectionStatusDescription()}",
                    cause = null
                )
            )
            return@flow
        }
        val apiResult = safeApiCall {
            userApi.getUser(userId)
        }

        // Transform empty data error to more specific error for single user
        val finalResult = when (apiResult) {
            is ApiResult.EmptyDataError -> ApiResult.HttpError(
                code = 404,
                message = "User with ID '$userId' not found"
            )

            else -> apiResult
        }

        emit(finalResult)
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

            is JsonEncodingException -> ApiResult.ParseError(
                "Failed to parse server response",
                exception
            )

            // Any other error
            else -> ApiResult.UnknownError(
                "An unexpected error occurred: ${exception.message}",
                exception
            )
        }
    }

    /**
     * Loads fake user data from assets for development/testing
     */
    private fun getFakeUsers(): List<User> {
        return try {
            val jsonString = context.assets.open("users.json")
                .bufferedReader()
                .use { it.readText() }

            val users = moshi.adapter(Array<User>::class.java)
                .fromJson(jsonString)?.toList() ?: emptyList()

            return users
        } catch (e: Exception) {
            throw e // Will be caught and converted to ApiResult.ParseError
        }
    }
}