package com.netsservices.dct.presentation.home

import android.os.Build
import android.util.Size
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.presentation.common.ConfigStep
import com.netsservices.dct.presentation.common.ConfirmDialog
import com.netsservices.dct.presentation.common.DeviceStatus
import com.netsservices.dct.presentation.common.LoadingOverlay
import com.netsservices.dct.presentation.common.NoInternetView
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.config.ConfigViewModel
import com.netsservices.dct.presentation.config.components.ModeSelectionDialog
import com.netsservices.dct.presentation.config.components.ScanMode
import com.netsservices.dct.presentation.helper.camera.FrameProcessor
import com.netsservices.dct.presentation.home.components.ScanCameraView
import java.util.concurrent.Executors


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    configViewModel: ConfigViewModel,
    gps: Pair<Double, Double>?,
    navigateLocation: () -> Unit,
    navigateVariety: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsState()

    val processor = remember { FrameProcessor() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    var deviceStatus by remember { mutableStateOf(DeviceStatus.UNACTIVE.value) }
    var scanMode by remember { mutableStateOf<ScanMode?>(null) }
    var durianType by remember { mutableStateOf<DurianItem?>(null) }

    var isInitialized by remember { mutableStateOf(false) }

    val isConfigReady by remember {
        derivedStateOf { scanMode != null && durianType != null }
    }

    val currentStep by remember {
        derivedStateOf {
            when {
                deviceStatus == DeviceStatus.UNACTIVE.value -> ConfigStep.REGISTER_DEVICE
                scanMode == null -> ConfigStep.MODE
                durianType == null -> ConfigStep.DURIAN_TYPE
                else -> ConfigStep.DONE
            }
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    var hasRegistered by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        val listener = Runnable {
            val provider = cameraProviderFuture.get()
            val resolution = Size(1080, 1920)

            // ===== PREVIEW =====
            val preview = Preview.Builder()
                .setTargetResolution(resolution)
                .build()
                .apply {
                    surfaceProvider = previewView.surfaceProvider
                }

            // ===== ANALYSIS =====
            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(resolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { image ->

                if (!isConfigReady || uiState.blockCapture) {
                    image.close()
                    return@setAnalyzer
                }

                try {
                    val bitmap = processor.imageProxyToBitmap(image)
                    bitmap?.let {
                        val rotation = image.imageInfo.rotationDegrees
                        val rotated = when (rotation) {
                            90 -> processor.rotateBitmap(it, 90)
                            180 -> processor.rotateBitmap(it, 180)
                            270 -> processor.rotateBitmap(it, 270)
                            else -> it
                        }
                        val jpeg = processor.bitmapToJpeg(rotated)
                        viewModel.checkFrame(jpeg)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    image.close()
                }
            }

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )
        }

        cameraProviderFuture.addListener(
            listener,
            ContextCompat.getMainExecutor(context)
        )

        onDispose {
            executor.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        deviceStatus = PreferenceManager.getDeviceStatus(context)
        scanMode = PreferenceManager.getScanMode(context)
        durianType = PreferenceManager.getDurianVariety(context)
        isInitialized = true
    }

    LaunchedEffect(currentStep, isInitialized) {
        if (!isInitialized) return@LaunchedEffect

        if (currentStep == ConfigStep.REGISTER_DEVICE && !hasRegistered) {
            hasRegistered = true
            viewModel.registerDevice {
                deviceStatus = PreferenceManager.getDeviceStatus(context)
            }
        }
    }

    LaunchedEffect(gps) {
        gps?.let { viewModel.updateGPS(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize()) {

            val guidance = uiState.dataFrame?.guidance ?: ""
            val isDetected =
                uiState.dataFrame?.durianDetected == true &&
                        uiState.dataFrame?.ready == true

            AppText(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(start = 10.dp, bottom = 30.dp),
                text = guidance,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = R.color.red
            )

            ScanCameraView(
                modifier = Modifier.weight(1f),
                previewView = previewView,
                isDetected = isDetected,
            )
        }

        if (uiState.isLoading) LoadingOverlay()

        if (uiState.blockCapture) {
            ConfirmDialog(onConfirm = { viewModel.unblockCapture() })
        }

        if (uiState.disconnect) {
            NoInternetView()
        }
    }

    if (isInitialized && currentStep != ConfigStep.DONE) {
        when (currentStep) {

            ConfigStep.MODE -> {
                ModeSelectionDialog(
                    viewModel = configViewModel,
                    currentMode = scanMode,
                    onConfirm = {
                        PreferenceManager.saveScanMode(context, it)
                        scanMode = it
                    },
                    onDismiss = {}
                )
            }

            ConfigStep.DURIAN_TYPE -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Configuration required") },
                    text = { Text("Select durian type") },
                    confirmButton = {
                        TextButton(onClick = navigateVariety) {
                            Text("OK")
                        }
                    }
                )
            }

            else -> Unit
        }
    }
}