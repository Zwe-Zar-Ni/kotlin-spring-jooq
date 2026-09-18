package com.vaddshah.ktjooq.features.products

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.features.products.dtos.CreateProductRequest
import com.vaddshah.ktjooq.features.products.dtos.Product
import com.vaddshah.ktjooq.features.products.dtos.ProductsFilter
import com.vaddshah.ktjooq.features.products.dtos.UpdateProductRequest
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun index(filters: ProductsFilter): PageResponse<Product> {
        return productRepository.index(filters)
    }

    fun create(payload: CreateProductRequest): Product {
        return productRepository.create(payload) ?: throw IllegalStateException("Product creation failed.")
    }

    fun details(id: Long): Product {
        return productRepository.details(id) ?: throw NoSuchElementException("Product with id $id not found")
    }

    fun update(id: Long, payload: UpdateProductRequest): Int {
        val effectedRows = productRepository.update(id, payload)
        if (effectedRows == null || effectedRows == 0) {
            throw NoSuchElementException("Product with id $id not found")
        } else {
            return effectedRows
        }
    }

    fun delete(id: Long): Int {
        val effectedRows = productRepository.delete(id)
        if (effectedRows == 0) {
            throw NoSuchElementException("Product with id $id not found")
        } else {
            return effectedRows
        }
    }
}