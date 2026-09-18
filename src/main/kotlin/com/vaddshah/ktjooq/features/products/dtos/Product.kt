package com.vaddshah.ktjooq.features.products.dtos

data class Product(
    val id : Long? = null,
    val name : String,
    val price : Double,
    val stock : Int
)