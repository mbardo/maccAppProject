// app/src/main/java/com.example.maccappproject/utils/FirebaseManager.kt

import android.graphics.Bitmap
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
    private val auth = Firebase.auth
    private val storage = Firebase.storage.reference
    private val firestore = Firebase.firestore

    // Auth functions
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Sign up failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // Drawing functions
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
                "createdAt" to com.google.firebase.Timestamp.now(),
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

            val snapshot = firestore.collection("drawings")
                .whereEqualTo("userId", currentUser)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val drawings = snapshot.documents.mapNotNull { doc ->
                Drawing(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    drawingId = doc.getString("drawingId") ?: "",
                    url = doc.getString("url") ?: "",
                    createdAt = doc.getTimestamp("createdAt")
                        ?: com.google.firebase.Timestamp.now(),
                    color = doc.getString("color") ?: "",
                    strokeSize = doc.getDouble("strokeSize")?.toFloat() ?: 8f
                )
            }

            Result.success(drawings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDrawing(drawingId: String): Result<Unit> {
        return try {
            val currentUser = getCurrentUser()?.uid ?: throw Exception("User not authenticated")

            // Delete from Storage
            storage.child("drawings/$currentUser/$drawingId.png").delete().await()

            // Delete from Firestore
            firestore.collection("drawings")
                .document(drawingId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Data class for Drawing
data class Drawing(
    val id: String = "",
    val userId: String = "",
    val drawingId: String = "",
    val url: String = "",
    val createdAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val color: String = "",
    val strokeSize: Float = 8f
)