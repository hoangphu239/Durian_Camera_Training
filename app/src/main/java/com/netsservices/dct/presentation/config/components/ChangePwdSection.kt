package com.netsservices.dct.presentation.config.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.netsservices.dct.R

@Composable
fun ChangePasswordSection(
    onChangePwd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                BorderStroke(0.8.dp, SolidColor(MaterialTheme.colorScheme.tertiary)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(start = 20.dp, end = 5.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.change_password),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = { onChangePwd() }) {
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = "Settings",
                modifier = Modifier.size(22.dp),
                tint = colorResource(R.color.black)
            )
        }
    }
}