package com.netsservices.dct.presentation.helper

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class PermissionManager(
    activity: ComponentActivity
) {
    companion object {
        const val PERMISSION_CAMERA = Manifest.permission.CAMERA
    }

    private var onComplete: ((Boolean) -> Unit)? = null
    private var allGranted = true

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
        requestCamera()
    }

    private fun requestCamera() {
        launcherCamera.launch(PERMISSION_CAMERA)
    }

    private fun finish() {
        onComplete?.invoke(allGranted)
        onComplete = null
    }
}


