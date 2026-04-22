package com.netsservices.dct.presentation.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository
) : ViewModel() {

    data class UiState(
        val sites: List<Site> = emptyList(),
        val selectSite: Site? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        val savedSite = PreferenceManager.getSite(context)
        if (savedSite != null) {
            _uiState.update { it.copy(selectSite = savedSite) }
        }
        observeSearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            queryFlow
                .map { it.trim() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotEmpty()) {
                        search(query)
                    } else {
                        _uiState.update { it.copy(sites = emptyList()) }
                    }
                }
        }
    }

    private suspend fun search(query: String) {
        repo.quickSearch(query).handle(
            onSuccess = { data ->
                _uiState.update { it.copy(sites = data.items) }
            }
        )
    }

    fun onQueryChanged(query: String) {
        queryFlow.value = query
    }

    fun selectSite(site: Site) {
        _uiState.update { it.copy(selectSite = site) }
        PreferenceManager.saveSite(context, site)
    }

    fun getCurrentSite(): Site? {
        return PreferenceManager.getSite(context)
    }

    fun updateAction(action: String) {
        PreferenceManager.saveAction(context, action)
    }
}