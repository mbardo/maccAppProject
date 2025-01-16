package com.example.maccappproject.components

import android.graphics.Bitmap
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
    onSaveComplete: () -> Unit = {}
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
    // Function to clear the list
    fun clearPoints() {
        pointList.clear()
        onClear() // Invoke the callback
    }

    // Use LaunchedEffect to observe clearOverlay and clear the list
    LaunchedEffect(clearOverlay) {
        if (clearOverlay) {
            clearPoints()
        }
    }

    // Use LaunchedEffect to observe save and save the drawing
    LaunchedEffect(save) {
        if (save) {
            val bitmap = view.drawToBitmap(Bitmap.Config.ARGB_8888)
            scope.launch {
                FirebaseManager.saveDrawing(
                    bitmap = bitmap,
                    color = drawingColor.toString(),
                    strokeSize = strokeSize
                ).onSuccess {
                    onSaveComplete()
                }.onFailure {
                    // Handle failure
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