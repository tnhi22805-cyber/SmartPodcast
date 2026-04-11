package com.example.smartpodcast.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartpodcast.R
import com.example.smartpodcast.ui.adapter.EpisodeAdapter
import com.example.smartpodcast.ui.player.PlayerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Main Home screen displaying the Podcast list automatically.
 * Implements modern UI with loading states and error handling.
 */
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
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

        // 2. Setup RecyclerView
        rvEpisodes.layoutManager = LinearLayoutManager(requireContext())
        rvEpisodes.adapter = episodeAdapter

        // 3. Collect UI States (Loading, Success, Error) from ViewModel
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
