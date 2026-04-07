package com.example.smartpodcast.ui.player

import android.os.CountDownTimer
import android.util.Log
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

    private val _sleepTimerText = MutableStateFlow("Set Timer")
    val sleepTimerText = _sleepTimerText.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

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
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    /**
     * Start a sleep timer to pause playback after X minutes.
     * @param minutes duration in minutes
     */
    fun startSleepTimer(minutes: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(minutes * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                _sleepTimerText.value = String.format("%02d:%02d", sec / 60, sec % 60)
            }

            override fun onFinish() {
                player.pause()
                _sleepTimerText.value = "Timer Finished"
            }
        }.start()
    }

    override fun onCleared() {
        countDownTimer?.cancel()
        super.onCleared()
    }
}
