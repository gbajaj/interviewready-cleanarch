package com.gauravbajaj.interviewready.repository

import com.gauravbajaj.interviewready.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<List<User>>

    fun getUser(userId: String): Flow<User>
}