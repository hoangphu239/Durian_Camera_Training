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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.presentation.common.ConfigStep
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_mode)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.choose_scan_mode),
                    fontSize = 14.sp
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
                Text(stringResource(R.string.confirm))
            }
        }
    )
}
