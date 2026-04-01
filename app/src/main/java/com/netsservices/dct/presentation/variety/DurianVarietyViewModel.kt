package com.netsservices.dct.presentation.variety

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.DurianItem
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
class DurianVarietyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository,
) : ViewModel() {

    data class UiState(
        val durianVarieties: List<DurianItem> = emptyList(),
        val selectDurianVariety: DurianItem? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        val savedVariety = PreferenceManager.getDurianVariety(context)
        if (savedVariety != null) {
            _uiState.update { it.copy(selectDurianVariety = savedVariety) }
        }
    }

    @SuppressLint("LogNotTimber")
    fun getDurianVarieties(countryCode: String) {
        viewModelScope.launch {
            repo.getDurianVarieties(countryCode).handle(
                onSuccess = { data ->
                    _uiState.update { it.copy(durianVarieties = data.items) }
                }
            )
        }
    }

    fun selectDurianVariety(durianVariety: DurianItem) {
        _uiState.update { it.copy(selectDurianVariety = durianVariety) }
        PreferenceManager.saveDurianVariety(context, durianVariety)
    }
}