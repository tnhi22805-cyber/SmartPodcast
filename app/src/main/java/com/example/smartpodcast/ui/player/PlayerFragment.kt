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
import com.bumptech.glide.Glide
import com.example.smartpodcast.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player) {

    private val viewModel: PlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find UI components
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlayPause)
        val btnRewind = view.findViewById<ImageButton>(R.id.btnRewind)
        val btnForward = view.findViewById<ImageButton>(R.id.btnForward)
        val tvTitle = view.findViewById<TextView>(R.id.tvPlayerTitle)
        val imgLarge = view.findViewById<ImageView>(R.id.imgLargePodcast)
        val tvTimerStatus = view.findViewById<TextView>(R.id.tvTimerStatus)
        val btnMenu = view.findViewById<ImageButton>(R.id.btnMenu)

        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val tvCurrentTime = view.findViewById<TextView>(R.id.tvCurrentTime)
        val tvTotalTime = view.findViewById<TextView>(R.id.tvTotalTime)

        val url = arguments?.getString("audioUrl") ?: ""
        val title = arguments?.getString("title") ?: "Loading..."
        val description = arguments?.getString("description") ?: ""
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        // Hiển thị ngay lập tức (phòng hờ Flow load chậm)
        if (arguments?.getString("title") != null) {
            tvTitle.text = title
            Glide.with(this).load(imageUrl).placeholder(android.R.drawable.ic_menu_report_image).into(imgLarge)
        }

        // 1. Auto play with Metadata if navigated from Home
        if (url.isNotEmpty()) {
            viewModel.playEpisode(url, title, description, imageUrl)
        }

        // 1.5 Lấy thông tin bài hát đang phát thực tế để cập nhật màn hình
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMediaItem.collectLatest { item ->
                item?.let {
                    val currentTitle = it.mediaMetadata.title?.toString() ?: title
                    val currentImgUrl = it.mediaMetadata.artworkUri?.toString() ?: imageUrl

                    tvTitle.text = currentTitle

                    // Tạo Nền và màu sắc động (Immersive UI) giống thông báo Lock Screen
                    Glide.with(this@PlayerFragment)
                        .asBitmap()
                        .load(currentImgUrl)
                        .placeholder(android.R.drawable.ic_menu_report_image)
                        .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                            override fun onResourceReady(bitmap: android.graphics.Bitmap, transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?) {
                                imgLarge.setImageBitmap(bitmap)

                                androidx.palette.graphics.Palette.from(bitmap).generate { palette ->
                                    val dominant = palette?.getDominantColor(android.graphics.Color.DKGRAY) ?: android.graphics.Color.DKGRAY
                                    val darkMuted = palette?.getDarkMutedColor(android.graphics.Color.BLACK) ?: android.graphics.Color.BLACK

                                    val gradient = android.graphics.drawable.GradientDrawable(
                                        android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                                        intArrayOf(dominant, darkMuted)
                                    )
                                    view.background = gradient

                                    // Chuyển chữ và icon sang màu trắng để nổi bật trên nền
                                    tvTitle.setTextColor(android.graphics.Color.WHITE)
                                    tvTimerStatus.setTextColor(android.graphics.Color.LTGRAY)
                                    tvCurrentTime.setTextColor(android.graphics.Color.LTGRAY)
                                    tvTotalTime.setTextColor(android.graphics.Color.LTGRAY)

                                    btnPlay.setColorFilter(android.graphics.Color.WHITE)
                                    btnRewind.setColorFilter(android.graphics.Color.WHITE)
                                    btnForward.setColorFilter(android.graphics.Color.WHITE)
                                    btnMenu.setColorFilter(android.graphics.Color.WHITE)
                                }
                            }
                            override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                        })
                }
            }
        }

        // 2. Playback controls
        btnPlay.setOnClickListener { viewModel.togglePlayPause() }
        btnRewind.setOnClickListener { viewModel.skipBackward() }
        btnForward.setOnClickListener { viewModel.skipForward() }

        // 3. Menu for Sleep Timer
        btnMenu.setOnClickListener {
            val dialog = SleepTimerDialog(
                onTimerSet = { h, m -> viewModel.startSleepTimer((h * 60 + m).toLong()) },
                onEndOfPodcast = { viewModel.startSleepTimer(1) },
                onCancelTimer = { viewModel.cancelTimer() }
            )
            dialog.show(parentFragmentManager, "SleepTimerDialog")
        }

        // 4. Seekbar interaction
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 5. Update UI from ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isPlaying.collectLatest { isPlaying ->
                btnPlay.setImageResource(if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentPosition.collectLatest { pos ->
                seekBar.progress = pos.toInt()
                tvCurrentTime.text = formatTime(pos)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.duration.collectLatest { dur ->
                seekBar.max = dur.toInt()
                tvTotalTime.text = formatTime(dur)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sleepTimerText.collectLatest { status ->
                tvTimerStatus.text = status
            }
        }

        // 6. Action Buttons
        val btnFavoritePlayer = view.findViewById<ImageButton>(R.id.btnFavoritePlayer)
        val btnDownloadPlayer = view.findViewById<ImageButton>(R.id.btnDownloadPlayer)

        btnFavoritePlayer.setOnClickListener {
            val mediaId = viewModel.currentMediaItem.value?.mediaId ?: url
            viewModel.toggleFavorite(mediaId)
        }

        btnDownloadPlayer.setOnClickListener {
            val mediaId = viewModel.currentMediaItem.value?.mediaId ?: url
            val currentTitle = viewModel.currentMediaItem.value?.mediaMetadata?.title?.toString() ?: title
            android.widget.Toast.makeText(requireContext(), "Đang bắt đầu tải...", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.downloadPodcast(mediaId, currentTitle)
        }

        // --- CẬP NHẬT TRẠNG THÁI YÊU THÍCH ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFavorite.collectLatest { isFav ->
                if (isFav) {
                    btnFavoritePlayer.setImageResource(android.R.drawable.btn_star_big_on)
                    btnFavoritePlayer.setColorFilter(android.graphics.Color.YELLOW)
                } else {
                    btnFavoritePlayer.setImageResource(android.R.drawable.btn_star)
                    btnFavoritePlayer.clearColorFilter()
                }
            }
        }

        // --- CẬP NHẬT TRẠNG THÁI TẢI XUỐNG ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isDownloadedState.collectLatest { d ->
                if (d) {
                    // Đã tải thành công -> Hiện màu xanh
                    btnDownloadPlayer.setColorFilter(android.graphics.Color.parseColor("#4CAF50"))
                } else {
                    // Chưa tải
                    btnDownloadPlayer.clearColorFilter()
                }
            }
        }

        // --- BẮT THÔNG BÁO TẢI XONG BẰNG TOAST ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.downloadStatusMsg.collectLatest { msg ->
                if (msg.isNotEmpty()) {
                    android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isDownloading.collectLatest { isDownloading ->
                if (isDownloading) {
                    // Tùy chọn: Thay đổi icon khi đang tải
                    btnDownloadPlayer.setColorFilter(android.graphics.Color.GRAY)
                }
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSec = millis / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format("%02d:%02d", m, s)
    }
}
