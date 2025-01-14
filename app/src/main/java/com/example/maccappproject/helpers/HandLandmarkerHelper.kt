package com.example.maccappproject.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkerHelper(
    private val context: Context,
    private var listener: HandLandmarkerListener? = null
) {

    private var handLandmarker: HandLandmarker? = null

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        val baseOptionsBuilder = BaseOptions.builder()
        baseOptionsBuilder.setDelegate(Delegate.GPU)
        baseOptionsBuilder.setModelAssetPath("hand_landmarker.task")

        val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result: HandLandmarkerResult, input: MPImage ->
                val resultBundle = ResultBundle(
                    listOf(result),
                    SystemClock.uptimeMillis(),
                    input.height,
                    input.width
                )
                listener?.onResults(resultBundle)
            }

        handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder.build())
    }


    fun detectLiveStream(imageProxy: ImageProxy) {
        val mpImage = imageProxy.toMPImage()
        handLandmarker?.detectAsync(mpImage, SystemClock.uptimeMillis())
        imageProxy.close()
    }

    fun clearHandLandmarker() {
        handLandmarker?.close()
        handLandmarker = null
    }

    interface HandLandmarkerListener {
        fun onResults(resultBundle: ResultBundle)
    }

    data class ResultBundle(
        val results: List<HandLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )
}

// Extension function to convert ImageProxy to MPImage
fun ImageProxy.toMPImage(): MPImage {
    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val plane = this.planes[0]
    val buffer = plane.buffer
    val pixelStride = plane.pixelStride
    val rowStride = plane.rowStride
    buffer.rewind()
    for (y in 0 until this.height) {
        for (x in 0 until this.width) {
            val pixel = buffer.get(y * rowStride + x * pixelStride).toInt() and 0xFF
            bitmap.setPixel(x, y, android.graphics.Color.rgb(pixel, pixel, pixel))
        }
    }
    val matrix = Matrix().apply {
        // Rotate the frame received from the camera to be in the same direction as it'll be shown
        postRotate(this@toMPImage.imageInfo.rotationDegrees.toFloat())
    }
    val rotatedBitmap = Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height,
        matrix, true
    )
    return BitmapImageBuilder(rotatedBitmap).build()
}