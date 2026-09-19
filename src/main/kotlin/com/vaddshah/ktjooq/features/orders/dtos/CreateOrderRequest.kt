package com.vaddshah.ktjooq.features.orders.dtos

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive

data class Item(
    @field:Positive
    val productId: Long,

    @field:Positive
    val quantity: Int,
)

data class CreateOrderRequest(
    @field:NotEmpty("Order at least 1 item.")
    @field:Valid
    val items: List<Item>
)