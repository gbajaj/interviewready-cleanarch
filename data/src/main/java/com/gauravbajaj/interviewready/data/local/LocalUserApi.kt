package com.gauravbajaj.interviewready.data.local

import android.content.Context
import com.gauravbajaj.interviewready.data.api.UserApi
import com.gauravbajaj.interviewready.model.User
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext

class LocalUserApi(val moshi: Moshi, @ApplicationContext val context: Context) : UserApi {
    /**
     * Loads fake user data from assets for development/testing
     */
    /**
     * Loads fake user data from assets for development/testing
     */
    override suspend fun getUsers(): List<User> {
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

    override suspend fun getUser(userId: String): User {
        return User(id = "1", name = "Alpha User", email = "alpha@example.com")
    }

}