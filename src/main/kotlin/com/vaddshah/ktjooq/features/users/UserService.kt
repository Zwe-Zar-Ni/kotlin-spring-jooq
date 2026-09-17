package com.vaddshah.ktjooq.features.users

import com.vaddshah.ktjooq.features.users.dtos.CreateUserRequest
import com.vaddshah.ktjooq.features.users.dtos.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val repository: UserRepository) {

    fun findAll(): List<User> {
        return repository.findAll()
    }

    @Transactional
    fun create(request: CreateUserRequest): User {
        return repository.create(request)
    }
}