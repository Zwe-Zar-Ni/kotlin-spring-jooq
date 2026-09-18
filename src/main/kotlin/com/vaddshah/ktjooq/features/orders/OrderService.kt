package com.vaddshah.ktjooq.features.orders

import com.vaddshah.ktjooq.features.orders.dtos.CreateOrderRequest
import com.vaddshah.ktjooq.features.orders.dtos.Order
import com.vaddshah.ktjooq.features.users.UserService
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val repository: OrderRepository,
    private val userService: UserService
) {
    fun index(email: String): List<Order> {
        val user = userService.getUser(email)
        return repository.index(user.id)
    }

    fun create(email: String, payload: CreateOrderRequest) {
        val user = userService.getUser(email)
        repository.create(user.id, payload)
    }

    fun details(email: String, orderId: Long): Order {
        val user = userService.getUser(email)
        return repository.details(user.id, orderId) ?: throw NoSuchElementException("Order not found.")
    }
}