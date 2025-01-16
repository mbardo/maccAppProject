package com.example.maccappproject.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.maccappproject.helpers.HandLandmarkerHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    onHandLandmark: (HandLandmarkerHelper.ResultBundle) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val handLandmarkerHelper = remember {
        HandLandmarkerHelper(context, object : HandLandmarkerHelper.HandLandmarkerListener {
            override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
                onHandLandmark(resultBundle)
            }
        })
    }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            Log.d("CameraView", "Unbinding camera use cases")
            handLandmarkerHelper.clearHandLandmarker()
            cameraProviderFuture.get().unbindAll()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Log.d("CameraView", "AndroidView factory called")
            val previewView = PreviewView(ctx).apply {
                keepScreenOn = true
                scaleType = PreviewView.ScaleType.FILL_START
            }

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
                Log.d("CameraView", "Preview bound to surface provider")
            }

            val imageAnalyzer = ImageAnalysis.Builder().build().also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    handLandmarkerHelper.detectLiveStream(imageProxy)
                }
                Log.d("CameraView", "ImageAnalyzer set")
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                Log.d("CameraView", "Camera bound to lifecycle")
            } catch (e: Exception) {
                Log.e("CameraView", "Error binding camera use cases", e)
            }

            previewView
        }
    )
}