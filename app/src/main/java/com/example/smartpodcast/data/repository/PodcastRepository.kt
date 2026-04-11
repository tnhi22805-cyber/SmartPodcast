package com.example.smartpodcast.data.repository

import com.example.smartpodcast.data.local.EpisodeDao
import com.example.smartpodcast.data.local.EpisodeEntity
import com.example.smartpodcast.data.remote.PodcastApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastRepository @Inject constructor(
    private val api: PodcastApi,
    private val dao: EpisodeDao
) {
    fun getEpisodes(): Flow<List<EpisodeEntity>> = dao.getAllEpisodes()

    suspend fun insertEpisodes(list: List<EpisodeEntity>) {
        dao.insertEpisodes(list)
    }
}