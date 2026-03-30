package com.netsservices.dct.presentation.helper.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
){
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCoordinate(onSuccess: (Pair<Double, Double>) -> Unit) {
        fusedLocationClient
            .lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val pair = Pair(location.latitude, location.longitude)
                    onSuccess(pair)
                }
            }
    }
}
























