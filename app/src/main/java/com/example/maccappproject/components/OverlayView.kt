package com.example.maccappproject.components

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalView
import androidx.core.view.drawToBitmap
import com.example.maccappproject.helpers.HandLandmarkerHelper
import com.example.maccappproject.utils.FirebaseManager
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun OverlayView(
    modifier: Modifier = Modifier,
    resultBundle: HandLandmarkerHelper.ResultBundle? = null,
    onClear: () -> Unit = {},
    clearOverlay: Boolean = false,
    drawingColor: Color = Color.Yellow,
    strokeSize: Float = 8f,
    save: Boolean = false,
    onSaveComplete: () -> Unit = {},
    onSaveFailed: () -> Unit = {}
) {
    val pointPaint = remember(drawingColor, strokeSize) {
        Paint().apply {
            color = drawingColor
            strokeWidth = strokeSize
            strokeCap = StrokeCap.Round
            strokeJoin = StrokeJoin.Round
        }
    }

    // Use remember to create a list that persists across recompositions
    val pointList = remember { mutableStateListOf<Offset>() }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val TAG = "OverlayView"

    // Function to clear the list
    fun clearPoints() {
        Log.d(TAG, "clearPoints called")
        pointList.clear()
        onClear() // Invoke the callback
    }

    // Use LaunchedEffect to observe clearOverlay and clear the list
    LaunchedEffect(clearOverlay) {
        Log.d(TAG, "LaunchedEffect(clearOverlay) triggered, clearOverlay = $clearOverlay")
        if (clearOverlay) {
            clearPoints()
        }
    }

    // Use LaunchedEffect to observe save and save the drawing
    LaunchedEffect(save) {
        Log.d(TAG, "LaunchedEffect(save) triggered, save = $save")
        if (save) {
            Log.d(TAG, "Saving drawing...")
            val bitmap = view.drawToBitmap(Bitmap.Config.ARGB_8888)
            Log.d(TAG, "Bitmap created")
            scope.launch {
                Log.d(TAG, "Coroutine launched to save drawing")
                Log.d(TAG, "Before FirebaseManager.saveDrawing")
                val result = FirebaseManager.saveDrawing(
                    bitmap = bitmap,
                    color = drawingColor.toString(),
                    strokeSize = strokeSize
                )
                Log.d(TAG, "After FirebaseManager.saveDrawing, result: $result")
                result.onSuccess { drawingId ->
                    Log.d(TAG, "Drawing saved successfully, drawingId: $drawingId")
                    onSaveComplete()
                }.onFailure { exception ->
                    Log.e(TAG, "Failed to save drawing", exception)
                    onSaveFailed()
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        val imageHeight = resultBundle?.inputImageHeight ?: 0
        val imageWidth = resultBundle?.inputImageWidth ?: 0

        val scaleFactor = if (imageWidth > 0 && imageHeight > 0) {
            max(
                size.width / imageWidth.toFloat(),
                size.height / imageHeight.toFloat()
            )
        } else {
            1f
        }

        val offsetY = if (imageHeight > 0) {
            (size.height - (imageHeight * scaleFactor)) / 2f
        } else {
            0f
        }

        drawIntoCanvas { canvas ->
            // Add new point to pointList
            resultBundle?.let { handLandmarkerResult ->
                handLandmarkerResult.results.firstOrNull()?.landmarks()?.firstOrNull()?.let { landmark ->
                    val indexFingerTip = landmark.getOrNull(8)

                    indexFingerTip?.let { normalizedLandmark ->
                        val x = normalizedLandmark.x() * imageWidth * scaleFactor
                        val y = normalizedLandmark.y() * imageHeight * scaleFactor + offsetY
                        val currentPoint = Offset(x, y)

                        // Add the current point to the list
                        pointList.add(currentPoint)
                    }
                }
            }

            // Draw lines connecting the points
            for (i in 0 until pointList.size - 1) {
                val startPoint = pointList[i]
                val endPoint = pointList[i + 1]
                canvas.drawLine(
                    p1 = startPoint,
                    p2 = endPoint,
                    paint = pointPaint
                )
            }
        }
    }
}