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

@Composable
fun DrawingScreen(navController: NavController) {
    val context = LocalContext.current
    var resultBundle by remember { mutableStateOf<HandLandmarkerHelper.ResultBundle?>(null) }
    var clearOverlay by remember { mutableStateOf(false) }
    var drawingColor by remember { mutableStateOf(Color.Yellow) }
    var strokeSize by remember { mutableFloatStateOf(8f) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraView(
                modifier = Modifier.fillMaxSize(),
                onHandLandmark = {
                    resultBundle = it
                }
            )
            OverlayView(
                modifier = Modifier.fillMaxSize(),
                resultBundle = resultBundle,
                onClear = { clearOverlay = false },
                clearOverlay = clearOverlay,
                drawingColor = drawingColor,
                strokeSize = strokeSize
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(androidx.compose.ui.Alignment.BottomCenter),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        clearOverlay = true
                    }) {
                        Text("Clear")
                    }
                    Button(onClick = { /* Handle Save Action */ }) {
                        Text("Save")
                    }
                    Button(onClick = { /* Handle Share Action */ }) {
                        Text("Share")
                    }
                    Button(onClick = {
                        navController.navigate("home")
                    }) {
                        Text("Home")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Color: ", modifier = Modifier.weight(0.2f))
                    ColorSelector(
                        onColorSelected = { drawingColor = it },
                        modifier = Modifier.weight(0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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