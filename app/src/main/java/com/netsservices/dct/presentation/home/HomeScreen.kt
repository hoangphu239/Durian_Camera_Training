package com.netsservices.dct.presentation.home

import android.os.Build
import android.util.Size
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.presentation.common.ConfigStep
import com.netsservices.dct.presentation.common.Constants
import com.netsservices.dct.presentation.common.DeviceStatus
import com.netsservices.dct.presentation.common.LoadingOverlay
import com.netsservices.dct.presentation.common.NoInternetView
import com.netsservices.dct.presentation.components.AppDialog
import com.netsservices.dct.presentation.config.ConfigViewModel
import com.netsservices.dct.presentation.config.components.ModeSelectionDialog
import com.netsservices.dct.presentation.config.components.ScanMode
import com.netsservices.dct.presentation.helper.camera.FrameProcessor
import com.netsservices.dct.presentation.helper.camera.GyroStabilityDetector
import com.netsservices.dct.presentation.home.components.DurianOverlay
import com.netsservices.dct.presentation.home.components.PreviewImageDialog
import com.netsservices.dct.presentation.home.components.ScanOverlayLayer
import com.netsservices.dct.presentation.utils.Utils
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
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var hasRegistered by remember { mutableStateOf(false) }
    val gyroDetector = remember { GyroStabilityDetector(context) }


    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        val listener = Runnable {
            val provider = cameraProviderFuture.get()
            val resolution = Size(Constants.CAMERA_TARGET_WIDTH, Constants.CAMERA_TARGET_HEIGHT)

            val preview = Preview.Builder()
                .setTargetResolution(resolution)
                .build()
                .apply {
                    surfaceProvider = previewView.surfaceProvider
                }

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(resolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { image ->

                if (!isConfigReady || uiState.blockCapture || !gyroDetector.isStable) {
                    image.close()
                    return@setAnalyzer
                }

                try {
                    val bitmap = processor.imageProxyToBitmap(image)
                    bitmap?.let { original ->
                        val rotation = image.imageInfo.rotationDegrees
                        val rotated = when (rotation) {
                            90 -> processor.rotateBitmap(original, 90)
                            180 -> processor.rotateBitmap(original, 180)
                            270 -> processor.rotateBitmap(original, 270)
                            else -> original
                        }

                        viewModel.imageSize = IntSize(
                            rotated.width,
                            rotated.height
                        )

                        val mode = PreferenceManager.getScanMode(context)
                        if (mode == ScanMode.COLLECTION) {
                            viewModel.updateLaserPoints(emptyList())
                            val previewWidth = previewView.width.toFloat()
                            val previewHeight = previewView.height.toFloat()

                            if (previewWidth == 0f || previewHeight == 0f) {
                                image.close()
                                return@setAnalyzer
                            }

                            val cropped = Utils.cropToOvalRegion(
                                bitmap = rotated,
                                previewWidth = previewWidth,
                                previewHeight = previewHeight
                            )
                            val jpeg = processor.bitmapToJpeg(cropped)
                            viewModel.checkFrame(jpeg)
                            return@setAnalyzer
                        }

                        val previewWidth = previewView.width.toFloat()
                        val previewHeight = previewView.height.toFloat()
                        if (previewWidth == 0f || previewHeight == 0f) {
                            image.close()
                            return@setAnalyzer
                        }

                        val cropped = Utils.cropToOvalRegion(
                            bitmap = rotated,
                            previewWidth = previewWidth,
                            previewHeight = previewHeight
                        )

                        val triangle = Utils.createTriangle(
                            previewWidth,
                            previewHeight
                        )

                        val bitmapTargets = triangle.targets.map { previewPoint ->
                            Utils.mapPreviewToBitmap(
                                point = previewPoint,
                                previewWidth = previewWidth,
                                previewHeight = previewHeight,
                                imageWidth = rotated.width.toFloat(),
                                imageHeight = rotated.height.toFloat()
                            )
                        }

                        val scaleX = rotated.width.toFloat() / previewWidth
                        val radiusInBitmap =
                            (triangle.side * Constants.CIRCLE_RADIUS_RATIO) * scaleX

                        val centers = processor.detectLaserCentersWithROI(
                            bitmap = rotated,
                            targets = bitmapTargets,
                            radius = radiusInBitmap
                        )

                        viewModel.updateLaserPoints(
                            centers.map { Offset(it.x, it.y) }
                        )
                        val jpeg = processor.bitmapToJpeg(cropped)
                        viewModel.updateLatestFrame(jpeg)
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

    DisposableEffect(Unit) {
        gyroDetector.start()

        onDispose {
            gyroDetector.stop()
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        val guidance = uiState.dataFrame?.guidance ?: ""
        val isDetected =
            uiState.dataFrame?.durianDetected == true &&
                    uiState.dataFrame?.ready == true

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                previewView.apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                }
            }
        )

        DurianOverlay(guidance = guidance)

        ScanOverlayLayer(
            viewModel = viewModel,
            context = context,
            isDetected = isDetected
        )

        if (uiState.isLoading) {
            LoadingOverlay()
        }

        if (uiState.blockCapture) {
            AppDialog(
                title = stringResource(R.string.confirm),
                message = stringResource(R.string.would_you_like_to_create_a_new_session),
                confirmText = stringResource(R.string.accept),
                onConfirm = { viewModel.unblockCapture() }
            )
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
                AppDialog(
                    title = stringResource(R.string.configuration_required),
                    message = stringResource(R.string.select_durian_type),
                    confirmText = stringResource(R.string.accept),
                    onConfirm = { navigateVariety() }
                )
            }

            else -> Unit
        }
    }

    if (uiState.showPreviewDialog && uiState.previewImage != null) {
        PreviewImageDialog(viewModel, uiState.previewImage!!)
    }
}