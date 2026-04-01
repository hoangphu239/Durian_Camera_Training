package com.netsservices.dct.domain.model


data class Meta(
    val brand: String,
    val cameraDetails: CameraDetail
)
data class CameraDetail(
    val facing: String = "",
    val type: String = "",
    val focalLengthMm: Double = 0.0,
    val sensor: SensorInfo? = null
)

data class SensorInfo(
    val widthMm: Double = 0.0,
    val heightMm: Double = 0.0
)
