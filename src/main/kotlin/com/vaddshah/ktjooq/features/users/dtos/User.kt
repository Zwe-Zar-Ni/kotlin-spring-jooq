package com.vaddshah.ktjooq.features.users.dtos

import java.time.LocalDateTime

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)