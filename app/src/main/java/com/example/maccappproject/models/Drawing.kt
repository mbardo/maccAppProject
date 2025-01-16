// app/src/main/java/com.example.maccappproject/models/Drawing.kt

package com.example.maccappproject.models

import java.util.Date

// app/src/main/java/com.example.maccappproject/models/Drawing.kt

data class Drawing(
    val id: String = "",
    val userId: String = "",
    val drawingId: String = "",
    val url: String = "",
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val color: String = "",
    val strokeSize: Float = 8f
)