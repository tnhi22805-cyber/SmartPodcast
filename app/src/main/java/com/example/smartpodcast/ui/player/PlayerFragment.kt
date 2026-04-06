package com.example.smartpodcast.ui.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
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
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlayPause)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val imageUrl = "https://picsum.photos/seed/${title.hashCode()}/300"

        view.findViewById<TextView>(R.id.tvPlayerTitle).text = title

        // Load ảnh bằng Glide
        val imgView = view.findViewById<ImageView>(R.id.imgLargePodcast)
        com.bumptech.glide.Glide.with(this).load(imageUrl).into(imgView)

        // Phát nhạc ngay lập tức
        viewModel.playEpisode(url)

        view.findViewById<ImageButton>(R.id.btnPlayPause).setOnClickListener {
            viewModel.togglePlayPause()
        }
// Cập nhật trạng thái nút bấm
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                btnPlay.setImageResource(if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
            }
        })

// Tua nhạc
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) player.seekTo(p.toLong())
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
    }
}