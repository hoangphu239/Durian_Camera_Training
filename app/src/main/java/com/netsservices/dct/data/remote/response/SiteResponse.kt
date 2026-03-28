package com.netsservices.dct.data.remote.response

data class SiteResponse(
    val items: List<Site>
)

data class Site(
    val id: String,
    val code: String,
    val name: String,
    val orchard: Orchard
)
