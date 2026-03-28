package com.netsservices.dct.data.remote.response

data class PlantationResponse(
    val items: List<Plantation>
)

data class Plantation(
    val id: String,
    val code: String,
    val name: String,
    val province: String,
    val country: Country
)

data class Country(
    val id: String,
    val code: String,
    val name: String,
    val nameTh: String,
)
