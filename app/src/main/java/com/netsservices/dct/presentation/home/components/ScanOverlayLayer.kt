package com.netsservices.dct.presentation.home.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.presentation.common.Constants
import com.netsservices.dct.presentation.common.Constants.VIBRATION_PATTERN
import com.netsservices.dct.presentation.config.components.ScanMode
import com.netsservices.dct.presentation.home.HomeViewModel
import com.netsservices.dct.presentation.utils.Utils
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@RequiresApi(Build.VERSION_CODES.P)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ScanOverlayLayer(
    viewModel: HomeViewModel,
    context: Context,
    isDetected: Boolean
) {
    val laserPoints = viewModel.laserPoints
    val imageSize = viewModel.imageSize
    val mode = PreferenceManager.getScanMode(context)

    var lastMatched by remember { mutableStateOf(false) }
    var lastDetected by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val offsetY by infiniteTransition.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = LinearEasing)
        ),
        label = "scan_line_anim"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val ovalWidth = width * 0.8f
        val ovalHeight = height * 0.6f

        val left = (width - ovalWidth) / 2
        val top = (height - ovalHeight) / 2

        val ovalRect = Rect(
            offset = Offset(left, top),
            size = Size(ovalWidth, ovalHeight)
        )

        val mappedPoints = remember(laserPoints, imageSize, width, height) {
            laserPoints.map {
                Utils.mapPreviewToBitmap(
                    it,
                    imageSize.width.toFloat(),
                    imageSize.height.toFloat(),
                    width,
                    height
                )
            }
        }

        val triangle = remember(width, height) {
            Utils.createTriangle(width, height)
        }

        val tolerance = triangle.side * Constants.TOLERANCE_RATIO
        val radius = triangle.side * Constants.CIRCLE_RADIUS_RATIO

        val matched by remember(mode, mappedPoints) {
            derivedStateOf {
                mode == ScanMode.FINGERPRINT &&
                        mappedPoints.size == 3 &&
                        matchFingerprint(
                            mappedPoints,
                            triangle.targets,
                            tolerance
                        )
            }
        }

        if (mode == ScanMode.FINGERPRINT) {
            val color = if (matched) Color.Green else Color.Red
            FingerprintOverlay(
                color = color,
                radius = radius,
                targets = triangle.targets
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    shape = object : Shape {
                        override fun createOutline(
                            size: Size,
                            layoutDirection: LayoutDirection,
                            density: Density
                        ): Outline {
                            return Outline.Generic(
                                Path().apply {
                                    addOval(ovalRect)
                                }
                            )
                        }
                    }
                    clip = true
                }
        ) {
            ScanLine(
                progress = offsetY,
                topOffset = top,
                heightPx = ovalHeight
            )
        }

        // ===== MATCH =====
        LaunchedEffect(matched) {
            if (mode == ScanMode.FINGERPRINT) {
                if (matched && !lastMatched) {
                    viewModel.onLaserMatched()
                }
                lastMatched = matched
            }
        }

        // ===== VIBRATE =====
        LaunchedEffect(isDetected) {
            if (isDetected && !lastDetected) {
                vibrate(context)
            }
            lastDetected = isDetected
        }
    }
}


private fun matchFingerprint(
    points: List<Offset>,
    targets: List<Offset>,
    tolerance: Float
): Boolean {

    if (points.size != 3) return false

    val perms = listOf(
        listOf(0,1,2), listOf(0,2,1),
        listOf(1,0,2), listOf(1,2,0),
        listOf(2,0,1), listOf(2,1,0)
    )

    return perms.any { perm ->
        perm.indices.all { i ->
            val p = points[perm[i]]
            val c = targets[i]

            val dx = p.x - c.x
            val dy = p.y - c.y

            dx * dx + dy * dy < tolerance * tolerance
        }
    }
}

private fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (!vibrator.hasVibrator()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createWaveform(
                VIBRATION_PATTERN,
                -1
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(150)
    }
}