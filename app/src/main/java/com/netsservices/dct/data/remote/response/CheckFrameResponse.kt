package com.netsservices.dct.data.remote.response

data class CheckFrameResponse(
    val ready: Boolean,
    val durianDetected: Boolean,
    val durianConfidence: Float,
    val checks: Checks,
    val guidance: String
)

data class Checks(
    val sufficientLighting: Boolean,
    val meanBrightness: Float,
    val sharpEnough: Boolean,
    val blurScore: Float,
    val marksDetected: Boolean,
    val markCount: Int,
    val correctScale: Boolean,
    val triangleSidePx: Float,
    val bottomAligned: Boolean,
    val bottomTiltDeg: Float,
    val apexCenteredX: Boolean,
    val apexNearTop: Boolean,
    val apexXFraction: Float,
    val apexYFraction: Float,
    val equilateralDeviation: Float,
    val equilateralValid: Boolean
)
