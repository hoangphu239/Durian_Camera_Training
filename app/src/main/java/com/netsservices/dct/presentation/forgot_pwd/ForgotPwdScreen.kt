package com.netsservices.dct.presentation.forgot_pwd

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.LoadingOverlay
import com.netsservices.dct.presentation.common.getText
import com.netsservices.dct.presentation.components.AppTextField

@Composable
fun ForgotPwdScreen(
    viewModel: ForgotPwdViewModel = hiltViewModel(),
    onNavigateLogin: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    val uiState = viewModel.uiState.collectAsState().value
    val keyboardController = LocalSoftwareKeyboardController.current
    val mapLang = viewModel.mapLang.collectAsState().value


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(Modifier.height(50.dp))

        Text(
            text = stringResource(R.string.forgot_your_password),
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            lineHeight = 40.sp
        )

        Spacer(Modifier.height(30.dp))

        AppTextField(
            value = email,
            onValueChange = { email = it },
            hint = mapLang.getText(LangKey.Input.NewPassword, R.string.new_password),
            error = viewModel.passwordError,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        )

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                keyboardController?.hide()
                viewModel.forgotPassword(context, email)
            }) {
            Text(mapLang.getText(LangKey.Button.Send, R.string.send))
        }
    }


    if (uiState.isLoading) {
        LoadingOverlay()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateLogin()
        }
    }
}