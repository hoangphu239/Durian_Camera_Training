package com.netsservices.dct.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun ScanLine(
    progress: Float,
    heightPx: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .offset {
                IntOffset(0, (progress * heightPx).toInt())
            }
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x55FF3B3B),
                        Color(0xFFFF3B3B),
                        Color.White,
                        Color(0xFFFF3B3B),
                        Color(0x55FF3B3B),
                        Color.Transparent
                    )
                )
            )
    )
}