package com.gauravbajaj.interviewready.di.module

import com.gauravbajaj.interviewready.repository.UserRepository
import com.gauravbajaj.interviewready.usecase.GetUsersUserCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides dependencies for the application.
 * This module is installed in the [SingletonComponent], meaning that all provided dependencies
 * will have a singleton scope and live as long as the application.
 */
@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    @Singleton
    fun provideUseCaseGetUsers(userRepository: UserRepository): GetUsersUserCase =
        GetUsersUserCase(userRepository)
}

