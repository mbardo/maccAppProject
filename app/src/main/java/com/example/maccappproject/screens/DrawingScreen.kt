package com.example.maccappproject.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.maccappproject.components.CameraView
import com.example.maccappproject.components.OverlayView
import com.example.maccappproject.helpers.HandLandmarkerHelper
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import com.example.maccappproject.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun DrawingScreen(navController: NavController) {
    // 1. State Variables
    val context = LocalContext.current
    var resultBundle by remember { mutableStateOf<HandLandmarkerHelper.ResultBundle?>(null) }
    var clearOverlay by remember { mutableStateOf(false) }
    var drawingColor by remember { mutableStateOf(Color.Yellow) }
    var strokeSize by remember { mutableFloatStateOf(8f) }
    var isSaving by remember { mutableStateOf(false) }
    var save by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 2. Camera Permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // 3. Camera Permission Check and UI Setup
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 4. Camera View
            CameraView(
                modifier = Modifier.fillMaxSize(),
                onHandLandmark = {
                    resultBundle = it
                }
            )
            // 5. Overlay View with Size Tracking
            OverlayView(
                modifier = Modifier.fillMaxSize(),
                resultBundle = resultBundle,
                onClear = { clearOverlay = false },
                clearOverlay = clearOverlay,
                drawingColor = drawingColor,
                strokeSize = strokeSize,
                save = save,
                onSaveComplete = {
                    scope.launch {
                        isSaving = false
                        save = false
                        navController.navigate(Screen.GALLERY)
                    }
                }
            )
            // 6. Bottom Controls, Color Selector and Stroke Size Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.Bottom
            ) {
                // 7. Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        clearOverlay = true
                    }) {
                        Text("Clear")
                    }

                    // 8. Save Button
                    Button(
                        onClick = {
                            isSaving = true
                            save = true
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save")
                        }
                    }

                    Button(onClick = {
                        navController.navigate(Screen.HOME)
                    }) {
                        Text("Home")
                    }
                }
                // 9. Color Selector
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Color: ", modifier = Modifier.weight(0.2f))
                    ColorSelector(
                        onColorSelected = { drawingColor = it },
                        modifier = Modifier.weight(0.8f)
                    )
                }
                // 10. Stroke Size Slider
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stroke Size: ", modifier = Modifier.weight(0.4f))
                    Slider(
                        value = strokeSize,
                        onValueChange = { strokeSize = it },
                        valueRange = 1f..30f,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }
    } else {
        // Permission is not granted, request it
        LaunchedEffect(Unit) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun ColorSelector(onColorSelected: (Color) -> Unit, modifier: Modifier = Modifier) {
    val colors = listOf(
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color.Black,
        Color.White
    )
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        colors.forEach { color ->
            Button(
                onClick = { onColorSelected(color) },
                modifier = Modifier.size(30.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = color)
            ) {}
        }
    }
}