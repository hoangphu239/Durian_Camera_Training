package com.netsservices.dct.data.remote.resquest

data class FingerPrintRequest(
    val mainFileId: String = "",
    val capturedAt: String = "",
    val deviceId: String = "",
    val deviceLabel: String = "",
    val scanPosition: String = "",
    val measurementTarget: String = "",
    val markerProfileId: String = "",
    val markerProfileCode: String = "",
    val plantationId: String = "",
    val orchardId: String = "",
    val siteId: String = "",
    val fruitTag: String = "",
    val operatorUserId: String = "",
    val appVersion: String = "",
    val cameraModel: String = "",
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val distanceMm: Float = 0f,
    val qualityScore: Float = 0f,
    val qualityChecks: QualityChecks? = null,
    val notes: String = ""
)

data class QualityChecks(
    val allMarkersVisible: Boolean = true,
    val sharpEnough: Boolean = true,
    val correctScale: Boolean = true,
    val sufficientLighting: Boolean = true,
    val enoughDurianAreaVisible: Boolean = true
)

enum class ScanPosition {
    SPOT_1, SPOT_2, SPOT_3
}

enum class MeasurementTarget {
    WHOLE_FRUIT, PARTIAL
}