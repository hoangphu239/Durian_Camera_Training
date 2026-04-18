package com.netsservices.dct.presentation.home

import android.os.Build
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.i18n.LangKey
import com.netsservices.dct.presentation.common.ConfigStep
import com.netsservices.dct.presentation.common.ConfirmDialog
import com.netsservices.dct.presentation.common.DeviceStatus
import com.netsservices.dct.presentation.common.LoadingOverlay
import com.netsservices.dct.presentation.common.NoInternetView
import com.netsservices.dct.presentation.common.getText
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
    val uiState = viewModel.uiState.collectAsState().value

    val processor = remember { FrameProcessor() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    var deviceStatus by remember { mutableStateOf(DeviceStatus.UNACTIVE.value) }
    var scanMode by remember { mutableStateOf<ScanMode?>(null) }
//  var site by remember { mutableStateOf<Site?>(null) }
    var durianType by remember { mutableStateOf<DurianItem?>(null) }

    var isInitialized by remember { mutableStateOf(false) }
    val isConfigReady by remember {
        derivedStateOf { deviceStatus == DeviceStatus.ACTIVATE.value && scanMode != null && durianType != null }
//     derivedStateOf { deviceStatus == DeviceStatus.ACTIVATE.value && scanMode != null && site != null && durianType != null }
    }
    val currentStep by remember {
        derivedStateOf {
            when {
                deviceStatus == DeviceStatus.UNACTIVE.value -> ConfigStep.REGISTER_DEVICE
                scanMode == null -> ConfigStep.MODE
//                site == null -> ConfigStep.SITE
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
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val mapLang = viewModel.mapLang.collectAsState().value

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        val listener = Runnable {
            val provider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

            val analysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
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
                        val finalBitmap = processor.cropAndResize(it)
                        val jpeg = processor.bitmapToJpeg(finalBitmap)
                        viewModel.checkFrame( jpeg)
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

        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            executor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            val guidance = uiState.dataFrame?.guidance?:" "
            val isDetected = uiState.dataFrame?.durianDetected == true && uiState.dataFrame.ready

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

        if (uiState.isLoading) {
            LoadingOverlay()
        }

        if (uiState.blockCapture) {
            ConfirmDialog(
                onConfirm = { viewModel.unblockCapture() }
            )
        }

        if (uiState.disconnect) {
            NoInternetView()
        }
    }

    if (isInitialized && currentStep != ConfigStep.DONE) {
        when (currentStep) {
            ConfigStep.REGISTER_DEVICE -> {
                viewModel.registerDevice {
                    deviceStatus = PreferenceManager.getDeviceStatus(context)
                }
            }

            ConfigStep.MODE -> {
                ModeSelectionDialog(
                    viewModel = configViewModel,
                    currentMode = scanMode,
                    onConfirm = { selectedMode ->
                        PreferenceManager.saveScanMode(context, selectedMode)
                        scanMode = selectedMode
                    },
                    onDismiss = {}
                )
            }

//            ConfigStep.SITE -> {
//                AlertDialog(
//                    onDismissRequest = {},
//                    title = { Text(stringResource(R.string.configuration_required)) },
//                    text = { Text(stringResource(R.string.select_site)) },
//                    confirmButton = {
//                        TextButton(onClick = navigateLocation) {
//                            Text(stringResource(R.string.accept))
//                        }
//                    }
//                )
//            }

            ConfigStep.DURIAN_TYPE -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        AppText(
                            text = mapLang.getText(
                                LangKey.Message.ConfigurationRequired,
                                R.string.configuration_required
                            ),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        AppText(
                            text = mapLang.getText(
                                LangKey.Message.SelectDurianVariety,
                                R.string.config_durian_variety
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = navigateVariety) {
                            AppText(
                                text = mapLang.getText(LangKey.Button.Accept, R.string.accept),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                        }
                    }
                )
            }

            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        deviceStatus = PreferenceManager.getDeviceStatus(context)
        scanMode = PreferenceManager.getScanMode(context)
//      site = PreferenceManager.getSite(context)
        durianType = PreferenceManager.getDurianVariety(context)
        isInitialized = true
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                val action = PreferenceManager.getAction(context)

                if (action == ConfigStep.MODE.name) {
                    PreferenceManager.clearAction(context)
                    PreferenceManager.clearDurianVariety(context)
                    durianType = null
                }

//                if (action == ConfigStep.SITE.name) {
//                    PreferenceManager.clearAction(context)
//                    PreferenceManager.clearDurianVariety(context)
//                    scanMode = null
//                    durianType = null
//                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(gps) {
        gps?.let {
            viewModel.updateGPS(it)
        }
    }
}