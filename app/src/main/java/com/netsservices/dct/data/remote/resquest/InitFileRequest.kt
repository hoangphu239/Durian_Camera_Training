package com.netsservices.dct.data.remote.resquest

data class InitFileRequest(
    val purpose: String,
    val filename: String,
    val contentType: String,
    val sizeBytes: Int,
    val sha256: String
)