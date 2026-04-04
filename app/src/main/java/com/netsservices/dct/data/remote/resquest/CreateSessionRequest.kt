package com.netsservices.dct.data.remote.resquest

data class CreateSessionRequest(
    val purpose: String,
    val durianTypeId: Int,
    val plantationId: String?=null,
    val orchardId: String?=null,
    val siteId: String?=null,
    val mainFileId: String,
    val latitude: Double,
    val longitude: Double,
    val contractId: String?=null
)