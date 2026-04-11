package com.example.smartpodcast.data.repository

import com.example.smartpodcast.data.local.EpisodeDao
import com.example.smartpodcast.data.local.EpisodeEntity
import com.example.smartpodcast.data.remote.PodcastApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class PodcastRepository @Inject constructor(
    private val api: PodcastApi,
    private val dao: EpisodeDao,
    @ApplicationContext private val context: Context,
    private val firebaseSync: FirebaseSyncRepository
) {
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

    fun getEpisodes(): Flow<List<EpisodeEntity>> = dao.getAllEpisodes(userId)

    suspend fun getEpisodeById(id: String): EpisodeEntity? {
        return dao.getEpisodeById(id, userId)
    }

    suspend fun insertEpisodes(episodes: List<EpisodeEntity>) {
        val userFilteredEpisodes = episodes.map { it.copy(userId = userId) }
        dao.insertEpisodes(userFilteredEpisodes)
    }

    suspend fun updateEpisode(episode: EpisodeEntity) {
        dao.updateEpisode(episode)
    }

    suspend fun markListenHistory(id: String) {
        val time = System.currentTimeMillis()
        dao.updateLastListenedAt(id, userId, time)
        val ep = dao.getEpisodeById(id, userId)
        if (ep != null) {
            // Đẩy lên mây ngầm không block UI
            kotlinx.coroutines.GlobalScope.launch {
                firebaseSync.syncInteractionUp(id, ep.isFavorite, time)
            }
        }
    }

    suspend fun toggleFavorite(id: String): Boolean {
        return withContext(Dispatchers.IO) {
            val ep = dao.getEpisodeById(id, userId)
            if (ep != null) {
                val newState = !ep.isFavorite
                dao.updateEpisode(ep.copy(isFavorite = newState))
                // Bắn lên Firebase ở một tiến trình riêng để UI sáng ngay lập tức
                kotlinx.coroutines.GlobalScope.launch {
                    try {
                        firebaseSync.syncInteractionUp(id, newState, ep.lastListenedAt)
                    } catch (e: Exception) { }
                }
                newState
            } else false
        }
    }

    suspend fun forceSyncCloudDown() {
        withContext(Dispatchers.IO) {
            try {
                val cloudData = firebaseSync.fetchInteractions()
                for ((epId, data) in cloudData) {
                   val ep = dao.getEpisodeById(epId, userId)
                   val cloudFav = data["isFavorite"] as? Boolean ?: false
                   val cloudTime = data["lastListenedAt"] as? Long ?: 0L
                   if (ep != null) {
                       dao.updateEpisode(ep.copy(isFavorite = cloudFav, lastListenedAt = cloudTime))
                   }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun downloadPodcast(id: String, audioUrl: String, title: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, "podcasts")
                if (!dir.exists()) dir.mkdirs()
                
                // An toàn hóa tên file
                val safeTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".mp3"
                val file = File(dir, safeTitle)

                // Bắt đầu cắm ống hút dữ liệu (Download)
                if (!file.exists()) {
                    URL(audioUrl).openStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                // Tải xong -> Lưu DB
                val ep = dao.getEpisodeById(id, userId)
                if (ep != null) {
                    dao.updateEpisode(ep.copy(isDownloaded = true, localPath = file.absolutePath))
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}