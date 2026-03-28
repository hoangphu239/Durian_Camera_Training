package com.netsservices.dct.data.remote.resquest

data class CreateSessionRequest(
    val purpose: String,
    val specimenId: String,
    val plantationId: String,
    val orchardId: String,
    val siteId: String,
    val profileId: String,
    val fruitTag: String,
    val notes: String,
)