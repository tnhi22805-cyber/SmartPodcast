package com.example.smartpodcast.service

import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSession
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PodcastService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
            .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(androidx.media3.common.C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // Tự dừng khi có cuộc gọi
            .setHandleAudioBecomingNoisy(true) // Tự dừng khi rút tai nghe
            .build()

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}