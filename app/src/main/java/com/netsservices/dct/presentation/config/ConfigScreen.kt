package com.netsservices.dct.presentation.config

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.netsservices.dct.presentation.config.components.LanguageSection
import com.netsservices.dct.presentation.config.components.LocationSection
import java.util.Locale


@Composable
fun ConfigScreen(
    activity: Activity,
    viewModel: ConfigViewModel = hiltViewModel(),
    onOpenLocation: () -> Unit
) {
    val language by viewModel.language.collectAsState()

    CompositionLocalProvider(
        LocalLayoutDirection provides LocalLayoutDirection.current,
        LocalConfiguration provides LocalConfiguration.current.apply {
            setLocale(Locale(language))
        }
    ) {
        ConfigScreenContent(
            language = language,
            onLanguageChange = { lang ->
                viewModel.onLanguageSelected(activity, lang)
            },
            onOpenLocation = { onOpenLocation() }
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ConfigScreenContent(
    language: String,
    onLanguageChange: (String) -> Unit,
    onOpenLocation: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LanguageSection(
                language = language,
                onLanguageChange = onLanguageChange
            )
            LocationSection(
                onOpenLocation = onOpenLocation
            )
        }
    }
}

