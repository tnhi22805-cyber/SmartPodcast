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

        // 1. Khởi tạo Adapter
        episodeAdapter = EpisodeAdapter { episode ->

val playerFragment = PlayerFragment().apply {
    arguments = Bundle().apply {
        putString("audioUrl", episode.audioUrl)
        putString("title", episode.title)
        putString("imageUrl", episode.imageUrl) // Truyền thêm ảnh
    }
}
// Chuyển màn hình
parentFragmentManager.beginTransaction()
    .replace(R.id.fragment_container, playerFragment)
    .addToBackStack(null)
    .commit()

            android.util.Log.d("DEBUG_CLICK", "Bạn vừa nhấn vào: ${episode.title}")
}

// 2. Thiết lập RecyclerView
rvEpisodes.layoutManager = LinearLayoutManager(requireContext())
rvEpisodes.adapter = episodeAdapter

// 3. Lắng nghe dữ liệu (Chỉ cần 1 khối duy nhất như thế này)
viewLifecycleOwner.lifecycleScope.launch {
viewModel.episodes.collectLatest { list ->
    // Thêm Log ở đây để kiểm tra dữ liệu trong Logcat
    android.util.Log.d("DEBUG_DATA", "Danh sách podcast có: ${list.size} tập")

    // Cập nhật lên màn hình
    episodeAdapter.updateData(list)
}
}
}
}