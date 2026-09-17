package com.vaddshah.ktjooq.features.users.dtos

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Password is required")
    @field:Min(value = 8, message = "Minimum password length is 8")
    val password: String,
)