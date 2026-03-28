package com.netsservices.dct.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R

@Composable
fun AppText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Start,
    color: Int = R.color.black,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        modifier = modifier,
        textAlign = textAlign,
        text = text,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = colorResource(id = color)
    )
}