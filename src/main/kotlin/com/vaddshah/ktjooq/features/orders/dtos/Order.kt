package com.vaddshah.ktjooq.features.orders.dtos

import com.vaddshah.ktjooq.features.products.dtos.Product
import com.vaddshah.ktjooq.features.users.dtos.UserResponse
import java.time.LocalDateTime

data class OrderItem(
    val id: Long,
    val createdAt: LocalDateTime,
    val productId : Long,
    val productName: String,
    val quantity: Int,
    val price: Double,
)

data class Order(
    val id: Long? = null,
    val userId: Long,
    val user: UserResponse? = null,
    val status: String,
    val total: Double,
    val items: List<OrderItem>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime? = null,
)