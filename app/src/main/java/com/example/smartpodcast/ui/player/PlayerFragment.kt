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

        // Load Arguments
        val url = arguments?.getString("audioUrl") ?: ""
        val title = arguments?.getString("title") ?: "Loading..."
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        tvTitle.text = title
        Glide.with(this).load(imageUrl).placeholder(android.R.drawable.ic_menu_report_image).into(imgLarge)

        // 1. Auto play
        if (url.isNotEmpty()) {
            viewModel.playEpisode(url)
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
    }

    private fun formatTime(millis: Long): String {
        val totalSec = millis / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format("%02d:%02d", m, s)
    }
}
