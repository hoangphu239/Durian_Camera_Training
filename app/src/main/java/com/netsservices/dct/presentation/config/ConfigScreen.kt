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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.presentation.config.components.ChangePasswordSection
import com.netsservices.dct.presentation.config.components.DurianVarietySection
import com.netsservices.dct.presentation.config.components.LanguageSection
import com.netsservices.dct.presentation.config.components.LocationSection
import com.netsservices.dct.presentation.config.components.PurposeSection
import com.netsservices.dct.presentation.config.components.ScanMode
import java.util.Locale


@Composable
fun ConfigScreen(
    activity: Activity,
    viewModel: ConfigViewModel = hiltViewModel(),
    openLocation: () -> Unit,
    openDurianVariety: () -> Unit,
    onChangePwd: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedSite by viewModel.currentSite.collectAsState()
    val selectedDurianVariety by viewModel.currentVariety.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadSite()
            viewModel.loadDurianVariety()
            viewModel.loadScanMode()
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LocalLayoutDirection.current,
        LocalConfiguration provides LocalConfiguration.current.apply {
            setLocale(Locale(language))
        }
    ) {
        ConfigScreenContent(
            viewModel = viewModel,
            language = language,
            selectedSite = selectedSite,
            selectedDurianVariety = selectedDurianVariety,
            currentMode = currentMode ?: ScanMode.FINGERPRINT,
            onLanguageChange = { lang ->
                viewModel.onLanguageSelected(activity, lang)
            },
            onOpenLocation = { openLocation() },
            onOpenDurianVariety = { openDurianVariety() },
            onChangePwd = { onChangePwd() }
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ConfigScreenContent(
    viewModel: ConfigViewModel,
    language: String,
    selectedSite: Site?,
    selectedDurianVariety: DurianItem?,
    currentMode: ScanMode,
    onLanguageChange: (String) -> Unit,
    onOpenLocation: () -> Unit,
    onOpenDurianVariety: () -> Unit,
    onChangePwd: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp, bottom = 16.dp)
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
                selectedSite = selectedSite,
                onOpenLocation = onOpenLocation
            )
            PurposeSection(
                viewModel = viewModel,
                currentMode = currentMode,
                onSelected = { mode ->
                    viewModel.saveScanMode(mode)
                }
            )
            DurianVarietySection(
                selectedDurianVariety = selectedDurianVariety,
                onOpenDurianVariety = onOpenDurianVariety
            )
            ChangePasswordSection(
                onChangePwd = onChangePwd
            )
        }
    }
}

