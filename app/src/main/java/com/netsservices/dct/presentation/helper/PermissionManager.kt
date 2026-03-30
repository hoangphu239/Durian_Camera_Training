package com.netsservices.dct.presentation.helper

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class PermissionManager(
    activity: ComponentActivity
) {
    companion object {
        val PERMISSION_LOCATION = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        const val PERMISSION_CAMERA = Manifest.permission.CAMERA
    }

    private var onComplete: ((Boolean) -> Unit)? = null
    private var allGranted = true

    private val launcherLocation =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val granted = result.values.all { it }
            allGranted = allGranted && granted
            requestCamera()
        }

    private val launcherCamera =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            allGranted = allGranted && granted
            finish()
        }

    fun requestAll(onComplete: (Boolean) -> Unit) {
        this.onComplete = onComplete
        allGranted = true
        requestLocation()
    }

    private fun requestLocation() {
        launcherLocation.launch(PERMISSION_LOCATION)
    }

    private fun requestCamera() {
        launcherCamera.launch(PERMISSION_CAMERA)
    }

    private fun finish() {
        onComplete?.invoke(allGranted)
        onComplete = null
    }
}


