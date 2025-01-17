package com.example.maccappproject.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.maccappproject.utils.FirebaseManager
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.maccappproject.navigation.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun GalleryScreen(navController: NavHostController) {
    var drawings by remember { mutableStateOf(emptyList<com.example.maccappproject.utils.Drawing>()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDrawings by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val borderWidth: Dp = 2.dp
    val borderColor: Color = Color.LightGray

    LaunchedEffect(Unit) {
        scope.launch {
            FirebaseManager.getDrawings()
                .onSuccess { fetchedDrawings ->
                    drawings = fetchedDrawings
                    isLoading = false
                }
                .onFailure { exception ->
                    // Handle error, e.g., show a toast
                    println("Error getting drawings: ${exception.message}")
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { navController.navigate(Screen.HOME) }) {
                Text("Home")
            }
            Button(
                onClick = {
                    scope.launch {
                        selectedDrawings.forEach { drawingId ->
                            FirebaseManager.deleteDrawing(drawingId)
                                .onSuccess {
                                    drawings = drawings.filterNot { it.drawingId == drawingId }
                                    selectedDrawings = selectedDrawings.filterNot { it == drawingId }.toSet()
                                }
                                .onFailure { exception ->
                                    println("Error deleting drawing: ${exception.message}")
                                }
                        }
                    }
                },
                enabled = selectedDrawings.isNotEmpty()
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                Text("Delete")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (drawings.isEmpty()) {
            Text("No drawings saved yet.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(drawings, key = {it.drawingId}) { drawing ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(drawing.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Drawing",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .border(
                                BorderStroke(
                                    borderWidth,
                                    if (selectedDrawings.contains(drawing.drawingId)) Color.Blue else borderColor
                                ),
                                shape = RectangleShape
                            )
                            .clickable {
                                selectedDrawings = if (selectedDrawings.contains(drawing.drawingId)) {
                                    selectedDrawings - drawing.drawingId
                                } else {
                                    selectedDrawings + drawing.drawingId
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}