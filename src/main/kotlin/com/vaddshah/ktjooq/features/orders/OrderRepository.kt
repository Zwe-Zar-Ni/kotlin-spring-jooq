package com.vaddshah.ktjooq.features.orders

import com.vaddshah.ktjooq.features.orders.dtos.CreateOrderRequest
import com.vaddshah.ktjooq.features.orders.dtos.Order
import com.vaddshah.ktjooq.features.orders.dtos.OrderItem
import com.vaddshah.ktjooq.features.users.dtos.UserResponse
import com.vaddshah.ktjooq.generated.tables.references.ORDERS
import com.vaddshah.ktjooq.generated.tables.references.ORDER_ITEMS
import com.vaddshah.ktjooq.generated.tables.references.PRODUCTS
import com.vaddshah.ktjooq.generated.tables.references.USERS
import org.jooq.DSLContext
import org.jooq.impl.DSL.row
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class OrderRepository(
    private val dsl: DSLContext
) {
    fun index(userId: Long): List<Order> {
        return dsl.select(
            ORDERS.ID,
            ORDERS.STATUS,
            ORDERS.TOTAL,
            ORDERS.CREATED_AT,
            ORDERS.UPDATED_AT,
            ORDERS.USER_ID,
            row(
                USERS.ID,
                USERS.NAME,
                USERS.EMAIL,
                USERS.CREATED_AT,
                USERS.UPDATED_AT
            ).mapping { id, name, email, createdAt, updatedAt ->
                UserResponse(
                    requireNotNull(id),
                    requireNotNull(name),
                    requireNotNull(email),
                    requireNotNull(createdAt),
                    updatedAt
                )
            }
        )
            .from(ORDERS)
            .leftJoin(USERS).on(ORDERS.USER_ID.eq(USERS.ID))
            .where(ORDERS.USER_ID.eq(userId))
            .fetch { record ->
                Order(
                    id = record.value1(),
                    status = requireNotNull(record.value2()),
                    total = requireNotNull(record.value3()).toDouble(),
                    createdAt = requireNotNull(record.value4()),
                    updatedAt = requireNotNull(record.value5()),
                    userId = requireNotNull(record.value6()),
                    user = record.value7(),
                    items = emptyList()
                )
            }
    }

    @Transactional
    fun create(userId: Long, payload: CreateOrderRequest) {

        var total = 0.0
        payload.items.forEach { item -> total += item.quantity * item.price }

        val record = dsl.insertInto(ORDERS)
            .set(ORDERS.USER_ID, userId)
            .set(ORDERS.TOTAL, total.toBigDecimal())
            .returning()
            .fetchOne() ?: throw IllegalStateException("Order creation failed.")

        payload.items.forEach { item ->
            dsl.insertInto(ORDER_ITEMS)
                .set(ORDER_ITEMS.PRODUCT_ID, item.productId)
                .set(ORDER_ITEMS.QUANTITY, item.quantity.toShort())
                .set(ORDER_ITEMS.PRICE, item.price.toBigDecimal())
                .set(ORDER_ITEMS.ORDER_ID, record.id)
                .returning()
                .fetchOne() ?: throw IllegalStateException("Order creation failed.")
        }
    }

    fun details(userId: Long, orderId: Long): Order? {
        val items = dsl.select(
            ORDER_ITEMS.ID,
            ORDER_ITEMS.PRODUCT_ID,
            ORDER_ITEMS.QUANTITY,
            ORDER_ITEMS.PRICE,
            PRODUCTS.NAME,
            ORDER_ITEMS.CREATED_AT
        ).from(ORDER_ITEMS)
            .join(PRODUCTS).on(ORDER_ITEMS.PRODUCT_ID.eq(PRODUCTS.ID))
            .where(ORDER_ITEMS.ORDER_ID.eq(orderId))
            .fetch { record ->
                OrderItem(
                    id = requireNotNull(record.value1()),
                    productId = requireNotNull(record.value1()),
                    quantity = requireNotNull(record.value3()).toInt(),
                    price = requireNotNull(record.value4()).toDouble(),
                    productName = requireNotNull(record.value5()),
                    createdAt = requireNotNull(record.value6()),
                )
            }

        val userRow = row(
            USERS.ID,
            USERS.NAME,
            USERS.EMAIL,
            USERS.CREATED_AT,
            USERS.UPDATED_AT
        ).mapping { id, name, email, createdAt, updatedAt ->
            UserResponse(
                requireNotNull(id),
                requireNotNull(name),
                requireNotNull(email),
                requireNotNull(createdAt),
                updatedAt
            )
        }

        return dsl.select(
            ORDERS.ID,
            ORDERS.STATUS,
            ORDERS.TOTAL,
            ORDERS.CREATED_AT,
            ORDERS.UPDATED_AT,
            ORDERS.USER_ID,
            userRow,
        )
            .from(ORDERS)
            .leftJoin(USERS).on(ORDERS.USER_ID.eq(USERS.ID))
            .where(ORDERS.ID.eq(orderId))
            .and(ORDERS.USER_ID.eq(userId))
            .fetchOne { record ->
                Order(
                    id = record.value1(),
                    status = requireNotNull(record.value2()),
                    total = requireNotNull(record.value3()).toDouble(),
                    createdAt = requireNotNull(record.value4()),
                    updatedAt = requireNotNull(record.value5()),
                    userId = requireNotNull(record.value6()),
                    user = record.value7(),
                    items = items
                )
            }
    }
}