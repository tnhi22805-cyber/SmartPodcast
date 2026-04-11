package com.example.smartpodcast.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpodcast.data.remote.PodcastApi
import com.example.smartpodcast.data.remote.RssParser
import com.example.smartpodcast.data.repository.PodcastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: PodcastApi,
    private val repository: PodcastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PodcastUiState>(PodcastUiState.Loading)
    val uiState: StateFlow<PodcastUiState> = _uiState.asStateFlow()

    private val _isShowingDownloadsOnly = MutableStateFlow(false)
    val isShowingDownloadsOnly: StateFlow<Boolean> = _isShowingDownloadsOnly.asStateFlow()

    private var baseEpisodes: List<com.example.smartpodcast.data.local.EpisodeEntity> = emptyList()
    private var dbEpisodesMap: Map<String, com.example.smartpodcast.data.local.EpisodeEntity> = emptyMap()
    private var currentSearchQuery: String = ""

    private val APPLE_RSS_URL = "https://feeds.npr.org/510289/podcast.xml"

    init {
        viewModelScope.launch {
            repository.getEpisodes().collect { localEpisodes ->
                dbEpisodesMap = localEpisodes.associateBy { it.id }
                if (baseEpisodes.isEmpty() && localEpisodes.isNotEmpty()) {
                    baseEpisodes = localEpisodes
                }
                applyFilters() 
            }
        }
        fetchPodcasts(APPLE_RSS_URL)
    }

    fun fetchPodcasts(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PodcastUiState.Loading
            try {
                val response = api.getRawRss(url)
                val episodes = RssParser().parse(response.byteStream())

                if (episodes.isNotEmpty()) {
                    baseEpisodes = episodes 
                    repository.insertEpisodes(episodes)
                } else {
                    _uiState.value = PodcastUiState.Error("RSS content is valid but no episodes found.")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_RSS", "Network Error: ${e.message}, falling back to local cache.")
                if (baseEpisodes.isEmpty()) {
                    _uiState.value = PodcastUiState.Error("Mất mạng và chưa có dữ liệu nào trong máy.")
                } else {
                    applyFilters()
                }
            }
        }
    }

    fun deleteDownload(episode: com.example.smartpodcast.data.local.EpisodeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                episode.localPath?.let { path ->
                    val rawPath = if (path.startsWith("file://")) android.net.Uri.parse(path).path ?: path else path
                    val file = java.io.File(rawPath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                val updated = episode.copy(isDownloaded = false, localPath = null)
                repository.updateEpisode(updated)
            } catch (e: Exception) {
                Log.e("DEBUG_DELETE", "Error deleting file: ${e.message}")
            }
        }
    }

    fun toggleDownloadFilter() {
        _isShowingDownloadsOnly.value = !_isShowingDownloadsOnly.value
        applyFilters()
    }

    fun search(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = baseEpisodes.map { base ->
            val dbItem = dbEpisodesMap[base.id]
            if (dbItem != null) {
                base.copy(isDownloaded = dbItem.isDownloaded, localPath = dbItem.localPath)
            } else base
        }
        
        if (_isShowingDownloadsOnly.value) {
            filtered = filtered.filter { it.isDownloaded }
        }

        if (currentSearchQuery.isNotBlank()) {
            val lowerQuery = currentSearchQuery.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(lowerQuery) ||
                it.description.lowercase().contains(lowerQuery)
            }
        }
        
        _uiState.value = PodcastUiState.Success(filtered)
    }
}
