package com.netsservices.dct.presentation.config.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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

enum class ScanMode {
    FINGERPRINT,
    COLLECTION
}

@Composable
fun ModeSelectionDialog(
    currentMode: ScanMode?,
    onConfirm: (ScanMode) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode ?: ScanMode.FINGERPRINT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Mode") },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.choose_scan_mode),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                ModeSelector(
                    modifier = Modifier.fillMaxWidth(),
                    selectedMode = selectedMode,
                    onSelected = { selectedMode = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMode) }) {
                Text(stringResource(R.string.confirm))
            }
        }
    )
}
