package com.netsservices.dct.presentation.config

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.netsservices.dct.data.remote.response.ContractItem
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.response.LanguageResponse
import com.netsservices.dct.presentation.config.components.ChangePasswordSection
import com.netsservices.dct.presentation.config.components.DurianVarietySection
import com.netsservices.dct.presentation.config.components.LanguageSection
import com.netsservices.dct.presentation.config.components.LocationSection
import com.netsservices.dct.presentation.config.components.PurposeSection
import com.netsservices.dct.presentation.config.components.ScanMode
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ConfigScreen(
    activity: Activity,
    viewModel: ConfigViewModel = hiltViewModel(),
    languages: List<LanguageResponse>,
    openLocation: () -> Unit,
    openDurianVariety: () -> Unit,
    onChangePwd: () -> Unit
) {
    val selectLanguage by viewModel.selectLanguage.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedDurianVariety by viewModel.currentVariety.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val contract by viewModel.activeContract.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadData()
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LocalLayoutDirection.current,
        LocalConfiguration provides LocalConfiguration.current.apply {
            setLocale(Locale(selectLanguage))
        }
    ) {
        ConfigScreenContent(
            viewModel = viewModel,
            languages = languages,
            deviceStatus = deviceStatus,
            currentMode = currentMode ?: ScanMode.COLLECTION,
            selectedDurianVariety = selectedDurianVariety,
            selectLanguage = selectLanguage,
            contract = contract,
            onLanguageChange = { lang -> viewModel.onLanguageSelected(activity, lang) },
            onOpenLocation = { openLocation() },
            onOpenDurianVariety = { openDurianVariety() },
            onChangePwd = { onChangePwd() }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ConfigScreenContent(
    viewModel: ConfigViewModel,
    languages: List<LanguageResponse>?,
    deviceStatus: String,
    currentMode: ScanMode?,
    contract: ContractItem?,
    selectedDurianVariety: DurianItem?,
    selectLanguage: String,
    onLanguageChange: (String) -> Unit,
    onOpenLocation: () -> Unit,
    onOpenDurianVariety: () -> Unit,
    onChangePwd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp, bottom = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 5.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                PurposeSection(
                    viewModel = viewModel,
                    currentMode = currentMode
                )
            }

            if(contract != null && currentMode == ScanMode.FINGERPRINT) {
                item {
                    LocationSection(
                        selectedSite = contract.site.name,
                        onOpenLocation = onOpenLocation
                    )
                }
            }

            item {
                DurianVarietySection(
                    selectedDurianVariety = selectedDurianVariety,
                    onOpenDurianVariety = onOpenDurianVariety
                )
            }

            item {
                ChangePasswordSection(
                    onChangePwd = onChangePwd
                )
            }

            item {
                LanguageSection(
                    languages = languages,
                    selectLanguage = selectLanguage,
                    onLanguageChange = onLanguageChange
                )
            }
        }
    }
}

