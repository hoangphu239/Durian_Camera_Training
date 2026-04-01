package com.netsservices.dct.presentation.config.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.presentation.components.AppText

@Composable
fun DurianVarietySection(
    selectedDurianVariety: DurianItem?,
    onOpenDurianVariety: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                BorderStroke(0.8.dp, SolidColor(MaterialTheme.colorScheme.tertiary)),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(start = 20.dp, end = 10.dp, top = 15.dp, bottom = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.durian_variety),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.clickable {
                onOpenDurianVariety()
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            selectedDurianVariety?.let {
                AppText(
                    modifier = Modifier.padding(end = 5.dp),
                    text = it.name,
                    color = R.color.gray,
                    fontSize = 14.sp,
                )
            }

            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = "Location",
                modifier = Modifier.size(22.dp),
                tint = colorResource(R.color.black)
            )
        }
    }
}