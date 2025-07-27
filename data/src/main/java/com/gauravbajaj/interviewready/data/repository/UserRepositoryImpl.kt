package com.gauravbajaj.interviewready.data.repository

import android.content.Context
import com.gauravbajaj.interviewready.data.api.UserApi
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.gauravbajaj.interviewready.R
import com.gauravbajaj.interviewready.model.User
import com.gauravbajaj.interviewready.repository.UserRepository
import kotlinx.coroutines.delay

/**
 * Repository for fetching user data.
 *
 * This class provides methods to fetch a list of users or a single user by their ID.
 * It uses a [UserApi] to make network requests and [Moshi] for JSON parsing.
 * For development purposes, it currently uses a local JSON file ([R.raw.users]) to provide fake user data.
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
    private val moshi: Moshi
): UserRepository {
    override fun getUsers(): Flow<List<User>> = flow {
        try {
            delay(1000)
//            emit(userApi.getUsers())
            emit(getFakeUsers())

        } catch (e: Exception) {
            throw Exception("Failed to fetch users", e)
        }
    }

    override fun getUser(userId: String): Flow<User> = flow {
        try {
            emit(userApi.getUser(userId))
        } catch (e: Exception) {
            throw Exception("Failed to fetch user", e)
        }
    }

    private fun getFakeUsers(): List<User> {
        val jsonString =
            context.assets.open("users.json")
                .bufferedReader()
                .use { it.readText() }
        return moshi.adapter(Array<User>::class.java).fromJson(jsonString)?.toList() ?: emptyList()
    }
}
