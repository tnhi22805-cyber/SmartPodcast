package com.example.smartpodcast.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpodcast.data.remote.PodcastApi
import com.example.smartpodcast.data.remote.RssParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
<<<<<<< Updated upstream
    private val api: PodcastApi
) : ViewModel() {

    private val _episodes = MutableStateFlow<List<EpisodeEntity>>(emptyList())
    val episodes: StateFlow<List<EpisodeEntity>> = _episodes.asStateFlow()

    init {
        // 1. HIỆN NGAY DỮ LIỆU ĐỂ TEST GIAO DIỆN
        _episodes.value = listOf(
            EpisodeEntity("id1", "Đang tải dữ liệu thật...", "Vui lòng đợi trong giây lát", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "https://picsum.photos/200", "Bây giờ"),
            EpisodeEntity("id2", "Apple Podcast Podcast", "Nguồn dữ liệu chuẩn từ Spotify/Anchor", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "https://picsum.photos/201", "Hôm nay")
        )
        // 2. SAU ĐÓ MỚI TẢI THẬT
        loadPodcasts()
=======
    private val api: PodcastApi,
    private val repository: PodcastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PodcastUiState>(PodcastUiState.Loading)
    val uiState: StateFlow<PodcastUiState> = _uiState.asStateFlow()

    // Đổi sang link Podcast chuẩn Apple/iTunes (NPR Planet Money)
    // Link này đảm bảo có Audio chuẩn và Hình ảnh đẹp
    private val APPLE_RSS_URL = "https://feeds.npr.org/510289/podcast.xml"

    init {
        fetchPodcasts(APPLE_RSS_URL)
>>>>>>> Stashed changes
    }

    fun fetchPodcasts(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PodcastUiState.Loading
            try {
<<<<<<< Updated upstream
                val response = api.getTrendingPodcasts() // Gọi API Apple
                val realEpisodes = response.results.map {
                    EpisodeEntity(
                        id = it.previewUrl ?: "",
                        title = it.trackName ?: "No Title",
                        description = it.collectionName ?: "",
                        audioUrl = it.previewUrl ?: "",
                        imageUrl = it.artworkUrl100 ?: "",
                        pubDate = "Apple Podcast"
                    )
                }
                withContext(Dispatchers.Main) {
                    _episodes.value = realEpisodes
=======
                Log.d("DEBUG_RSS", "Fetching Apple Standard RSS: $url")
                val response = api.getRawRss(url)
                val episodes = RssParser().parse(response.byteStream())

                if (episodes.isNotEmpty()) {
                    repository.insertEpisodes(episodes)
                    _uiState.value = PodcastUiState.Success(episodes)
                    Log.d("DEBUG_RSS", "Successfully loaded ${episodes.size} episodes")
                } else {
                    _uiState.value = PodcastUiState.Error("RSS content is valid but no episodes found.")
>>>>>>> Stashed changes
                }
            } catch (e: Exception) {
                Log.e("DEBUG_RSS", "Error: ${e.message}")
                _uiState.value = PodcastUiState.Error("Failed to connect to Apple Feed: ${e.message}")
            }
        }
    }
}
