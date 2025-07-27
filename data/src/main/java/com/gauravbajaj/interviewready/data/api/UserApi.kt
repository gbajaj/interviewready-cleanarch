package com.gauravbajaj.interviewready.data.api

import com.gauravbajaj.interviewready.model.User
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface defining the API endpoints for user-related operations.
 * This interface is used by Retrofit to generate the network request implementations.
 */
interface UserApi {
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): User

    @GET("users")
    suspend fun getUsers(): List<User>
}
