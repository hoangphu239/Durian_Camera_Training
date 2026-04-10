package com.netsservices.dct.presentation.register

import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.RegisterResponse
import com.netsservices.dct.data.remote.resquest.RegisterRequest
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.LanguagePrefs
import com.netsservices.dct.presentation.utils.Utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository
) : ViewModel() {

    val mapLang = LanguagePrefs.getTranslations(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    data class UiState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val data: RegisterResponse? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set

    fun validateCredentials(context: Context, email: String, password: String): Boolean {
        emailError = when {
            email.isBlank() ->  mapLang.value[LangKey.Message.EmailRequired]?:
                context.getString(R.string.email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() ->  mapLang.value[LangKey.Message.InvalidEmailFormat]?:
                    context.getString(R.string.invalid_email_format)

            else -> null
        }
        passwordError = when {
            password.isBlank() ->  mapLang.value[LangKey.Message.PasswordRequired]?:
                context.getString(R.string.password_required)
            password.length < 10 ->  mapLang.value[LangKey.Message.PasswordMinLength]?:
                context.getString(R.string.password_require_least_10_chars)
            !password.any { it.isDigit() } -> mapLang.value[LangKey.Message.PasswordRequireNumber]?:
                context.getString(R.string.password_must_contain_at_least_1_number)
            else -> null
        }

        return emailError == null && passwordError == null
    }

    fun register(context: Context, email: String, password: String) {
        viewModelScope.launch {
            if (!validateCredentials(context, email, password)) return@launch
            _uiState.update { state -> state.copy(isLoading = true) }
            repo.register(RegisterRequest(email, password)).handle(
                onSuccess = { _ ->
                    _uiState.update { state -> state.copy(isSuccess = true) }
                },
                onError = { _, message ->
                    message?.let { showToast(context, it) }
                }
            )
            _uiState.update { state -> state.copy(isLoading = false) }
        }
    }
}