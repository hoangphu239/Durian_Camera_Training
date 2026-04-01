package com.netsservices.dct.domain.model

data class Plantation(
    val id: String,
    val code: String,
    val name: String,
    val province: String,
    val country: Country
)
