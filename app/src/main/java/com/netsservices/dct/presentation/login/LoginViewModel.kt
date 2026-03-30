package com.netsservices.dct.presentation.login

import android.annotation.SuppressLint
import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.LoginResponse
import com.netsservices.dct.data.remote.resquest.LoginRequest
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val data: LoginResponse? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set

    fun validateCredentials(email: String, password: String): Boolean {
        emailError = when {
            email.isBlank() -> context.getString(R.string.email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() -> context.getString(R.string.invalid_email_format)

            else -> null
        }

        passwordError = when {
            password.isBlank() -> context.getString(R.string.password_required)
            password.length < 6 -> context.getString(R.string.password_too_short)
            !password.any { it.isDigit() } -> context.getString(R.string.password_must_contain_at_least_1_number)
            else -> null
        }

        return emailError == null && passwordError == null
    }

    @SuppressLint("LogNotTimber")
    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (!validateCredentials(email, password)) return@launch

            _uiState.update { state -> state.copy(isLoading = true) }
            repo.login(LoginRequest(email, password)).handle(
                onSuccess = { data ->
                    saveDataToLocal(data.token, data.user.id)
                    _uiState.update { it.copy(isSuccess = true) }
                }
            )
            _uiState.update { state -> state.copy(isLoading = false) }
        }
    }

    private fun saveDataToLocal(token: String, userId: String) {
        PreferenceManager.saveAuthToken(context, token)
        PreferenceManager.saveUserId(context, userId)
    }
}