package com.vaddshah.ktjooq.features.users.dtos

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val name: String,
    val email: String,
    val password: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)