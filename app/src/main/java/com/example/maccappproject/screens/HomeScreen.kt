package com.example.maccappproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { navController.navigate("drawing") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Start Drawing")
        }
        Button(
            onClick = { navController.navigate("gallery") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("View Gallery")
        }
        Button(
            onClick = { navController.navigate("settings") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Settings")
        }
    }
}
