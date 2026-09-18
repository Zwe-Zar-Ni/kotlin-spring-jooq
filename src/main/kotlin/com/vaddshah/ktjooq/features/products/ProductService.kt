package com.vaddshah.ktjooq.features.products

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.features.products.dtos.CreateProductRequest
import com.vaddshah.ktjooq.features.products.dtos.Product
import com.vaddshah.ktjooq.features.products.dtos.ProductsFilter
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun findAll(filters: ProductsFilter): PageResponse<Product> {
        return productRepository.findAll(filters)
    }

    fun create(product: CreateProductRequest): Product {
        return productRepository.create(product)
    }
}