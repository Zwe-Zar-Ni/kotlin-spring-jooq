package com.vaddshah.ktjooq.features.users

import com.vaddshah.ktjooq.features.users.dtos.UserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    fun getCurrentUser(
        authentication: Authentication,
    ): ResponseEntity<UserResponse> {
        val user = userService.getUser(authentication.name)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

}