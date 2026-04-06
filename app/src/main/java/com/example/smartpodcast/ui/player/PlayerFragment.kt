package com.example.smartpodcast.ui.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.smartpodcast.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ... giữ các dòng import cũ ...

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private val viewModel: PlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString("audioUrl") ?: ""
        val title = arguments?.getString("title") ?: "Unknown"
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlayPause)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)

        view.findViewById<TextView>(R.id.tvPlayerTitle).text = title
        val imgView = view.findViewById<ImageView>(R.id.imgLargePodcast)
        com.bumptech.glide.Glide.with(this).load(imageUrl).into(imgView)

        // 1. Phát nhạc ngay khi vào màn hình
        viewModel.playEpisode(url)

        // 2. Click nút Play/Pause
        btnPlay.setOnClickListener { viewModel.togglePlayPause() }

        // 3. Xử lý kéo SeekBar để tua nhạc
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                if (fromUser) viewModel.seekTo(p.toLong())
            }

            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        // 4. Lắng nghe trạng thái để cập nhật Giao diện (Rất quan trọng)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isPlaying.collect { playing ->
                btnPlay.setImageResource(if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentPosition.collect { pos -> seekBar.progress = pos.toInt() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.duration.collect { dur -> if (dur > 0) seekBar.max = dur.toInt() }
        }
    }
}