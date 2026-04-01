package com.netsservices.dct.presentation.main

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.netsservices.dct.domain.model.Country
import com.netsservices.dct.presentation.helper.location.LocationManager
import com.netsservices.dct.presentation.utils.Utils.getCountryInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.P)
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationManager: LocationManager,
) : ViewModel() {
    var isAllGranted by mutableStateOf(false)

    var gps by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    var countryInfo by mutableStateOf(Country())
        private set

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
}