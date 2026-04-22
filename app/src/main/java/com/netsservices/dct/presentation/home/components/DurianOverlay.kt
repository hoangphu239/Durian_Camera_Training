package com.netsservices.dct.presentation.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.presentation.components.AppText


@Composable
fun DurianOverlay(
    guidance: String
) {
    val displayText =
        guidance.ifBlank { stringResource(R.string.durian_within_the_frame) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ===== MASK + OVAL =====
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
        ) {
            val overlayColor = Color.Black.copy(alpha = 0.65f)

            drawRect(overlayColor)

            val ovalWidth = size.width * 0.8f
            val ovalHeight = size.height * 0.65f

            val left = (size.width - ovalWidth) / 2
            val top = (size.height - ovalHeight) / 2

            val ovalRect = Rect(
                offset = Offset(left, top),
                size = Size(ovalWidth, ovalHeight)
            )

            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    blendMode = BlendMode.Clear
                }
                canvas.drawOval(ovalRect, paint)
            }

            // viền oval
            drawOval(
                color = Color.White,
                topLeft = ovalRect.topLeft,
                size = ovalRect.size,
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(20f, 10f)
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.position_the_durian_inside_the_frame),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            AnimatedContent(targetState = displayText) { text ->
                Text(
                    text,
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            AppText(
                text= stringResource(R.string.keep_steady_and_ensure_good_lighting),
                color = R.color.white,
                fontSize = 13.sp
            )
        }
    }
}