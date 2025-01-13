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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.maccappproject.components.CameraView
import com.example.maccappproject.components.OverlayView
import com.example.maccappproject.helpers.HandLandmarkerHelper

@Composable
fun DrawingScreen(navController: NavController) {
    val context = LocalContext.current
    var resultBundle by remember { mutableStateOf<HandLandmarkerHelper.ResultBundle?>(null) }
    var isFrontCamera by remember { mutableStateOf(false) } // Track camera facing

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
                },
                isFrontCamera = isFrontCamera
            )
            OverlayView(modifier = Modifier.fillMaxSize(), resultBundle = resultBundle, isFrontCamera = isFrontCamera)
        }
    } else {
        LaunchedEffect(Unit) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                resultBundle = null
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
                isFrontCamera = !isFrontCamera
            }) {
                Text("Switch Camera")
            }
        }
    }
}