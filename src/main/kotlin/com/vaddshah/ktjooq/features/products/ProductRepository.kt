package com.vaddshah.ktjooq.features.products

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.features.products.dtos.CreateProductRequest
import com.vaddshah.ktjooq.features.products.dtos.Product
import com.vaddshah.ktjooq.features.products.dtos.ProductsFilter
import com.vaddshah.ktjooq.generated.tables.records.ProductsRecord
import com.vaddshah.ktjooq.generated.tables.references.PRODUCTS
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import kotlin.math.ceil

@Repository
class ProductRepository(
    private val dsl: DSLContext
) {
    fun findAll(filters: ProductsFilter): PageResponse<Product> {
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

    fun create(product: CreateProductRequest): Product {
        val record = dsl.insertInto(PRODUCTS)
            .set(PRODUCTS.NAME, product.name)
            .set(PRODUCTS.PRICE, product.price.toBigDecimal())
            .set(PRODUCTS.STOCK, product.stock.toShort())
            .returning()
            .fetchOne()
        return toProductResponse(record ?: error("Insert returned no row"))
    }

    private fun toProductResponse(product: ProductsRecord): Product {
        return Product(
            id = requireNotNull(product.id),
            name = requireNotNull(product.name),
            price = requireNotNull(product.price).toDouble(),
            stock = requireNotNull(product.stock).toInt(),
        )
    }
}