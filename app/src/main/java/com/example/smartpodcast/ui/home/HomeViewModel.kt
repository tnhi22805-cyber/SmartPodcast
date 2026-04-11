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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FilterType { ALL, HISTORY, FAVORITE, DOWNLOAD }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: PodcastApi,
    private val repository: PodcastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PodcastUiState>(PodcastUiState.Loading)
    val uiState: StateFlow<PodcastUiState> = _uiState.asStateFlow()

    private val _currentFilter = MutableStateFlow(FilterType.ALL)
    val currentFilter: StateFlow<FilterType> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Đổi sang link Podcast chuẩn Apple/iTunes (NPR Planet Money)
    // Link này đảm bảo có Audio chuẩn và Hình ảnh đẹp
    private val APPLE_RSS_URL = "https://feeds.npr.org/510289/podcast.xml"

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(repository.getEpisodes(), _currentFilter, _searchQuery) { episodes, filter, query ->
                var filtered = when (filter) {
                    FilterType.ALL -> episodes
                    FilterType.HISTORY -> episodes.filter { it.lastListenedAt > 0L }.sortedByDescending { it.lastListenedAt }
                    FilterType.FAVORITE -> episodes.filter { it.isFavorite }
                    FilterType.DOWNLOAD -> episodes.filter { it.isDownloaded }
                }
                
                if (query.isNotBlank()) {
                    filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
                }
                
                filtered
            }.collect { filteredList ->
                // Chỉ hiển thị rỗng nếu Filter != ALL, còn ALL mà rỗng thì chờ API
                if (filteredList.isNotEmpty() || _currentFilter.value != FilterType.ALL) {
                    _uiState.value = PodcastUiState.Success(filteredList)
                }
            }
        }
        fetchPodcasts(APPLE_RSS_URL)
    }

    fun setFilter(filter: FilterType) {
        _currentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
                    repository.forceSyncCloudDown()
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
}
