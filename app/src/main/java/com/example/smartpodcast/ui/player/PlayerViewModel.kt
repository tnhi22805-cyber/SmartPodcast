package com.example.smartpodcast.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val player: ExoPlayer // Dùng Player của Nhi đã setup
) : ViewModel() {

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    init {
        updateProgress()
    }

    fun playEpisode(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _isPlaying.value = true
        _duration.value = player.duration
    }

    private fun updateProgress() {
        viewModelScope.launch {
            while (true) {
                _currentPosition.value = player.currentPosition
                _isPlaying.value = player.isPlaying
                _duration.value = player.duration
                delay(1000) // Cập nhật thanh SeekBar mỗi giây
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }
    fun startSleepTimer(minutes: Int) {
        viewModelScope.launch {
            delay(minutes * 60 * 1000L)
            player.pause()
            _isPlaying.value = false
        }
    }
}