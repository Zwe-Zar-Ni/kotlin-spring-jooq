package com.vaddshah.ktjooq.common.api


import java.time.Instant

data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val errors: Map<String, String>? = null
)