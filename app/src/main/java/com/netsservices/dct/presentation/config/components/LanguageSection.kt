package com.netsservices.dct.presentation.config.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.LanguageResponse
import com.netsservices.dct.presentation.common.LanguagePrefs
import com.netsservices.dct.presentation.components.AppText


@Composable
fun LanguageSection(
    languages: List<LanguageResponse>?,
    selectLanguage: String,
    onLanguageChange: (String) -> Unit,
) {
    val context = LocalContext.current

    if (languages.isNullOrEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                BorderStroke(0.8.dp, SolidColor(MaterialTheme.colorScheme.tertiary)),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(start = 20.dp, end = 20.dp, top = 16.dp)
    ) {

        Text(
            text = stringResource(R.string.select_language),
            style = MaterialTheme.typography.titleMedium
        )

        languages.forEach { language ->
            LanguageOption(
                title = language.label,
                selected = selectLanguage == language.id
            ) {
                val currentLang = LanguagePrefs.getLanguageNow(context)
                if (currentLang == language.id) return@LanguageOption
                onLanguageChange(language.id)
            }
        }
    }
}

@Composable
fun LanguageOption(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable {
                onSelect()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        AppText(
            text = title,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}