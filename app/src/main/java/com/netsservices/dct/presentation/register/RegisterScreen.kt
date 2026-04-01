package com.netsservices.dct.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState = viewModel.uiState.collectAsState().value
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(Modifier.height(50.dp))

            Text(
                text = stringResource(R.string.create_account_to_get_started_now),
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 40.sp
            )

            Spacer(Modifier.height(30.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                hint = stringResource(R.string.email),
                error = viewModel.emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(16.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                hint = stringResource(R.string.password),
                isPassword = true,
                error = viewModel.passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )

            Spacer(Modifier.height(40.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    keyboardController?.hide()
                    viewModel.register(email, password)
                }) {
                Text(stringResource(R.string.sign_up))
            }

            Spacer(Modifier.weight(1f))

            Row {
                Text(stringResource(R.string.already_have_an_account))
                Text(
                    text = stringResource(R.string.login_now),
                    color = Color.Blue,
                    modifier = Modifier.clickable { onNavigateLogin() }
                )
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
            onNavigateLogin()
        }
    }
}