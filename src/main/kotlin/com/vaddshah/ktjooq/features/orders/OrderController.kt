package com.vaddshah.ktjooq.features.orders

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.common.security.AuthenticatedUser
import com.vaddshah.ktjooq.features.orders.dtos.CreateOrderRequest
import com.vaddshah.ktjooq.features.orders.dtos.Order
import com.vaddshah.ktjooq.features.orders.dtos.OrderSummary
import com.vaddshah.ktjooq.features.orders.dtos.OrdersFilter
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val service: OrderService
) {
    @GetMapping
    fun getAll(
        @AuthenticationPrincipal user: AuthenticatedUser,
        filters: OrdersFilter
    ): ResponseEntity<PageResponse<OrderSummary>> {
        val orders = service.index(user.id, filters)
        return ResponseEntity.ok(orders)
    }

    @PostMapping
    fun createOrder(
        @Valid @RequestBody payload: CreateOrderRequest,
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<Order> {
        val order = service.create(user.id, payload)
        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @GetMapping("/{id}")
    fun getOrderDetails(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: AuthenticatedUser
    ): ResponseEntity<Order> {
        val order = service.details(user.id, id)
        return ResponseEntity.ok(order)
    }
}