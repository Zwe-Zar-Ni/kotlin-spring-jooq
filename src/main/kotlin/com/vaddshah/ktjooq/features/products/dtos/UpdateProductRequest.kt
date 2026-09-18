package com.vaddshah.ktjooq.features.products.dtos

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class UpdateProductRequest(
    val name: String? = null,

    @field:Min(0)
    val price: Double? = null,

    @field:Min(0)
    val stock: Int? = null
)