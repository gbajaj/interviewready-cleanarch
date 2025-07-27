package com.gauravbajaj.interviewready.usecase

import com.gauravbajaj.interviewready.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetUsersUserCase @Inject constructor(private val userRepository: UserRepository) {
    operator fun invoke() = userRepository.getUsers()

}
