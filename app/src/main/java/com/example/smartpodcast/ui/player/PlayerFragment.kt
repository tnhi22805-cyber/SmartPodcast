package com.example.smartpodcast.ui.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.media3.exoplayer.ExoPlayer
import com.example.smartpodcast.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    @Inject lateinit var player: ExoPlayer // Lấy cái máy phát nhạc mà Nhi đã setup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlayPause)
        val tvTitle = view.findViewById<TextView>(R.id.tvPlayerTitle)

        btnPlay.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                btnPlay.setImageResource(android.R.drawable.ic_media_play)
            } else {
                player.play()
                btnPlay.setImageResource(android.R.drawable.ic_media_pause)
            }
        }
    }
}