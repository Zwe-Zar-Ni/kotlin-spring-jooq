package com.vaddshah.ktjooq.features.products

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.features.products.dtos.CreateProductRequest
import com.vaddshah.ktjooq.features.products.dtos.Product
import com.vaddshah.ktjooq.features.products.dtos.ProductsFilter
import com.vaddshah.ktjooq.features.products.dtos.UpdateProductRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val service: ProductService
) {

    @GetMapping
    fun getProducts(filters: ProductsFilter): ResponseEntity<PageResponse<Product>> {
        val products = service.index(filters)
        return ResponseEntity(products, HttpStatus.OK)
    }

    @PostMapping
    fun createProduct(@Validated @RequestBody payload: CreateProductRequest): ResponseEntity<Product> {
        val product = service.create(payload)
        return ResponseEntity(product, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getProductById(@PathVariable id: Long): ResponseEntity<Product> {
        val product = service.details(id)
        return ResponseEntity(product, HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    fun updateProduct(
        @Validated @RequestBody payload: UpdateProductRequest,
        @PathVariable id: Long,
    ): ResponseEntity<Int> {
        val effectedRows = service.update(id, payload)
        return ResponseEntity.ok(effectedRows)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Int> {
        val effectedRows = service.delete(id)
        return ResponseEntity.ok(effectedRows)
    }

}