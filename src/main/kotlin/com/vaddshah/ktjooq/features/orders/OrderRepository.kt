package com.vaddshah.ktjooq.features.orders

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.features.orders.dtos.*
import com.vaddshah.ktjooq.features.users.dtos.UserResponse
import com.vaddshah.ktjooq.generated.tables.records.OrderItemsRecord
import com.vaddshah.ktjooq.generated.tables.references.ORDERS
import com.vaddshah.ktjooq.generated.tables.references.ORDER_ITEMS
import com.vaddshah.ktjooq.generated.tables.references.PRODUCTS
import com.vaddshah.ktjooq.generated.tables.references.USERS
import org.jooq.DSLContext
import org.jooq.impl.DSL.row
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import kotlin.math.ceil

@Repository
class OrderRepository(
    private val dsl: DSLContext
) {
    companion object {
        private val log = LoggerFactory.getLogger(OrderRepository::class.java)
    }

    private fun userResponseRow() = row(
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

    fun index(userId: Long, filters: OrdersFilter): PageResponse<OrderSummary> {
        val totalElements = dsl.selectCount()
            .from(ORDERS)
            .where(ORDERS.USER_ID.eq(userId))
            .fetchOne(0, Int::class.java) ?: 0

        val totalPages = ceil(totalElements.toDouble() / filters.size.toDouble()).toInt()

        val orders = dsl.selectFrom(ORDERS)
            .where(ORDERS.USER_ID.eq(userId))
            .orderBy(ORDERS.CREATED_AT.desc())
            .limit(filters.size)
            .offset((filters.page - 1) * filters.size)
            .fetch { record ->
                OrderSummary(
                    id = requireNotNull(record.id),
                    status = requireNotNull(record.status),
                    total = requireNotNull(record.total),
                    createdAt = requireNotNull(record.createdAt),
                    updatedAt = record.updatedAt,
                )
            }

        return PageResponse(
            content = orders,
            page = filters.page,
            size = filters.size,
            totalElements = totalElements.toLong(),
            totalPages = totalPages,
        )
    }

    fun create(userId: Long, payload: CreateOrderRequest): Order {

        val productsById = dsl.selectFrom(PRODUCTS)
            .where(PRODUCTS.ID.`in`(payload.items.map { it.productId }))
            .fetch()
            .associateBy { requireNotNull(it.id) }

        payload.items.forEach { item ->
            val product = productsById[item.productId]
                ?: throw IllegalArgumentException("Product with id ${item.productId} does not exist.")
            val available = requireNotNull(product.stock)
            log.info("Product {} has stock {}, quantity {}", product.name, product.stock, item.quantity)
            if (item.quantity > available.toInt()) {
                throw IllegalStateException(
                    "Insufficient stock for product ${requireNotNull(product.name)}. " +
                            "Requested ${item.quantity}, available $available."
                )
            }
        }

        val total = payload.items.fold(BigDecimal.ZERO) { acc, item ->
            acc + item.quantity.toBigDecimal() * requireNotNull(productsById.getValue(item.productId).price)
        }

        val record = dsl.insertInto(ORDERS)
            .set(ORDERS.USER_ID, userId)
            .set(ORDERS.TOTAL, total)
            .returning()
            .fetchOne() ?: throw IllegalStateException("Order creation failed.")

        dsl.batchInsert(
            payload.items.map { item ->
                OrderItemsRecord().apply {
                    orderId = record.id
                    productId = item.productId
                    quantity = item.quantity.toShort()
                    price = requireNotNull(productsById.getValue(item.productId).price)
                }
            }
        ).execute()

        log.info("Created order {} for user {} with {} items", record.id, userId, payload.items.size)

        payload.items.forEach { (productId, quantity) ->
            val product = productsById.getValue(productId)
            val updatedRows = dsl.update(PRODUCTS)
                .set(PRODUCTS.STOCK, PRODUCTS.STOCK.sub(quantity.toShort()))
                .where(PRODUCTS.ID.eq(productId))
                .execute()
            if (updatedRows == 0) {
                log.info(
                    "Insufficient stock for product : {} , stock : {} , quantity : {}",
                    product.name,
                    product.stock,
                    quantity
                )
                throw IllegalStateException("Insufficient stock for product ${requireNotNull(product.name)}. Order rolled back.")
            }
        }

        return details(userId, requireNotNull(record.id))
            ?: throw IllegalStateException("Order creation failed.")
    }

    fun details(userId: Long, orderId: Long): Order? {
        val order = dsl.select(
            ORDERS.ID,
            ORDERS.STATUS,
            ORDERS.TOTAL,
            ORDERS.CREATED_AT,
            ORDERS.UPDATED_AT,
            ORDERS.USER_ID,
            userResponseRow()
        )
            .from(ORDERS)
            .leftJoin(USERS).on(ORDERS.USER_ID.eq(USERS.ID))
            .where(ORDERS.ID.eq(orderId))
            .and(ORDERS.USER_ID.eq(userId))
            .fetchOne() ?: run {
            log.warn("Order {} not found for user {}", orderId, userId)
            return null
        }

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
                    productId = requireNotNull(record.value2()),
                    quantity = requireNotNull(record.value3()).toInt(),
                    price = requireNotNull(record.value4()),
                    productName = requireNotNull(record.value5()),
                    createdAt = requireNotNull(record.value6()),
                )
            }

        return Order(
            id = order.value1(),
            status = requireNotNull(order.value2()),
            total = requireNotNull(order.value3()),
            createdAt = requireNotNull(order.value4()),
            updatedAt = requireNotNull(order.value5()),
            userId = requireNotNull(order.value6()),
            user = order.value7(),
            items = items
        )
    }
}