package com.example.maccappproject.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import com.example.maccappproject.helpers.HandLandmarkerHelper
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max
import kotlin.math.min

@Composable
fun OverlayView(
    modifier: Modifier = Modifier,
    resultBundle: HandLandmarkerHelper.ResultBundle? = null,
    runningMode: RunningMode = RunningMode.LIVE_STREAM,
    isFrontCamera: Boolean = false
) {
    val linePaint = Paint().apply {
        color = Color.Green
        strokeWidth = 8f
        style = androidx.compose.ui.graphics.PaintingStyle.Stroke
    }
    val pointPaint = Paint().apply {
        color = Color.Yellow
        strokeWidth = 8f
        style = androidx.compose.ui.graphics.PaintingStyle.Fill
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        resultBundle?.let { handLandmarkerResult ->
            val imageHeight = handLandmarkerResult.inputImageHeight
            val imageWidth = handLandmarkerResult.inputImageWidth

            val scaleFactor = when (runningMode) {
                RunningMode.IMAGE,
                RunningMode.VIDEO -> {
                    min(canvasWidth / imageWidth, canvasHeight / imageHeight)
                }
                RunningMode.LIVE_STREAM -> {
                    max(canvasWidth / imageWidth, canvasHeight / imageHeight)
                }
                else -> 1f
            }

            val offsetX = (canvasWidth - (imageWidth * scaleFactor)) / 2f
            val offsetY = (canvasHeight - (imageHeight * scaleFactor)) / 2f


            drawIntoCanvas { canvas ->
                for (landmark in handLandmarkerResult.results.first().landmarks()) {
                    for (normalizedLandmark in landmark) {
                        val x = normalizedLandmark.x() * imageWidth * scaleFactor + offsetX
                        val y = normalizedLandmark.y() * imageHeight * scaleFactor + offsetY
                        val mirroredX = if (isFrontCamera) canvasWidth - x else x
                        canvas.drawCircle(
                            Offset(
                                mirroredX,
                                y
                            ),
                            5.dp.toPx(),
                            pointPaint
                        )
                    }

                    HandLandmarker.HAND_CONNECTIONS.forEach {
                        val startX = landmark[it!!.start()].x() * imageWidth * scaleFactor + offsetX
                        val startY = landmark[it.start()].y() * imageHeight * scaleFactor + offsetY
                        val endX = landmark[it.end()].x() * imageWidth * scaleFactor + offsetX
                        val endY = landmark[it.end()].y() * imageHeight * scaleFactor + offsetY

                        val mirroredStartX = if (isFrontCamera) canvasWidth - startX else startX
                        val mirroredEndX = if (isFrontCamera) canvasWidth - endX else endX

                        canvas.drawLine(
                            Offset(mirroredStartX, startY),
                            Offset(mirroredEndX, endY),
                            linePaint
                        )
                    }
                }
            }
        }
    }
}