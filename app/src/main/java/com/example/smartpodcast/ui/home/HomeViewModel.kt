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
            // Lấy dữ liệu từ Repository (Sinh và Sương đã chuẩn bị)
            repository.getEpisodes().collect { list ->
                _episodes.value = list
            }
        }
    }
}