package com.netsservices.dct.presentation.home

import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.response.DurianItem
import com.netsservices.dct.data.remote.response.Site
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.presentation.common.ConfigStep
import com.netsservices.dct.presentation.common.IMAGE_HEIGHT
import com.netsservices.dct.presentation.common.IMAGE_WIDTH
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.config.components.ModeSelectionDialog
import com.netsservices.dct.presentation.config.components.ScanMode
import com.netsservices.dct.presentation.helper.camera.FrameProcessor
import com.netsservices.dct.presentation.home.components.ScanCameraView
import java.util.concurrent.Executors


@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateLocation: () -> Unit,
    navigateVariety: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState = viewModel.uiState.collectAsState().value

    val processor = remember { FrameProcessor() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    var site by remember { mutableStateOf<Site?>(null) }
    var durianType by remember { mutableStateOf<DurianItem?>(null) }
    var scanMode by remember { mutableStateOf<ScanMode?>(null) }

    var isInitialized by remember { mutableStateOf(false) }
    val isConfigReady by remember {
        derivedStateOf { site != null && durianType != null && scanMode != null }
    }
    val currentStep by remember {
        derivedStateOf {
            when {
                site == null -> ConfigStep.SITE
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

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

            val capture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(IMAGE_WIDTH, IMAGE_HEIGHT))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { image ->
                if (uiState.blockCapture) return@setAnalyzer
                try {
                    if (isConfigReady) {
                        val raw = processor.imageProxyToJpeg(image)
                        raw?.let {
                            viewModel.checkFrame(raw)
                        }
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
                analysis,
                capture
            )
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            val guidance = uiState.dataFrame?.guidance ?: ""
            val isDetected = uiState.dataFrame?.durianDetected == true && uiState.dataFrame.ready

            AppText(
                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        if (uiState.blockCapture) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.confirm)) },
                text = { Text(stringResource(R.string.would_you_like_to_create_a_new_session)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.unblockCapture()
                    }) {
                        Text(stringResource(R.string.accept))
                    }
                }
            )
        }

        if (uiState.disconnect) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        imageVector = Icons.Default.SignalWifiConnectedNoInternet4,
                        contentDescription = "Back",
                        tint = colorResource(R.color.black)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(text = stringResource(R.string.no_internet_connection))
                }
            }
        }
    }

    if (isInitialized && currentStep != ConfigStep.DONE) {
        when (currentStep) {
            ConfigStep.SITE -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(stringResource(R.string.configuration_required)) },
                    text = { Text(stringResource(R.string.select_site)) },
                    confirmButton = {
                        TextButton(onClick = navigateLocation) {
                            Text(stringResource(R.string.accept))
                        }
                    }
                )
            }

            ConfigStep.MODE -> {
                ModeSelectionDialog(
                    currentMode = scanMode,
                    onConfirm = { selected ->
                        PreferenceManager.saveScanMode(context, selected)
                        scanMode = selected
                    },
                    onDismiss = {}
                )
            }

            ConfigStep.DURIAN_TYPE -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(stringResource(R.string.configuration_required)) },
                    text = { Text(stringResource(R.string.select_durian_type)) },
                    confirmButton = {
                        TextButton(onClick = navigateVariety) {
                            Text(stringResource(R.string.accept))
                        }
                    }
                )
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        site = PreferenceManager.getSite(context)
        durianType = PreferenceManager.getDurianVariety(context)
        scanMode = PreferenceManager.getScanMode(context)
        isInitialized = true
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                val action = PreferenceManager.getAction(context)

                if (action == ConfigStep.SITE.name) {
                    PreferenceManager.clearAction(context)
                    PreferenceManager.clearDurianVariety(context)
                    PreferenceManager.clearDurianVariety(context)
                    scanMode = null
                    durianType = null
                }

                if (action == ConfigStep.MODE.name) {
                    PreferenceManager.clearAction(context)
                    PreferenceManager.clearDurianVariety(context)
                    durianType = null
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }
}