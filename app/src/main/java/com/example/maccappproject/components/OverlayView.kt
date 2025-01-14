package com.example.maccappproject.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.example.maccappproject.helpers.HandLandmarkerHelper
import kotlin.math.max

@Composable
fun OverlayView(
    modifier: Modifier = Modifier,
    resultBundle: HandLandmarkerHelper.ResultBundle? = null,
    onClear: () -> Unit = {},
    clearOverlay: Boolean = false
) {
    val pointPaint = Paint().apply {
        color = Color.Yellow
        strokeWidth = 8f
        style = androidx.compose.ui.graphics.PaintingStyle.Fill
    }

    // Use remember to create a list that persists across recompositions
    val pointList = remember { mutableStateListOf<Offset>() }

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
            // Draw all points in the list
            pointList.forEach { point ->
                canvas.drawCircle(
                    point,
                    5.dp.toPx(),
                    pointPaint
                )
            }

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
        }
    }
}