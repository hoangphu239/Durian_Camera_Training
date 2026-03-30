package com.netsservices.dct.presentation.home

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.netsservices.dct.R
import com.netsservices.dct.data.remote.utils.PreferenceManager
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.helper.camera.FrameProcessor
import com.netsservices.dct.presentation.home.components.ScanCameraView
import com.netsservices.dct.presentation.main.MainViewModel
import java.util.concurrent.Executors


@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    viewModel: HomeViewModel,
    navigateConfig: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState = viewModel.uiState.collectAsState().value

    val processor = remember { FrameProcessor() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val captureExecutor = remember { Executors.newSingleThreadExecutor() }
    val isReady = remember { mutableStateOf(false) }
    val showConfigDialog = remember { mutableStateOf(false) }


    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            captureExecutor.shutdown()
        }
    }

    LaunchedEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

            val capture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val analysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { image ->
                try {
                    if (isReady.value) {
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
    }

    if (showConfigDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.configuration_required)) },
            text = { Text(stringResource(R.string.select_site_and_durian_type)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfigDialog.value = false
                    navigateConfig()
                }) {
                    Text(stringResource(R.string.accept))
                }
            }
        )
    }

    LaunchedEffect(mainViewModel.isAllGranted) {
        val site = PreferenceManager.getSite(context)
        val durianType = PreferenceManager.getDurianVariety(context)

        if (mainViewModel.isAllGranted) {
            if (site == null || durianType == null) {
                showConfigDialog.value = true
                isReady.value = false
            } else {
                isReady.value = true
            }
        }
    }
}