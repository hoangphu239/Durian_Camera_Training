package com.netsservices.dct.domain.model

data class Orchard(
    val id: String,
    val code: String,
    val name: String,
    val plantation: Plantation
)
