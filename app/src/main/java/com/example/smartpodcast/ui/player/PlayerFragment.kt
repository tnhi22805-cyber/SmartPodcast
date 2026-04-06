package com.example.smartpodcast.ui.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.exoplayer.ExoPlayer
import com.example.smartpodcast.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    @Inject lateinit var player: ExoPlayer // Lấy cái máy phát nhạc mà Nhi đã setup
    private val viewModel: PlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString("audioUrl") ?: ""
        val title = arguments?.getString("title") ?: "Unknown"

        view.findViewById<TextView>(R.id.tvPlayerTitle).text = title

        // Gọi phát nhạc ngay khi vào màn hình player
        viewModel.playEpisode(url)

        view.findViewById<ImageButton>(R.id.btnPlayPause).setOnClickListener {
            viewModel.togglePlayPause()
        }
    }
}