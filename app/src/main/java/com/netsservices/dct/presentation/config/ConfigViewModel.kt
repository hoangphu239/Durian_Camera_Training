package com.netsservices.dct.presentation.config

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.presentation.common.LanguagePrefs
import com.netsservices.dct.presentation.config.components.ScanMode
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
) : ViewModel() {

    private val _language = MutableStateFlow("en")
    val language = _language.asStateFlow()

    private val _currentSite = MutableStateFlow<Site?>(null)
    val currentSite: StateFlow<Site?> = _currentSite

    private val _currentVariety = MutableStateFlow<DurianItem?>(null)
    val currentVariety: StateFlow<DurianItem?> = _currentVariety

    private val _currentMode = MutableStateFlow<ScanMode?>(null)
    val currentMode: StateFlow<ScanMode?> = _currentMode


    init {
        observeLanguage()
    }

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

    fun loadSite() {
        _currentSite.value = PreferenceManager.getSite(context)
    }

    fun loadDurianVariety() {
        _currentVariety.value = PreferenceManager.getDurianVariety(context)
    }

    fun loadScanMode() {
        _currentMode.value = PreferenceManager.getScanMode(context)
    }

    fun saveScanMode(scanMode: ScanMode) {
        PreferenceManager.saveScanMode(context, scanMode)
        _currentMode.value = scanMode
    }

    fun updateAction(action: String) {
        PreferenceManager.saveAction(context, action)
    }
}