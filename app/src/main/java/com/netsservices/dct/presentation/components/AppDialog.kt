package com.netsservices.dct.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.netsservices.dct.R

@Composable
fun AppDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    confirmText: String = stringResource(R.string.accept),
    onDismiss: (() -> Unit)? = null,
    dismissText: String = stringResource(R.string.cancel),
    cancelable: Boolean = false
) {
    AlertDialog(
        onDismissRequest = {
            if (cancelable) {
                onDismiss?.invoke()
            }
        },
        title = {
            AppText(text = title)
        },
        text = {
            AppText(text = message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                AppText(text = confirmText)
            }
        },
        dismissButton = {
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    AppText(text = dismissText)
                }
            }
        }
    )
}