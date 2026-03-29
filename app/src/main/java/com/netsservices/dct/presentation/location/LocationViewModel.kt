package com.netsservices.dct.presentation.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.ApiResult
import com.netsservices.dct.data.remote.response.Orchard
import com.netsservices.dct.data.remote.response.Plantation
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.SearchMode
import com.netsservices.dct.presentation.common.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository
) : ViewModel() {

    data class UiState(
        val searchMode: SearchMode = SearchMode.SITE,

        val sites: List<Site> = emptyList(),
        val plantations: List<Plantation> = emptyList(),
        val orchards: List<Orchard> = emptyList(),

        val selectSite: Site? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var job: Job? = null


    fun setSearchMode(mode: SearchMode) {
        _uiState.update {
            it.copy(
                searchMode = mode,
                sites = emptyList(),
                orchards = emptyList(),
                plantations = emptyList()
            )
        }
    }

    fun searchRouter(query: String) {
        job?.cancel()
        job = viewModelScope.launch {
            when (_uiState.value.searchMode) {
                SearchMode.SITE -> quickSearch(query)
                SearchMode.ORCHARD -> searchOrchards(query)
                SearchMode.PLANTATION -> searchPlantations(query)
            }
        }
    }

    fun quickSearch(query: String) {
        viewModelScope.launch {
            when (val result = repo.quickSearch(query)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(sites = result.data.items) }
                }

                is ApiResult.Error -> {
                    context.showToast(result.message ?: "Unknown error")
                }

                is ApiResult.Exception -> {
                    context.showToast("Network error: ${result.exception.localizedMessage}")
                }
            }
        }
    }

    fun searchPlantations(query: String) {
        viewModelScope.launch {
            when (val result = repo.searchPlantations(query)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(plantations = result.data.items) }
                }

                is ApiResult.Error -> {
                    context.showToast(result.message ?: "Unknown error")
                }

                is ApiResult.Exception -> {
                    context.showToast("Network error: ${result.exception.localizedMessage}")
                }
            }
        }
    }

    fun searchOrchards(query: String) {
        viewModelScope.launch {
            when (val result = repo.searchOrchards(query)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(orchards = result.data.items) }
                }

                is ApiResult.Error -> {
                    context.showToast(result.message ?: "Unknown error")
                }

                is ApiResult.Exception -> {
                    context.showToast("Network error: ${result.exception.localizedMessage}")
                }
            }
        }
    }

    fun selectSite(site: Site) {
        _uiState.update { it.copy(selectSite = site) }
    }
}