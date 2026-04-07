package com.example.smartpodcast.ui.home

import com.example.smartpodcast.data.local.EpisodeEntity

/**
 * Sealed interface to manage UI States: Loading, Success, and Error.
 * Following the UED Research Repository requirements.
 */
sealed interface PodcastUiState {
    object Loading : PodcastUiState
    data class Success(val episodes: List<EpisodeEntity>) : PodcastUiState
    data class Error(val message: String) : PodcastUiState
}
