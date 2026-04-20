package com.netsservices.dct.domain.model

import androidx.compose.ui.geometry.Offset

data class Triangle(
    val targets: List<Offset>,
    val side: Float
)
