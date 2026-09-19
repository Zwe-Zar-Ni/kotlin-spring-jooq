package com.vaddshah.ktjooq.features.orders.dtos

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderSummary(
    val id: Long,
    val status: String,
    val total: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
)