package com.netsservices.dct.presentation.forgot_pwd

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.resquest.ForgotPwdRequest
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.utils.Utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPwdViewModel @Inject constructor(
    private val repo: Repository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    var passwordError by mutableStateOf<String?>(null)
        private set

    fun validateCredentials(
        context: Context,
        newPassword: String
    ): Boolean {
        passwordError = when {
            newPassword.isBlank() -> context.getString(R.string.password_required)
            newPassword.length < 6 -> context.getString(R.string.password_too_short)
            else -> null
        }

        return passwordError == null
    }

    fun forgotPassword(context: Context, newPassword: String) {
        viewModelScope.launch {
            if (!validateCredentials(context, newPassword)) return@launch

            _uiState.update { it.copy(isLoading = true) }
            repo.forgotPassword(ForgotPwdRequest(newPassword)).handle(
                onSuccess = { data ->
                    data.message?.let { showToast(context, it) }
                    _uiState.update { state -> state.copy(isLoading = false, isSuccess = true) }
                },
                onError = { _, message ->
                    message?.let { showToast(context, it) }
                    _uiState.update { state -> state.copy(isLoading = false) }
                }
            )
        }
    }
}