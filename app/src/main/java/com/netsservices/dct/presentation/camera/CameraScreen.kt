package com.netsservices.dct.presentation.camera

import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.netsservices.dct.R
import com.netsservices.dct.presentation.components.AppText
import com.netsservices.dct.presentation.helper.camera.FrameProcessor
import com.netsservices.dct.presentation.home.HomeViewModel
import com.netsservices.dct.presentation.main.MainViewModel
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    mainViewModel: MainViewModel,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState = viewModel.uiState.collectAsState().value

    val processor = remember { FrameProcessor() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(executor) { image ->
            try {
                if (processor.shouldProcess() && viewModel.shouldSendFrame()) {
                    val raw = processor.process(image)
                    raw?.let {
                        if (mainViewModel.isAllGranted && viewModel.isLoggedIn) {
                            viewModel.checkFrame(it)
                        }
                    }
                }
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

    Column(modifier = Modifier.fillMaxSize()) {
        val guidance = uiState.dataFrame?.guidance ?: ""
        if (guidance.isNotEmpty()) {
            AppText(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = guidance,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = R.color.red
            )
        }
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp),
            factory = { previewView }
        )
    }
}
