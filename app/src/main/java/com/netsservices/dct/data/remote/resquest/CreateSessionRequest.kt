package com.netsservices.dct.data.remote.resquest

data class CreateSessionRequest(
    val purpose: String,
    val durianTypeId: Int,
    val plantationId: String,
    val orchardId: String,
    val siteId: String,
    val fileId: String
)