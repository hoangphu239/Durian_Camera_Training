package com.netsservices.dct.data.remote.response

data class DeviceResponse(
    val deviceId: String,
    val status: String,
    val macAddress: String,
    val deviceCode: String?,
    val name: String?,
    val createdAt: String,
    val message: String
)
