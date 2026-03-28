package com.netsservices.dct.presentation.config

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.presentation.common.LanguagePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ConfigViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _language = MutableStateFlow("en")
    val language = _language.asStateFlow()

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
}