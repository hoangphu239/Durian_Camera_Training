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
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.getText
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.config.ConfigViewModel

@Composable
fun ChangePasswordSection(
    viewModel: ConfigViewModel,
    onChangePwd: () -> Unit
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
            .padding(start = 20.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(
            text = mapLang.getText(LangKey.Button.ChangePassword, R.string.change_password),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
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