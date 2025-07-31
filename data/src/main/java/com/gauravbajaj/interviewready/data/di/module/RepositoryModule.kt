package com.gauravbajaj.interviewready.data.di.module

import android.content.Context
import com.gauravbajaj.interviewready.data.api.UserApi
import com.gauravbajaj.interviewready.data.di.DemoUserApiType
import com.gauravbajaj.interviewready.data.network.NetworkConnectivityChecker
import com.gauravbajaj.interviewready.data.repository.UserRepositoryImpl
import com.gauravbajaj.interviewready.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides dependencies for the application.
 * This module is installed in the [SingletonComponent], meaning that all provided dependencies
 * will have a singleton scope and live as long as the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        @DemoUserApiType userApi: UserApi,
        networkChecker: NetworkConnectivityChecker
    ): UserRepository =
        UserRepositoryImpl(userApi,networkChecker)
}
