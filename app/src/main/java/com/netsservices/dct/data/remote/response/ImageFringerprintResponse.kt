package com.netsservices.dct.data.remote.response

data class FringerPrintResponse(
    val success: Boolean,
    val sessionId: String,
    val measurementId: String,
    val jobId: String,
    val jobKind: String,
    val jobStatus: String
)
