package com.netsservices.dct.presentation.config.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.DeviceStatus
import com.netsservices.dct.presentation.common.getText
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.config.ConfigViewModel


@Composable
fun RegisterDeviceSection(
    viewModel: ConfigViewModel,
    status: String
) {
    val mapLang = viewModel.mapLang.collectAsState().value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                BorderStroke(0.8.dp, SolidColor(MaterialTheme.colorScheme.tertiary)),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(start = 20.dp, end = 15.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(
            text = mapLang.getText(LangKey.Text.MyPhone, R.string.my_phone),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
        when(status) {
            DeviceStatus.UNACTIVE.value -> {
                ItemStatus(
                    color = Color(0xFF444444),
                    text = mapLang.getText(LangKey.Text.Inactive, R.string.inactive),
                )
            }
            DeviceStatus.PENDING_APPROVAL.value -> {
                ItemStatus(
                    color = Color(0xFFFF9800),
                    text = mapLang.getText(LangKey.Text.PendingReview, R.string.pending_review),
                )
            }
            DeviceStatus.ACTIVATE.value -> {
                ItemStatus(
                    color = Color(0xFF4CAF50),
                    text = mapLang.getText(LangKey.Text.Active, R.string.activated),
                )
            }
        }
    }
}

@Composable
fun ItemStatus(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(16.dp)
                .padding(end = 5.dp)
        )
        AppText(
            text = text,
            color = R.color.gray
        )
    }
}