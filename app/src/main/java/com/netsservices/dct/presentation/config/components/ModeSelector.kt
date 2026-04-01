package com.netsservices.dct.presentation.config.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R


@Composable
fun ModeSelector(
    modifier: Modifier = Modifier,
    selectedMode: ScanMode,
    onSelected: (ScanMode) -> Unit,
    itemPadding: Dp = 12.dp,
    textSize: TextUnit = 14.sp
) {
    Row(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {

        ModeItem(
            title = stringResource(R.string.fringerprint),
            selected = selectedMode == ScanMode.FINGERPRINT,
            onClick = { onSelected(ScanMode.FINGERPRINT) },
            modifier = Modifier.weight(1f),
            padding = itemPadding,
            textSize = textSize
        )

        ModeItem(
            title = stringResource(R.string.collection),
            selected = selectedMode == ScanMode.COLLECTION,
            onClick = { onSelected(ScanMode.COLLECTION) },
            modifier = Modifier.weight(1f),
            padding = itemPadding,
            textSize = textSize
        )
    }
}

@Composable
fun ModeItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    padding: Dp = 12.dp,
    textSize: TextUnit = 14.sp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = textSize,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.Black else Color.Gray
        )
    }
}
