package com.example.smartpodcast.ui.home

import android.os.Bundle
import android.view.View
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

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var episodeAdapter: EpisodeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvEpisodes = view.findViewById<RecyclerView>(R.id.rvEpisodes)

        // Khởi tạo Adapter và xử lý Click
        episodeAdapter = EpisodeAdapter { episode ->
            val playerFragment = PlayerFragment().apply {
                arguments = Bundle().apply {
                    putString("audioUrl", episode.audioUrl)
                    putString("title", episode.title)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, playerFragment)
                .addToBackStack(null)
                .commit()
        }

        rvEpisodes.layoutManager = LinearLayoutManager(requireContext())
        rvEpisodes.adapter = episodeAdapter

        // Lắng nghe dữ liệu từ ViewModel để hiện lên màn hình
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.episodes.collectLatest { list ->
                episodeAdapter.updateData(list)
            }
        }
    }
}