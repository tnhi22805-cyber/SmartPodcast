package com.example.smartpodcast.data.repository

import com.example.smartpodcast.data.local.EpisodeDao
import com.example.smartpodcast.data.local.EpisodeEntity
import com.example.smartpodcast.data.remote.PodcastApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
// Data repository
@Singleton
class PodcastRepository @Inject constructor(
    private val api: PodcastApi,
    private val dao: EpisodeDao
) {
    // Lấy dữ liệu từ Database (Local)
    fun getEpisodes(): Flow<List<EpisodeEntity>> = dao.getAllEpisodes()

    // Logic lấy dữ liệu từ mạng (Remote) và lưu vào máy (Local)
    suspend fun fetchAndSavePodcasts() {
        try {
            // Sau này Sương (B) sẽ làm phần Parse XML ở đây
            // Tạm thời Sinh viết khung logic để gánh dòng code
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}