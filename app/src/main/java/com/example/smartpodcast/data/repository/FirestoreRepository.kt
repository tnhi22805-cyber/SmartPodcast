package com.example.smartpodcast.data.repository

import com.example.smartpodcast.data.local.EpisodeEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    // Collection for Users
    private val usersRef = firestore.collection("users")

    /**
     * Đồng bộ bài hát ưa thích lên đám mây
     */
    suspend fun syncFavoriteToCloud(episode: EpisodeEntity) {
        val user = authRepository.currentUser ?: return
        
        // Lưu trữ với ID bài hát làm Document ID
        val data = hashMapOf(
            "id" to episode.id,
            "title" to episode.title,
            "description" to episode.description,
            "audioUrl" to episode.audioUrl,
            "imageUrl" to episode.imageUrl,
            "pubDate" to episode.pubDate,
            "timestamp" to System.currentTimeMillis()
        )
        
        if (episode.isFavorite) {
            usersRef.document(user.uid).collection("favorites").document(episode.id).set(data).await()
        } else {
            usersRef.document(user.uid).collection("favorites").document(episode.id).delete().await()
        }
    }

    /**
     * Kéo toàn bộ danh sách yêu thích từ đám mây xuống (Khi đăng nhập máy mới)
     */
    suspend fun fetchCloudFavorites(): List<EpisodeEntity> {
        val user = authRepository.currentUser ?: return emptyList()
        val result = mutableListOf<EpisodeEntity>()
        
        try {
            val snapshot = usersRef.document(user.uid).collection("favorites").get().await()
            for (doc in snapshot.documents) {
                val episode = EpisodeEntity(
                    id = doc.getString("id") ?: "",
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    audioUrl = doc.getString("audioUrl") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    pubDate = doc.getString("pubDate") ?: "",
                    isFavorite = true, // Đã có trên cloud chắc chắn là favorite
                    isDownloaded = false,
                    localPath = null
                )
                result.add(episode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
