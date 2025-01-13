package com.example.maccappproject.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.maccappproject.helpers.HandLandmarkerHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    onHandLandmark: (HandLandmarkerHelper.ResultBundle) -> Unit,
    isFrontCamera: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val handLandmarkerHelper = remember {
        HandLandmarkerHelper(context, object : HandLandmarkerHelper.HandLandmarkerListener {
            override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
                onHandLandmark(resultBundle)
            }
        })
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Log.d("CameraView", "AndroidView factory called")
            val previewView = PreviewView(ctx).apply {
                keepScreenOn = true
                scaleType = PreviewView.ScaleType.FILL_START
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                    Log.d("CameraView", "Preview bound to surface provider")
                }

                val imageAnalyzer = ImageAnalysis.Builder().build().also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        handLandmarkerHelper.detectLiveStream(imageProxy, isFrontCamera)
                    }
                    Log.d("CameraView", "ImageAnalyzer set")
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(if (isFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                Log.d("CameraView", "Camera bound to lifecycle")
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        onRelease = {
            handLandmarkerHelper.clearHandLandmarker()
        }
    )
}