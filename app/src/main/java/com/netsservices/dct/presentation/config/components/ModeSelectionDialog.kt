package com.netsservices.dct.presentation.config.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.ConfigStep
import com.netsservices.dct.presentation.common.getText
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.config.ConfigViewModel

enum class ScanMode {
    FINGERPRINT,
    COLLECTION
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun ModeSelectionDialog(
    viewModel: ConfigViewModel,
    currentMode: ScanMode?,
    onConfirm: (ScanMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode ?: ScanMode.COLLECTION) }
    val mapLang = viewModel.mapLang.collectAsState().value

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            AppText(
                text = mapLang.getText(LangKey.Text.SelectMode, R.string.select_mode),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                AppText(
                    text = mapLang.getText(LangKey.Text.ChooseScanMode, R.string.choose_scan_mode)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ModeSelector(
                    viewModel = viewModel,
                    currentMode = selectedMode,
                    onSelected = { selectedMode = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateAction(ConfigStep.MODE.name)
                onConfirm(selectedMode) }
            ) {
                AppText(
                    text = mapLang.getText(LangKey.Button.Confirm, R.string.confirm),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    )
}
