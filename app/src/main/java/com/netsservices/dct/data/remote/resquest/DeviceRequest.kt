package com.netsservices.dct.data.remote.resquest

import com.netsservices.dct.domain.model.Meta

data class DeviceRequest(
    val macAddress: String,
    val deviceCode: String,
    val name: String,
    val meta: Meta
)
