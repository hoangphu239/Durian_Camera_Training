package com.netsservices.dct.domain.model

import com.netsservices.dct.data.remote.resquest.DeviceRequest
import com.netsservices.dct.presentation.common.toMacAddress

data class DeviceInfo(
    val deviceId: String,
    val deviceCode: String,
    val name: String,
    val meta: Meta
)

fun DeviceInfo.toRegisterDeviceRequest(): DeviceRequest {
    return DeviceRequest(
        macAddress = this.deviceId.toMacAddress(),
        deviceCode = this.deviceCode,
        name = this.name,
        meta = this.meta.toRegisterMeta()
    )
}

fun Meta.toRegisterMeta(): Meta {
    return Meta(
        brand = this.brand,
        cameraDetails = this.cameraDetails.toRegisterCameraDetail()
    )
}

fun CameraDetail.toRegisterCameraDetail(): CameraDetail {
    return CameraDetail(
        facing = this.facing,
        type = this.type,
        focalLengthMm = this.focalLengthMm,
        sensor = this.sensor
    )
}