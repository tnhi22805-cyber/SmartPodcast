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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var episodeAdapter: EpisodeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvEpisodes = view.findViewById<RecyclerView>(R.id.rvEpisodes)
        episodeAdapter = EpisodeAdapter { episode ->
            // Sau này An (E) sẽ xử lý nhấn vào đây để phát nhạc
        }

        rvEpisodes.layoutManager = LinearLayoutManager(requireContext())
        rvEpisodes.adapter = episodeAdapter

        // Quan sát dữ liệu từ ViewModel để hiển thị lên màn hình
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.episodes.collect { list ->
                episodeAdapter.updateData(list)
            }
        }
    }
}