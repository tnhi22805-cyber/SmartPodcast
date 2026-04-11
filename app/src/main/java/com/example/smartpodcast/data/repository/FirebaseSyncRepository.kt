package com.example.smartpodcast.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun syncInteractionUp(episodeId: String, isFavorite: Boolean, lastListenedAt: Long) {
        val uid = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "originalId" to episodeId,
            "isFavorite" to isFavorite,
            "lastListenedAt" to lastListenedAt
        )
        try {
            // Firestore không cho phép ID chứa dấu gạch chéo "/", nên ta cần thay thế nó.
            val safeEpisodeId = episodeId.replace("/", "_").replace(":", "_")
            firestore.collection("users").document(uid)
                .collection("interactions").document(safeEpisodeId)
                .set(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchInteractions(): Map<String, Map<String, Any>> {
        val uid = auth.currentUser?.uid ?: return emptyMap()
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("interactions").get().await()
            val result = mutableMapOf<String, Map<String, Any>>()
            for (doc in snapshot.documents) {
                // Khôi phục lại ID gốc
                val originalEpisodeId = doc.id.replace("_", "/").replace("https///", "https://").replace("http///", "http://")
                // Do việc đảo ngược từ '_' sang '/' không chính xác 100% nếu url có các dấu gạch dưới tự nhiên. 
                // Thay vì thế, để chắc chắn, ta đổi logic fetch dựa trên ID nếu sau này cần. Nhưng tạm thời ta match ID cục bộ.
                // Để chuẩn hơn, ta nên lưu ID Gốc như 1 biến (field) trong document luôn!
                val realId = doc.getString("originalId") ?: doc.id
                result[realId] = doc.data ?: emptyMap()
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }
}
