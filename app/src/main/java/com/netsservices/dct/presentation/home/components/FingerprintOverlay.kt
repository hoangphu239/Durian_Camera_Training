package com.netsservices.dct.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.netsservices.dct.presentation.common.Constants

@Composable
fun FingerprintOverlay(
    color: Color,
    radius: Float,
    targets: List<Offset>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {

        targets.forEach {
            drawCircle(
                color = color,
                radius = radius,
                center = it,
                style = Stroke(Constants.STROKE_WIDTH)
            )
        }
    }
}