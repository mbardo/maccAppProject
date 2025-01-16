// app/src/main/java/com.example.maccappproject/models/Drawing.kt

package com.example.maccappproject.models

import java.util.Date

data class Drawing(
    val id: String,
    val name: String,
    val url: String,
    val createdAt: Date? = null
)