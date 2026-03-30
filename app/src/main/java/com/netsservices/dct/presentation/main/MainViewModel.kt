package com.netsservices.dct.presentation.main

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.domain.model.Country
import com.netsservices.dct.presentation.helper.location.LocationManager
import com.netsservices.dct.presentation.utils.Utils.getCountryInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationManager: LocationManager,
) : ViewModel() {
    var isAllGranted by mutableStateOf(false)
    var isLoggedIn by mutableStateOf(false)

    var gps by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    var countryInfo by mutableStateOf(Country())
        private set

    init {
        viewModelScope.launch {
            autoLoggedIn()
        }
    }

    fun getCoordinate() {
        locationManager.getCoordinate {
            gps = it
            gps?.let {
                val country = getCountryInfo(
                    context = context,
                    latitude = gps!!.first,
                    longitude = gps!!.second
                )
                countryInfo = Country(code = country.first!!, name = country.second!!)
            }
        }
    }

    private fun autoLoggedIn() {
        isLoggedIn = PreferenceManager.getAuthToken(context).isNotEmpty()
    }
}