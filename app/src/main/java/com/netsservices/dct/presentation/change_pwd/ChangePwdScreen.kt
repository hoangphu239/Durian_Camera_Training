package com.netsservices.dct.presentation.change_pwd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.netsservices.dct.R
import com.netsservices.dct.presentation.components.AppTextField


@Composable
fun ChangePwdScreen(
    viewModel: ChangePwdViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsState().value
    val keyboardController = LocalSoftwareKeyboardController.current

    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(50.dp))

        Text(
            text = stringResource(R.string.update_your_password),
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 40.sp
        )

        Spacer(Modifier.height(30.dp))

        AppTextField(
            value = current,
            onValueChange = { current = it },
            hint = stringResource(R.string.current_password),
            isPassword = true,
            error = viewModel.passwordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        )

        Spacer(Modifier.height(16.dp))

        AppTextField(
            value = newPass,
            onValueChange = { newPass = it },
            hint = stringResource(R.string.new_password),
            isPassword = true,
            keyboardType = KeyboardType.Password,
            error = viewModel.newPasswordError,
            imeAction = ImeAction.Next
        )

        Spacer(Modifier.height(16.dp))

        AppTextField(
            value = confirm,
            onValueChange = { confirm = it },
            hint = stringResource(R.string.confirm_new_password),
            isPassword = true,
            error = viewModel.confirmPasswordError,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        )

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                keyboardController?.hide()
                viewModel.changePassword(context,current, newPass, confirm)
            }) {
            Text(stringResource(R.string.change_password))
        }
    }


    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (uiState.isSuccess) {
        navigateToLogin()
    }
}