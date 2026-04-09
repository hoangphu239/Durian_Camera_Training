package com.netsservices.dct.presentation.main

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.handle
import com.netsservices.dct.data.remote.response.LanguageResponse
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.model.Country
import com.netsservices.dct.domain.repository.Repository
import com.netsservices.dct.presentation.common.LanguagePrefs
import com.netsservices.dct.presentation.helper.location.LocationManager
import com.netsservices.dct.presentation.utils.Utils.getCountryInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.P)
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationManager: LocationManager,
    private val repo: Repository
) : ViewModel() {

    val mapLang = LanguagePrefs.getTranslations(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    var isAllGranted by mutableStateOf(false)

    var gps by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    var countryInfo by mutableStateOf(Country())
        private set

    var languages by mutableStateOf<List<LanguageResponse>>(emptyList())
        private set

    init {
        getLanguages()
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ]
    )
    fun getCoordinate() {
        locationManager.getCoordinate { coord ->
            gps = coord
            coord.let {
                val country = getCountryInfo(
                    context = context,
                    latitude = coord.first,
                    longitude = coord.second
                )
                countryInfo = Country(
                    code = country.first!!,
                    name = country.second!!
                )
            }
        }
    }

    fun getLanguages() {
        viewModelScope.launch {
            repo.getLanguages().handle(
                onSuccess = { data ->
                    languages = data
                }
            )
        }
    }

    fun clearData() {
        PreferenceManager.clearData(context)
    }
}