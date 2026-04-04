package com.netsservices.dct.presentation.config.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.presentation.config.ConfigViewModel


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ModeSelector(
    viewModel: ConfigViewModel,
    currentMode: ScanMode,
    onSelected: (ScanMode) -> Unit,
    itemPadding: Dp = 12.dp,
    textSize: TextUnit = 14.sp
) {
    val context = LocalContext.current
    val verifiedContract = viewModel.verifiedContract.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value

    Box(contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ){
            ModeItem(
                title = stringResource(R.string.fringerprint),
                selected = currentMode == ScanMode.FINGERPRINT,
                modifier = Modifier.weight(1f),
                padding = itemPadding,
                textSize = textSize,
                onClick = {
                    if(viewModel.getContract(context) == null) {
                        viewModel.verifyContract(context,"") {
                            onSelected(ScanMode.FINGERPRINT)
                        }
                    } else {
                        onSelected(ScanMode.FINGERPRINT)
                    }
                }
            )

            ModeItem(
                title = stringResource(R.string.collection),
                selected = currentMode == ScanMode.COLLECTION,
                modifier = Modifier.weight(1f),
                padding = itemPadding,
                textSize = textSize,
                onClick = { onSelected(ScanMode.COLLECTION) }
            )
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
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
