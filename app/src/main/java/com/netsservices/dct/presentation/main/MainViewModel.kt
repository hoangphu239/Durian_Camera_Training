package com.netsservices.dct.presentation.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    var isAllGranted by mutableStateOf(false)
    var isLoggedIn by mutableStateOf(false)

    init {
        viewModelScope.launch {
            autoLoggedIn()
        }
    }

    private fun autoLoggedIn() {
        isLoggedIn = PreferenceManager.getAuthToken(context).isNotEmpty()
    }
}