package com.vaddshah.ktjooq.features.orders

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.features.orders.dtos.CreateOrderRequest
import com.vaddshah.ktjooq.features.orders.dtos.Order
import com.vaddshah.ktjooq.features.orders.dtos.OrderSummary
import com.vaddshah.ktjooq.features.orders.dtos.OrdersFilter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val repository: OrderRepository
) {
    fun index(userId: Long, filters: OrdersFilter): PageResponse<OrderSummary> =
        repository.index(userId, filters)

    @Transactional
    fun create(userId: Long, payload: CreateOrderRequest): Order =
        repository.create(userId, payload)

    fun details(userId: Long, orderId: Long): Order =
        repository.details(userId, orderId) ?: throw NoSuchElementException("Order not found.")
}