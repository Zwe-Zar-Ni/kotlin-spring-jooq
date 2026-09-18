package com.vaddshah.ktjooq.features.products

import com.vaddshah.ktjooq.common.api.PageResponse
import com.vaddshah.ktjooq.features.products.dtos.CreateProductRequest
import com.vaddshah.ktjooq.features.products.dtos.Product
import com.vaddshah.ktjooq.features.products.dtos.ProductsFilter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/products")
class ProductController(
    private val service: ProductService
) {

    @GetMapping
    fun getProducts(filters: ProductsFilter): ResponseEntity<PageResponse<Product>> {
        val products = service.findAll(filters)
        return ResponseEntity(products, HttpStatus.OK)
    }

    @PostMapping
    fun createProduct(@Validated @RequestBody product: CreateProductRequest): ResponseEntity<Product> {
        val product = service.create(product)
        return ResponseEntity(product, HttpStatus.CREATED)
    }
}