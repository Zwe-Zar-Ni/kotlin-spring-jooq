package com.vaddshah.ktjooq.features.products.dtos

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateProductRequest(
    @field:NotBlank("Name is required.")
    val name: String,
    
    @field:Min(1)
    val price: Double,

    @field:Min(0)
    val stock: Int
)