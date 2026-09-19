package com.vaddshah.ktjooq.features.users

import com.vaddshah.ktjooq.features.users.dtos.UserResponse
import org.springframework.stereotype.Service

@Service
class UserService(private val repository: UserRepository) {

    fun getUser(email: String): UserResponse {
        val user = repository.findByEmail(email)
        return if (user != null) {
            UserResponse(
                id = user.id ?: error("Insert user returned null"),
                email = user.email,
                name = user.name,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
            )
        } else {
            throw NoSuchElementException("User not found with email: $email")
        }
    }
}