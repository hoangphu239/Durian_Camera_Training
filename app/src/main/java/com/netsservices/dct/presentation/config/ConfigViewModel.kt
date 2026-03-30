package com.netsservices.dct.presentation.config

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.presentation.common.LanguagePrefs
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

    private val _selectedSite = MutableStateFlow<Site?>(null)
    val selectedSite: StateFlow<Site?> = _selectedSite

    private val _selectedDurianVariety = MutableStateFlow<DurianItem?>(null)
    val selectedDurianVariety: StateFlow<DurianItem?> = _selectedDurianVariety

    private val _selectedFringerprint = MutableStateFlow(false)
    val selectedFringerprint: StateFlow<Boolean> = _selectedFringerprint


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
        _selectedSite.value = PreferenceManager.getSite(context)
    }

    fun loadDurianVariety() {
        _selectedDurianVariety.value = PreferenceManager.getDurianVariety(context)
    }

    fun loadFringerPrint() {
        _selectedFringerprint.value = PreferenceManager.getFingerprintStatus(context)
    }

    fun saveFingerprintStatus(context: Context, isEnable: Boolean) {
        PreferenceManager.saveFingerprintStatus(context, isEnable)
        _selectedFringerprint.value = isEnable
    }
}