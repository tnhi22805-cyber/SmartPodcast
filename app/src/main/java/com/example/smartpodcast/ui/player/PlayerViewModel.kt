package com.example.smartpodcast.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val player: ExoPlayer
) : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    // Bộ lắng nghe sự kiện THẬT từ máy phát
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            _isPlaying.value = playing // Cập nhật icon nút bấm đúng theo thực tế
        }
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_READY) {
                _duration.value = player.duration // Lấy độ dài thật khi nhạc đã sẵn sàng
            }
        }
    }

    init {
        player.addListener(playerListener)
        startTimer()
    }

    fun playEpisode(url: String) {
        // Nếu đang phát đúng bài này rồi thì giữ nguyên để nghe tiếp
        if (player.currentMediaItem?.localConfiguration?.uri.toString() == url) return

        try {
            val mediaItem = MediaItem.fromUri(url)
            player.setMediaItem(mediaItem)
            player.prepare() // BƯỚC NÀY LÀM NHẠC KÊU
            player.play()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                _currentPosition.value = player.currentPosition
                delay(1000) // Cập nhật thanh SeekBar mỗi giây
            }
        }
    }

    override fun onCleared() {
        player.removeListener(playerListener) // Dọn dẹp để không tốn pin
        super.onCleared()
    }
}