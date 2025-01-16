import android.graphics.Bitmap
import com.example.maccappproject.models.Drawing
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.Date
// app/src/main/java/com.example.maccappproject/utils/FirebaseManager.kt

object FirebaseManager {
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private val firestore = Firebase.firestore

    fun getCurrentUser() = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDrawing(bitmap: Bitmap, name: String = "drawing_${System.currentTimeMillis()}"): Result<String> {
        return try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            val storageRef = storage.reference
                .child("users/${getCurrentUser()?.uid}/drawings/$name.png")

            val uploadTask = storageRef.putBytes(data).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            getCurrentUser()?.let { user ->
                firestore.collection("drawings")
                    .add(hashMapOf<String, Any>(
                        "userId" to user.uid,
                        "name" to name,
                        "url" to downloadUrl.toString(),
                        "createdAt" to FieldValue.serverTimestamp()
                    )).await()
            } ?: throw IllegalStateException("User must be logged in to save drawings")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDrawings(): Result<List<Drawing>> {
        return try {
            val drawings = firestore.collection("drawings")
                .whereEqualTo("userId", getCurrentUser()?.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val drawingList = drawings.map { doc ->
                Drawing(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    url = doc.getString("url") ?: "",
                    createdAt = doc.getTimestamp("createdAt")?.toDate()
                )
            }

            Result.success(drawingList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}