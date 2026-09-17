package com.vaddshah.ktjooq.features.users

import com.vaddshah.ktjooq.features.users.dtos.CreateUserRequest
import com.vaddshah.ktjooq.features.users.dtos.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> =
        ResponseEntity.ok(userService.findAll())

    @PostMapping
    fun createUser(@Validated @RequestBody request: CreateUserRequest): ResponseEntity<User> =
        ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request))
}