package com.example.smartpodcast.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpodcast.data.local.EpisodeEntity
import com.example.smartpodcast.data.repository.PodcastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PodcastRepository
) : ViewModel() {

    private val _episodes = MutableStateFlow<List<EpisodeEntity>>(emptyList())
    val episodes: StateFlow<List<EpisodeEntity>> = _episodes

    init {
        loadPodcasts()
    }

    private fun loadPodcasts() {
        viewModelScope.launch {
            // --- ĐOẠN DỮ LIỆU GIẢ ĐỂ HIỆN GIAO DIỆN ---
            val dummyData = listOf(
                EpisodeEntity(
                    id = "1",
                    title = "Tập Podcast 1: Kiến thức Android",
                    description = "Chào mừng bạn đến với tập đầu tiên về lập trình Android.",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    imageUrl = "https://picsum.photos/200", // Link ảnh ngẫu nhiên
                    pubDate = "05/04/2024"
                ),
                EpisodeEntity(
                    id = "2",
                    title = "Tập Podcast 2: Hướng dẫn Hilt",
                    description = "Tìm hiểu về Dependency Injection trong Android với Hilt.",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    imageUrl = "https://picsum.photos/201",
                    pubDate = "06/04/2024"
                )
            )

            // Gán dữ liệu giả vào Flow để Fragment nhận được
            _episodes.value = dummyData
        }
    }
    }
