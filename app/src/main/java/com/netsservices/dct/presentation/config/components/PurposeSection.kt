package com.netsservices.dct.presentation.config.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.presentation.components.AppText

@Composable
fun PurposeSection(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
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
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = stringResource(R.string.fringerprint),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(3.dp))
            if(!isEnabled) {
                AppText(
                    text = stringResource(R.string.collection_mode_is_now_enabled),
                    fontSize = 12.sp,
                    color = R.color.blue
                )
            }
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle(it) }
        )
    }
}