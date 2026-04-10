package com.netsservices.dct.presentation.change_pwd

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.resquest.ChangePwdRequest
import com.netsservices.dct.data.remote.utils.PreferenceManager
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
class ChangePwdViewModel @Inject constructor(
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
        val isSuccess: Boolean = false
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    var passwordError by mutableStateOf<String?>(null)
        private set
    var newPasswordError by mutableStateOf<String?>(null)
        private set
    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    fun validateCredentials(
        context: Context,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        passwordError = when {
            currentPassword.isBlank() -> mapLang.value[LangKey.Message.PasswordRequired]?:
            context.getString(R.string.password_required)
            currentPassword.length < 6 -> mapLang.value[LangKey.Message.PasswordTooShort]?:
            context.getString(R.string.password_too_short)
            !currentPassword.any { it.isDigit() } -> mapLang.value[LangKey.Message.PasswordRequireNumber]?:
            context.getString(R.string.password_must_contain_at_least_1_number)
            else -> null
        }

        newPasswordError = when {
            newPassword.isBlank() -> mapLang.value[LangKey.Message.NewPasswordRequired]?:
            context.getString(R.string.new_password_required)
            newPassword.length < 6 -> mapLang.value[LangKey.Message.NewPasswordTooShort]?:
            context.getString(R.string.new_password_too_short)
            !newPassword.any { it.isDigit() } -> mapLang.value[LangKey.Message.NewPasswordRequireNumber]?:
            context.getString(R.string.new_password_must_contain_at_least_1_number)
            newPassword == currentPassword -> mapLang.value[LangKey.Message.NewPasswordNotSame]?:
            context.getString(R.string.new_password_must_be_different_from_current_password)
            else -> null
        }

        confirmPasswordError = when {
            confirmPassword.isBlank() -> mapLang.value[LangKey.Message.ConfirmPasswordRequired]?:
            context.getString(R.string.confirm_password_required)
            confirmPassword != newPassword -> mapLang.value[LangKey.Message.PasswordNotMatch]?:
            context.getString(R.string.new_password_and_confirm_new_password_do_not_match)
            else -> null
        }

        return passwordError == null &&
                newPasswordError == null &&
                confirmPasswordError == null
    }

    fun changePassword(context: Context, currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (!validateCredentials(context,currentPassword, newPassword, confirmPassword)) return@launch

            _uiState.update { it.copy(isLoading = true) }
            repo.changePassword(ChangePwdRequest(currentPassword, newPassword)).handle(
                onSuccess = { data ->
                    clearDataLocal()
                    showToast(context, data.message?:"")
                    _uiState.update { state -> state.copy(isSuccess = true) }
                },
                onError = { _, message ->
                    message?.let { showToast(context, it) }
                }
            )
            _uiState.update { state -> state.copy(isLoading = false) }
        }
    }

    private fun clearDataLocal() {
        PreferenceManager.clearData(context)
    }
}