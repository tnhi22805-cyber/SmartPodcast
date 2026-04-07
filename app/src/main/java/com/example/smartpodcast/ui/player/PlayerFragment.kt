package com.example.smartpodcast.ui.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.smartpodcast.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private val viewModel: PlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ánh xạ đúng ID từ file fragment_player.xml của bạn
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlayPause)
        val tvTitle = view.findViewById<TextView>(R.id.tvPlayerTitle)
        val imgLarge = view.findViewById<ImageView>(R.id.imgLargePodcast)

        val url = arguments?.getString("audioUrl") ?: ""
        val title = arguments?.getString("title") ?: "Đang tải..."
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        tvTitle.text = title
        Glide.with(this).load(imageUrl).placeholder(android.R.drawable.ic_menu_report_image).into(imgLarge)

        // 1. Tự động phát ngay khi vào màn hình
        if (url.isNotEmpty()) {
            viewModel.playEpisode(url)
        }

        // 2. Xử lý sự kiện bấm nút (Thêm Toast để kiểm tra)
        btnPlay.setOnClickListener {
            Toast.makeText(context, "Bạn vừa bấm nút Play/Pause", Toast.LENGTH_SHORT).show()
            viewModel.togglePlayPause()
        }

        // 3. Quan sát trạng thái nhạc để đổi ICON nút bấm (Tam giác <-> 2 gạch)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                if (isPlaying) {
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause)
                } else {
                    btnPlay.setImageResource(android.R.drawable.ic_media_play)
                }
            }
        }
    }
}