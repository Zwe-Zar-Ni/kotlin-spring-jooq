package com.vaddshah.ktjooq.features.products

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.features.products.dtos.CreateProductRequest
import com.vaddshah.ktjooq.features.products.dtos.Product
import com.vaddshah.ktjooq.features.products.dtos.ProductsFilter
import com.vaddshah.ktjooq.features.products.dtos.UpdateProductRequest
import com.vaddshah.ktjooq.generated.tables.records.ProductsRecord
import com.vaddshah.ktjooq.generated.tables.references.PRODUCTS
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.math.ceil

@Repository
class ProductRepository(
    private val dsl: DSLContext
) {
    fun index(filters: ProductsFilter): PageResponse<Product> {
        val queryCondition =
            if (!filters.query.isNullOrBlank()) PRODUCTS.NAME.like("%${filters.query}%") else DSL.noCondition()
        val minPriceCondition =
            if (filters.minPrice != null) PRODUCTS.PRICE.greaterOrEqual(filters.minPrice.toBigDecimal()) else DSL.noCondition()
        val maxPriceCondition =
            if (filters.maxPrice != null) PRODUCTS.PRICE.lessOrEqual(filters.maxPrice.toBigDecimal()) else DSL.noCondition()


        val totalElements =
            dsl.selectCount()
                .from(PRODUCTS)
                .where(queryCondition)
                .and(minPriceCondition)
                .and(maxPriceCondition)
                .fetchOne(0, Int::class.java) ?: error("Unable to fetch products");

        val totalPages = ceil(totalElements.toDouble() / filters.size.toDouble()).toInt()

        val products = dsl.selectFrom(PRODUCTS)
            .where(queryCondition)
            .and(minPriceCondition)
            .and(maxPriceCondition)
            .limit(filters.size)
            .offset((filters.page - 1) * filters.size)
            .fetch { toProductResponse(it) }

        return PageResponse(
            content = products,
            page = filters.page,
            size = filters.size,
            totalElements = totalElements.toLong(),
            totalPages = totalPages,
        )
    }

    @Transactional
    fun create(product: CreateProductRequest): Product? {
        val record = dsl.insertInto(PRODUCTS)
            .set(PRODUCTS.NAME, product.name)
            .set(PRODUCTS.PRICE, product.price.toBigDecimal())
            .set(PRODUCTS.STOCK, product.stock.toShort())
            .returning()
            .fetchOne()
        return toProductResponse(record)
    }

    fun details(id: Long): Product? {
        val product = dsl.selectFrom(PRODUCTS).where(PRODUCTS.ID.eq(id)).fetchOne()
        return toProductResponse(product)
    }

    fun update(id: Long, payload: UpdateProductRequest): Int? {
        val product = this.details(id) ?: return null
        val updatedRows = dsl.update(PRODUCTS)
            .set(PRODUCTS.NAME, if (!payload.name.isNullOrBlank()) payload.name else product.name)
            .set(
                PRODUCTS.PRICE,
                if (payload.price != null) payload.price.toBigDecimal() else product.price.toBigDecimal()
            )
            .set(PRODUCTS.STOCK, if (payload.stock != null) payload.stock.toShort() else product.stock.toShort())
            .where(PRODUCTS.ID.eq(id))
            .execute()
        return updatedRows
    }

    fun delete(id: Long): Int {
        val effectedRows = dsl.deleteFrom(PRODUCTS).where(PRODUCTS.ID.eq(id)).execute()
        return effectedRows
    }

    private fun toProductResponse(product: ProductsRecord?): Product? {
        return if (product == null) {
            null
        } else {
            Product(
                id = requireNotNull(product.id),
                name = requireNotNull(product.name),
                price = requireNotNull(product.price).toDouble(),
                stock = requireNotNull(product.stock).toInt(),
            )
        }
    }
}