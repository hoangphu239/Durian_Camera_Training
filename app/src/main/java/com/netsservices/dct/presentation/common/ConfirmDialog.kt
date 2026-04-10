package com.netsservices.dct.presentation.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.netsservices.dct.R

@Composable
fun ConfirmDialog(
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(stringResource(R.string.confirm))
        },
        text = {
            Text(stringResource(R.string.would_you_like_to_create_a_new_session))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.accept))
            }
        }
    )
}