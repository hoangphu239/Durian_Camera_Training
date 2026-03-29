package com.netsservices.dct.data.remote.response

import com.netsservices.dct.domain.model.Country

data class DurianTypeResponse(
    val items: List<DurianItem>
)

data class DurianItem(
    val id: Int,
    val code: Int,
    val name: String,
    val localName: String,
    val description: String,
    val popularity: Int,
    val country: Country
)