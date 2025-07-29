package com.gauravbajaj.interviewready.usecase

import com.gauravbajaj.interviewready.base.ApiResult
import com.gauravbajaj.interviewready.model.User
import com.gauravbajaj.interviewready.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for retrieving users with business logic applied.
 *
 * This use case can apply business rules, filtering, sorting, or other
 * domain-specific logic before returning the result to the presentation layer.
 */
@Singleton
class GetUsersUserCase @Inject constructor(
    private val userRepository: UserRepository
) {

    /**
     * Executes the use case to get users.
     *
     * @return Flow<ApiResult<List<User>>> A flow containing the result with
     *         business logic applied.
     */
    operator fun invoke(): Flow<ApiResult<List<User>>> {
        return userRepository.getUsers()
            .map { apiResult ->
                when (apiResult) {
                    is ApiResult.Success -> {
                        // Apply business logic here if needed
                        val users = apiResult.data

                        // Example business rules:
                        // - Filter out inactive users
                        // - Sort by name
                        // - Apply any domain-specific transformations
                        val processedUsers = users
                            .sortedBy { it.name }

                        ApiResult.Success(processedUsers)
                    }
                    // Pass through all error types unchanged
                    is ApiResult.NetworkError -> apiResult
                    is ApiResult.ParseError -> apiResult
                    is ApiResult.EmptyDataError -> apiResult
                    is ApiResult.HttpError -> apiResult
                    is ApiResult.UnknownError -> apiResult
                }
            }
    }
}