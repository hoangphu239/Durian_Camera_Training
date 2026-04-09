package com.netsservices.dct.presentation.config.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.getText
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.config.ConfigViewModel

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun PurposeSection(
    viewModel: ConfigViewModel,
    currentMode: ScanMode
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
            .padding(start = 20.dp, end = 10.dp, top = 15.dp, bottom = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            AppText(
                modifier = Modifier.padding(top = 7.dp),
                text = mapLang.getText(LangKey.Text.ScanMode, R.string.scan_mode),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(10.dp))

        ModeSelector(
            viewModel = viewModel,
            itemPadding = 6.dp,
            textSize = 13.sp,
            currentMode = currentMode,
            onSelected = { selectedMode ->
                if(currentMode != selectedMode) {
                    viewModel.saveScanMode(selectedMode)
                }
            }
        )
    }
}