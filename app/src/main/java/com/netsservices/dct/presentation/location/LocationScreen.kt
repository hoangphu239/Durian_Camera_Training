package com.netsservices.dct.presentation.location

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.presentation.config.ConfigViewModel

@Composable
fun LocationScreen(viewModel: ConfigViewModel) {

    val context = LocalContext.current
    val contract = viewModel.getContract(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(stringResource(R.string.site))
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                    append((" " + contract?.site?.name))
                }
            },
            fontSize = 15.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(stringResource(R.string.orchard))
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                    append((" " + contract?.orchard?.name))
                }
            },
            fontSize = 15.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(stringResource(R.string.plantation))
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                    append((" " + contract?.plantation?.name))
                }
            },
            fontSize = 15.sp
        )
    }
}