package com.netsservices.dct.presentation.config

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.ContractItem
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.ContractStatus
import com.netsservices.dct.presentation.common.LanguagePrefs
import com.netsservices.dct.presentation.config.components.ScanMode
import com.netsservices.dct.presentation.utils.Utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ConfigViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val verifiedContract: Boolean = false,
    )
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _deviceStatus = MutableStateFlow(PreferenceManager.getDeviceStatus(context))
    val deviceStatus = _deviceStatus.asStateFlow()
    private val _currentMode = MutableStateFlow<ScanMode?>(null)
    val currentMode: StateFlow<ScanMode?> = _currentMode
    private val _currentVariety = MutableStateFlow<DurianItem?>(null)
    val currentVariety: StateFlow<DurianItem?> = _currentVariety
    private val _activeContract = MutableStateFlow<ContractItem?>(null)
    val activeContract: StateFlow<ContractItem?> = _activeContract
    private val _selectLanguage = MutableStateFlow("en")
    val selectLanguage = _selectLanguage.asStateFlow()

    init {
        observeLanguage()
        _currentMode.value = PreferenceManager.getScanMode(context)
    }

    fun loadData() {
        _currentVariety.value = PreferenceManager.getDurianVariety(context)
        _activeContract.value = PreferenceManager.getActiveContract(context)
    }

    fun verifyContract(context: Context, search: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repo.getContracts(search, ContractStatus.ACTIVE.value).handle(
                onSuccess = { data ->
                    _uiState.update { it.copy(isLoading = false) }
                    val result = findActiveContract(data.items)
                    if(result!=null) {
                        _uiState.update { it.copy(verifiedContract = true) }
                        saveContract(result)
                        _activeContract.value = getContract(context)
                        onSuccess()
                    } else {
                        _uiState.update { it.copy(verifiedContract = false) }
                        showToast(context, context.getString(R.string.required_to_sign_a_contract))
                    }
                },
                onError = { _, _ ->
                    _uiState.update { it.copy(isLoading = false, verifiedContract = false) }
                }
            )
        }
    }

    private fun saveContract(contract: ContractItem) {
        PreferenceManager.saveActiveContract(context, contract)
    }

    fun getContract(context: Context): ContractItem? {
        return PreferenceManager.getActiveContract(context)
    }

    private fun findActiveContract(items: List<ContractItem>): ContractItem?
            = items.find { it.status == ContractStatus.ACTIVE.value }


    private fun observeLanguage() {
        viewModelScope.launch {
            LanguagePrefs.getLanguage(context).collect {
                _selectLanguage.value = it
            }
        }
    }

    fun onLanguageSelected(activity: Activity, lang: String) {
        viewModelScope.launch {
            LanguagePrefs.setLanguage(activity, lang)
            activity.recreate()
        }
    }

    fun saveScanMode(scanMode: ScanMode) {
        PreferenceManager.saveScanMode(context, scanMode)
        _currentMode.value = scanMode
    }

    fun updateAction(action: String) {
        PreferenceManager.saveAction(context, action)
    }
}