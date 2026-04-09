package com.netsservices.dct.presentation.home.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netsservices.dct.R
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.home.HomeViewModel

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun RequestActivationView(
    viewmodel: HomeViewModel
) {
    val uiState = viewmodel.uiState.collectAsState().value
    val deviceId = viewmodel.getDeviceId()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppText(
            text = uiState.message.ifEmpty { stringResource(R.string.waiting_for_admin_approval_device) },
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.enableActivation,
            onClick = {
                viewmodel.requestActivation(deviceId)
            }) {
            Text(stringResource(R.string.request_activation))
        }
    }
}