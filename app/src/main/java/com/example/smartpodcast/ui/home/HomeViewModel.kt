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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: PodcastApi,
    private val repository: PodcastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PodcastUiState>(PodcastUiState.Loading)
    val uiState: StateFlow<PodcastUiState> = _uiState.asStateFlow()

    private var allEpisodes: List<com.example.smartpodcast.data.local.EpisodeEntity> = emptyList()

    // Đổi sang link Podcast chuẩn Apple/iTunes (NPR Planet Money)
    // Link này đảm bảo có Audio chuẩn và Hình ảnh đẹp
    private val APPLE_RSS_URL = "https://feeds.npr.org/510289/podcast.xml"

    init {
        fetchPodcasts(APPLE_RSS_URL)
    }

    fun fetchPodcasts(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PodcastUiState.Loading
            try {
                Log.d("DEBUG_RSS", "Fetching Apple Standard RSS: $url")
                val response = api.getRawRss(url)
                val episodes = RssParser().parse(response.byteStream())

                if (episodes.isNotEmpty()) {
                    repository.insertEpisodes(episodes)
                    allEpisodes = episodes
                    _uiState.value = PodcastUiState.Success(episodes)
                    Log.d("DEBUG_RSS", "Successfully loaded ${episodes.size} episodes")
                } else {
                    _uiState.value = PodcastUiState.Error("RSS content is valid but no episodes found.")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_RSS", "Error: ${e.message}")
                _uiState.value = PodcastUiState.Error("Failed to connect to Apple Feed: ${e.message}")
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = PodcastUiState.Success(allEpisodes)
            return
        }
        val lowerQuery = query.lowercase()
        val filtered = allEpisodes.filter {
            it.title.lowercase().contains(lowerQuery) ||
                    it.description.lowercase().contains(lowerQuery)
        }
        _uiState.value = PodcastUiState.Success(filtered)
    }
}
