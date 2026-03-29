package com.netsservices.dct.data.remote.response

import com.netsservices.dct.domain.model.Country

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


