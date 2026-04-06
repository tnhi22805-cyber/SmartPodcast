package com.example.smartpodcast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpodcast.data.local.EpisodeEntity
import com.example.smartpodcast.data.remote.PodcastApi
import com.example.smartpodcast.data.remote.RssParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
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
    }

    private fun loadPodcasts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}