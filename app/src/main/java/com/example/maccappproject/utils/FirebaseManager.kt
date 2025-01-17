package com.example.maccappproject.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private val auth = Firebase.auth
    private val storage = Firebase.storage.reference
    @SuppressLint("StaticFieldLeak")
    private val firestore = Firebase.firestore

    // Auth functions
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "signIn: Attempting sign in with email: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "signIn: Sign in successful for user: ${it.uid}")
                Result.success(it)
            } ?: run {
                Log.e(TAG, "signIn: Authentication failed")
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "signIn: Exception during sign in", e)
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "signUp: Attempting sign up with email: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "signUp: Sign up successful for user: ${it.uid}")
                Result.success(it)
            } ?: run {
                Log.e(TAG, "signUp: Sign up failed")
                Result.failure(Exception("Sign up failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUp: Exception during sign up", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        Log.d(TAG, "signOut: User signed out")
        auth.signOut()
    }

    suspend fun saveDrawing(
        bitmap: Bitmap,
        color: String,
        strokeSize: Float
    ): Result<String> {
        return try {
            val currentUser = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
            val drawingId = UUID.randomUUID().toString()

            // Convert bitmap to bytes
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val imageData = baos.toByteArray()

            // Upload to Storage
            val storageRef = storage.child("drawings/$currentUser/$drawingId.png")
            storageRef.putBytes(imageData).await()
            val downloadUrl = storageRef.downloadUrl.await()

            // Save metadata to Firestore
            val drawing = hashMapOf(
                "userId" to currentUser,
                "drawingId" to drawingId,
                "url" to downloadUrl.toString(),
                "createdAt" to Timestamp.now(),
                "color" to color,
                "strokeSize" to strokeSize
            )

            firestore.collection("drawings")
                .document(drawingId)
                .set(drawing)
                .await()

            Result.success(drawingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDrawings(): Result<List<Drawing>> {
        return try {
            val currentUser = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
            Log.d(TAG, "getDrawings: Getting drawings for user: $currentUser")

            val snapshot = firestore.collection("drawings")
                .whereEqualTo("userId", currentUser)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            Log.d(TAG, "getDrawings: Firestore snapshot retrieved")

            val drawings = snapshot.documents.mapNotNull { doc ->
                Drawing(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    drawingId = doc.getString("drawingId") ?: "",
                    url = doc.getString("url") ?: "",
                    createdAt = doc.getTimestamp("createdAt")
                        ?: Timestamp.now(),
                    color = doc.getString("color") ?: "",
                    strokeSize = doc.getDouble("strokeSize")?.toFloat() ?: 8f
                )
            }
            Log.d(TAG, "getDrawings: Drawings mapped successfully, count: ${drawings.size}")

            Result.success(drawings)
        } catch (e: Exception) {
            Log.e(TAG, "getDrawings: Exception during get drawings", e)
            Result.failure(e)
        }
    }

    suspend fun deleteDrawing(drawingId: String): Result<Unit> {
        return try {
            val currentUser = getCurrentUser()?.uid ?: throw Exception("User not authenticated")
            Log.d(TAG, "deleteDrawing: Deleting drawing with ID: $drawingId for user: $currentUser")

            // Delete from Storage
            storage.child("drawings/$currentUser/$drawingId.png").delete().await()
            Log.d(TAG, "deleteDrawing: Image deleted from storage")

            // Delete from Firestore
            firestore.collection("drawings")
                .document(drawingId)
                .delete()
                .await()
            Log.d(TAG, "deleteDrawing: Metadata deleted from Firestore")

            Log.d(TAG, "deleteDrawing: com.example.maccappproject.utils.Drawing deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteDrawing: Exception during delete drawing", e)
            Result.failure(e)
        }
    }
}

data class Drawing(
    val id: String = "",
    val userId: String = "",
    val drawingId: String = "",
    val url: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val color: String = "",
    val strokeSize: Float = 8f
)