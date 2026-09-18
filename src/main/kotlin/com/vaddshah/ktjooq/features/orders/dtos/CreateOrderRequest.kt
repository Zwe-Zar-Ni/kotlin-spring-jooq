package com.vaddshah.ktjooq.features.orders.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class Item(
    val productId: Long,
    val quantity: Int,
    val price: Double,
)

data class CreateOrderRequest(
    @field:NotEmpty("Order at least 1 item.")
    @field:Valid
    val items: List<Item>
)