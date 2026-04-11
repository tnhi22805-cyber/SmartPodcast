package com.example.smartpodcast.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartpodcast.R
import com.example.smartpodcast.ui.adapter.EpisodeAdapter
import com.example.smartpodcast.ui.player.PlayerFragment
import com.example.smartpodcast.ui.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.widget.ImageView
import android.widget.TextView
import android.widget.ImageButton
import android.graphics.Color
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main Home screen displaying the Podcast list automatically.
 * Implements modern UI with loading states and error handling.
 */
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var rvEpisodes: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find UI components
        rvEpisodes = view.findViewById(R.id.rvEpisodes)
        progressBar = view.findViewById(R.id.progressBar)

        // 1. Initialize Adapter with a click listener to navigate to Player
        episodeAdapter = EpisodeAdapter { episode ->
            navigateToPlayer(episode.audioUrl, episode.title, episode.imageUrl)
        }

        val ivProfile = view.findViewById<android.widget.ImageView>(R.id.ivProfile)
        ivProfile.setOnClickListener {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user != null) {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Tài khoản")
                    .setMessage("Đang đăng nhập: ${user.email}\nBạn có muốn đăng xuất không?")
                    .setPositiveButton("Đăng xuất") { _, _ ->
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()

                        // Xóa sách lịch sử điều hướng (backstack) và đẩy về trang đăng nhập
                        parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, com.example.smartpodcast.ui.auth.AuthFragment())
                            .commit()
                    }
                    .setNegativeButton("Đóng", null)
                    .show()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, com.example.smartpodcast.ui.auth.AuthFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 2. Setup Filter Buttons
        setupFilterButtons(view)

        // 2.5 Setup Search Bar
        val etSearch = view.findViewById<android.widget.EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // 3. Setup RecyclerView
        rvEpisodes.layoutManager = GridLayoutManager(requireContext(), 2)
        rvEpisodes.adapter = episodeAdapter

        // 3. Setup Mini Player
        setupMiniPlayer(view)

        // 4. Collect UI States (Loading, Success, Error) from ViewModel
        observeUiStates()
    }

    private fun observeUiStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is PodcastUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        rvEpisodes.visibility = View.GONE
                        Log.d("UI_STATE", "Data is loading...")
                    }
                    is PodcastUiState.Success -> {
                        progressBar.visibility = View.GONE
                        rvEpisodes.visibility = View.VISIBLE
                        episodeAdapter.updateData(state.episodes)
                        Log.d("UI_STATE", "Data loaded successfully: ${state.episodes.size} items")
                    }
                    is PodcastUiState.Error -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                        Log.e("UI_STATE", "Error loading data: ${state.message}")
                    }
                }
            }
        }
    }

    private fun setupFilterButtons(view: View) {
        val btnAll = view.findViewById<ImageView>(R.id.btnFilterAll)
        val btnHistory = view.findViewById<ImageView>(R.id.btnFilterHistory)
        val btnFavorite = view.findViewById<ImageView>(R.id.btnFilterFavorite)
        val btnDownload = view.findViewById<ImageView>(R.id.btnFilterDownload)

        val buttons = listOf(btnAll, btnHistory, btnFavorite, btnDownload)
        val setHighlight = { activeBtn: ImageView ->
            buttons.forEach { it.setColorFilter(Color.parseColor("#888888")) }
            activeBtn.setColorFilter(Color.parseColor("#6441A5"))
        }

        btnAll.setOnClickListener {
            viewModel.setFilter(FilterType.ALL)
            setHighlight(btnAll)
        }
        btnHistory.setOnClickListener {
            viewModel.setFilter(FilterType.HISTORY)
            setHighlight(btnHistory)
            Toast.makeText(context, "Lọc Lịch Sử", Toast.LENGTH_SHORT).show()
        }
        btnFavorite.setOnClickListener {
            viewModel.setFilter(FilterType.FAVORITE)
            setHighlight(btnFavorite)
            Toast.makeText(context, "Lọc Yêu Thích", Toast.LENGTH_SHORT).show()
        }
        btnDownload.setOnClickListener {
            viewModel.setFilter(FilterType.DOWNLOAD)
            setHighlight(btnDownload)
            Toast.makeText(context, "Lọc Tập Đã Tải", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMiniPlayer(view: View) {
        val layoutMiniPlayer = view.findViewById<View>(R.id.layoutMiniPlayer)
        val ivMiniArtwork = view.findViewById<ImageView>(R.id.ivMiniArtwork)
        val tvMiniTitle = view.findViewById<TextView>(R.id.tvMiniTitle)
        val tvMiniArtist = view.findViewById<TextView>(R.id.tvMiniArtist)
        val btnMiniPlayPause = view.findViewById<ImageButton>(R.id.btnMiniPlayPause)

        layoutMiniPlayer.setOnClickListener {
            val mediaItem = playerViewModel.currentMediaItem.value ?: return@setOnClickListener
            val audioUrl = mediaItem.mediaId
            val title = mediaItem.mediaMetadata.title?.toString() ?: ""
            val imageUrl = mediaItem.mediaMetadata.artworkUri?.toString() ?: ""
            navigateToPlayer(audioUrl, title, imageUrl)
        }

        btnMiniPlayPause.setOnClickListener {
            playerViewModel.togglePlayPause()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            playerViewModel.currentMediaItem.collectLatest { mediaItem ->
                if (mediaItem != null) {
                    layoutMiniPlayer.visibility = View.VISIBLE
                    tvMiniTitle.text = mediaItem.mediaMetadata.title ?: "Unknown Title"
                    tvMiniArtist.text = mediaItem.mediaMetadata.artist ?: "Unknown Artist"
                    Glide.with(requireContext())
                        .load(mediaItem.mediaMetadata.artworkUri)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivMiniArtwork)
                } else {
                    layoutMiniPlayer.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            playerViewModel.isPlaying.collectLatest { isPlaying ->
                if (isPlaying) {
                    btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause)
                } else {
                    btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_play)
                }
            }
        }
    }

    private fun navigateToPlayer(audioUrl: String, title: String, imageUrl: String) {
        val playerFragment = PlayerFragment().apply {
            arguments = Bundle().apply {
                putString("audioUrl", audioUrl)
                putString("title", title)
                putString("imageUrl", imageUrl)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, playerFragment)
            .addToBackStack(null)
            .commit()
    }
}
