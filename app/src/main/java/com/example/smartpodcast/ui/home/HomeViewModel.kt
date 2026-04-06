package com.example.smartpodcast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpodcast.data.local.EpisodeEntity
import com.example.smartpodcast.data.remote.PodcastApi
import com.example.smartpodcast.data.remote.RssParser
import com.example.smartpodcast.data.repository.PodcastRepository
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
    private val api: PodcastApi, // Khai báo api ở đây để hết lỗi đỏ dòng 34
    private val repository: PodcastRepository
) : ViewModel() {

    // Khai báo _episodes ở đây để hết lỗi đỏ dòng 38
    private val _episodes = MutableStateFlow<List<EpisodeEntity>>(emptyList())
    val episodes: StateFlow<List<EpisodeEntity>> = _episodes.asStateFlow()

    init {
        loadPodcasts()
    }

    private fun loadPodcasts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Tải dữ liệu thật từ VnExpress
                val response = api.getRawRss("https://vnexpress.net/rss/podcast.rss")
                val parser = RssParser()
                val realEpisodes = parser.parse(response.byteStream())

                withContext(Dispatchers.Main) {
                    _episodes.value = realEpisodes
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}