package com.vaddshah.ktjooq.features.auth.dtos

import com.vaddshah.ktjooq.features.users.dtos.UserResponse

data class AuthResponse(
    val token: String,
    val user : UserResponse? = null
)