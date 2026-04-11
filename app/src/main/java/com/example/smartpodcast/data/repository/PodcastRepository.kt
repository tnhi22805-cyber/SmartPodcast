package com.example.smartpodcast.data.repository

import com.example.smartpodcast.data.local.EpisodeDao
import com.example.smartpodcast.data.local.EpisodeEntity
import com.example.smartpodcast.data.remote.PodcastApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastRepository @Inject constructor(
    private val api: PodcastApi,
    private val dao: EpisodeDao
) {
    fun getEpisodes(): Flow<List<EpisodeEntity>> = dao.getAllEpisodes()

    suspend fun insertEpisodes(list: List<EpisodeEntity>) {
        val existingList = dao.getAllEpisodes().firstOrNull() ?: emptyList()
        val existingMap = existingList.associateBy { it.id }

        val mergedList = list.map { newEpisode ->
            val old = existingMap[newEpisode.id]
            if (old != null) {
                newEpisode.copy(
                    isDownloaded = old.isDownloaded,
                    localPath = old.localPath,
                    isFavorite = old.isFavorite
                )
            } else {
                newEpisode
            }
        }
        dao.insertEpisodes(mergedList)
    }

    suspend fun updateEpisode(episode: EpisodeEntity) {
        dao.updateEpisode(episode)
    }
}