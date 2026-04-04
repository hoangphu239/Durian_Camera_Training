package com.netsservices.dct.presentation.config

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.ContractItem
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.response.Site
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
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ConfigViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository
) : ViewModel() {
    private val _deviceStatus = MutableStateFlow(PreferenceManager.getDeviceStatus(context))
    val deviceStatus = _deviceStatus.asStateFlow()
    private val _currentMode = MutableStateFlow<ScanMode?>(null)
    val currentMode: StateFlow<ScanMode?> = _currentMode
    private val _verifiedContract = MutableStateFlow(false)
    val verifiedContract = _verifiedContract.asStateFlow()
    private val _currentVariety = MutableStateFlow<DurianItem?>(null)
    val currentVariety: StateFlow<DurianItem?> = _currentVariety
    private val _language = MutableStateFlow("en")
    val language = _language.asStateFlow()
    private val _currentSite = MutableStateFlow<Site?>(null)
    val currentSite: StateFlow<Site?> = _currentSite
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


    init {
        observeLanguage()
    }

    fun loadData() {
        _currentMode.value = PreferenceManager.getScanMode(context)
        _currentVariety.value = PreferenceManager.getDurianVariety(context)
//        _currentSite.value = PreferenceManager.getSite(context)
    }

    fun verifyContract(context: Context, search: String, onSuccess: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            repo.getContracts(search, ContractStatus.ACTIVE.value).handle(
                onSuccess = { data ->
                    _isLoading.value = false
                    val result = findActiveContract(data.items)
                    if(result!=null) {
                        _verifiedContract.value = true
                        saveContract(result)
                        onSuccess()
                    } else {
                        _verifiedContract.value = false
                        showToast(context, context.getString(R.string.required_to_sign_a_contract))
                    }
                },
                onError = { _, _ ->
                    _isLoading.value = false
                    _verifiedContract.value = false
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
                _language.value = it
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