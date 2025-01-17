package com.example.maccappproject.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.maccappproject.utils.FirebaseManager

@Composable
fun SettingsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Placeholder for settings options
        Column {
            Text("Settings Screen Placeholder")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Option 1: Placeholder")
            Text("Option 2: Placeholder")
            Text("Option 3: Placeholder")
        }

        // Logout button
        Button(
            onClick = {
                FirebaseManager.signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Logout")
        }
    }
}