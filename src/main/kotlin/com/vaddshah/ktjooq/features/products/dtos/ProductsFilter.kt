package com.vaddshah.ktjooq.features.products.dtos

data class ProductsFilter(
    val page : Int = 1,
    val size : Int = 20,
    val query : String? =null,
    val minPrice : Int? =null,
    val maxPrice : Int? =null,
)