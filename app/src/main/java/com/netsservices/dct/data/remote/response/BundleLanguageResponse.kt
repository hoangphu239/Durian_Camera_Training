package com.netsservices.dct.data.remote.response

data class BundleLanguageResponse(
    val ok: Boolean,
    val version: String,
    val langId: String,
    val prefix: String,
    val count: Int,
    val items: Map<String, String>
)