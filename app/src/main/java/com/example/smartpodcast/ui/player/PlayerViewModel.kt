package com.example.smartpodcast.ui.player

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(val player: ExoPlayer) : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
            }
        })
    }

    fun playEpisode(url: String) {
        if (url.isEmpty()) return
        if (player.currentMediaItem?.localConfiguration?.uri.toString() == url) return

        val mediaItem = MediaItem.fromUri(url)
        player.stop()
        player.setMediaItem(mediaItem)
        player.prepare() // Nạp nhạc
        player.play()    // Phát nhạc
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }
}