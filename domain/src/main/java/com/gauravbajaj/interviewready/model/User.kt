package com.gauravbajaj.interviewready.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a user in the application.
 *
 * This data class is Parcelable, allowing it to be passed between Android components.
 *
 * @property id The unique identifier of the user.
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property avatarUrl The URL of the user's avatar image. This can be null if the user has no avatar.
 */
@Parcelize
data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null
) : Parcelable