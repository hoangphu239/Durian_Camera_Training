package com.netsservices.dct.presentation.login

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.netsservices.dct.R
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.getText
import com.netsservices.dct.presentation.components.AppButton
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.components.AppTextField

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateForgot: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsState().value
    val keyboardController = LocalSoftwareKeyboardController.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val mapLang = viewModel.mapLang.collectAsState().value

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            AppText(
                modifier = Modifier.align(Alignment.Start),
                text = mapLang.getText(LangKey.Text.WelcomeBack,R.string.welcome_back),
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 40.sp
            )

            Spacer(Modifier.height(30.dp))

            AppTextField(
                value = email,
                onValueChange = { email = it },
                hint = mapLang.getText(LangKey.Input.Email,R.string.email),
                error = viewModel.emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(24.dp))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                hint = mapLang.getText(LangKey.Input.Password,R.string.password),
                isPassword = true,
                error = viewModel.passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                AppText(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .clickable {
                            keyboardController?.hide()
                            onNavigateForgot()
                        },
                    text = mapLang.getText(LangKey.Text.ForgotPassword,R.string.forgot_your_password),
                    color = R.color.blue,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(40.dp))

            AppButton(
                modifier = Modifier.fillMaxWidth(),
                text = mapLang.getText(LangKey.Button.Login,R.string.login),
                onClick = {
                    keyboardController?.hide()
                    viewModel.login(context, email, password)
                }
            )

            Spacer(Modifier.weight(1f))

            Row {
                AppText(
                    text = mapLang.getText(LangKey.Message.NotHaveAccount,R.string.not_have_account),
                    fontSize = 15.sp,
                )
                Spacer(modifier = Modifier.width(5.dp))
                AppText(
                    text = mapLang.getText(LangKey.Button.SignUpNow, R.string.sign_up_now),
                    color = R.color.blue,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable { onNavigateRegister() }
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
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }
}