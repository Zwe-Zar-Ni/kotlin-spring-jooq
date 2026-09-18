package com.vaddshah.ktjooq.features.orders

import com.vaddshah.ktjooq.features.orders.dtos.CreateOrderRequest
import com.vaddshah.ktjooq.features.orders.dtos.Order
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val service: OrderService
) {
    @GetMapping
    fun getAll(authentication: Authentication): ResponseEntity<List<Order>> {
        val orders = service.index(authentication.name)
        return ResponseEntity.ok(orders)
    }

    @PostMapping
    fun createOrder(
        @Validated @RequestBody payload: CreateOrderRequest,
        authentication: Authentication
    ) {
        service.create(authentication.name, payload)
    }

    @GetMapping("/{id}")
    fun getOrderDetails(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Order> {
        val order = service.details(authentication.name, id)
        return ResponseEntity.ok(order)
    }
}