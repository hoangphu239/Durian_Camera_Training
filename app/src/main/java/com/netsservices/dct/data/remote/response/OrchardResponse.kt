package com.netsservices.dct.data.remote.response

data class OrchardResponse(
    val items: List<Orchard>
)

data class Orchard(
    val id: String,
    val code: String,
    val name: String,
    val plantation: Plantation
)