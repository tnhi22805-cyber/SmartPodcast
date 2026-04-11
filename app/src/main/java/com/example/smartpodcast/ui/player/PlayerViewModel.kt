package com.example.smartpodcast.ui.player

import android.net.Uri
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.smartpodcast.data.repository.PodcastRepository
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val player: ExoPlayer,
    private val repository: PodcastRepository
) : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _isDownloadedState = MutableStateFlow(false)
    val isDownloadedState = _isDownloadedState.asStateFlow()

    private val _downloadStatusMsg = MutableStateFlow("")
    val downloadStatusMsg = _downloadStatusMsg.asStateFlow()

    private val _sleepTimerText = MutableStateFlow("Sleep Timer: Off")
    val sleepTimerText = _sleepTimerText.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem = _currentMediaItem.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    init {
        // Khởi tạo ngay trạng thái hiện tại (Đặc biệt quan trọng khi mở lại app từ thông báo)
        _isPlaying.value = player.isPlaying
        _currentMediaItem.value = player.currentMediaItem
        if (player.playbackState == Player.STATE_READY) {
            _duration.value = player.duration
            _currentPosition.value = player.currentPosition
        }

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _duration.value = player.duration
                }
            }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMediaItem.value = mediaItem
            }
        })

        // Update progress every second
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                }
                delay(1000)
            }
        }
    }

    fun playEpisode(url: String, title: String, description: String, imageUrl: String) {
        if (url.isEmpty()) return

        viewModelScope.launch {
            // Lấy trực tiếp thông tin từ Database (để xem đã tải về chưa)
            val episode = repository.getEpisodeById(url)
            
            // Xóa rào cản: LUÔN cập nhật lại trạng thái Tim và Tải kể cả khi đang phát dở
            _isFavorite.value = episode?.isFavorite ?: false
            _isDownloadedState.value = episode?.isDownloaded ?: false

            val uriToPlay = if (episode != null && episode.isDownloaded && episode.localPath != null) {
                // Đã tải về nằm trên ổ cứng điện thoại
                episode.localPath
            } else {
                // Chưa tải, nghe qua mạng Internet
                url
            }

            // Check if already playing this URL
            if (player.currentMediaItem?.localConfiguration?.uri.toString() == uriToPlay || player.currentMediaItem?.mediaId == url) {
                if (!player.isPlaying) player.play()
                return@launch
            }

            // 1. Create Metadata (Crucial for Lock Screen/Notification)
            val metadata = MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(description)
                .setArtworkUri(Uri.parse(imageUrl))
                .setIsPlayable(true)
                .build()

            // 2. Create MediaItem with Metadata
            val mediaItem = MediaItem.Builder()
                .setMediaId(url)
                .setUri(uriToPlay)
                .setMediaMetadata(metadata)
                .build()

            player.stop()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            // Ghi lại Lịch sử nghe vào Database
            repository.markListenHistory(url)
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            val newState = repository.toggleFavorite(id)
            _isFavorite.value = newState
        }
    }

    fun downloadPodcast(id: String, title: String) {
        viewModelScope.launch {
            _isDownloading.value = true
            val success = repository.downloadPodcast(id, id, title)
            
            _downloadStatusMsg.value = "" // Xóa trạng thái cũ
            delay(10) // Đợi Flow reset
            
            if (success) {
                _isDownloadedState.value = true
                _downloadStatusMsg.value = "Tải thành công! Đã lưu $title"
            } else {
                _downloadStatusMsg.value = "Lỗi tải $title. Kiểm tra mạng hoặc bật chế độ máy bay!"
            }
            _isDownloading.value = false
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun skipForward() {
        player.seekTo(player.currentPosition + 15000) // +15s
    }

    fun skipBackward() {
        player.seekTo(player.currentPosition - 15000) // -15s
    }

    fun startSleepTimer(minutes: Long) {
        startCountdown(minutes * 60 * 1000)
    }

    private fun startCountdown(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSec = millisUntilFinished / 1000
                val m = totalSec / 60
                val s = totalSec % 60
                _sleepTimerText.value = String.format("Closing in: %02d:%02d", m, s)
            }
            override fun onFinish() {
                player.pause()
                _sleepTimerText.value = "Sleep Timer: Off"
            }
        }.start()
    }

    fun cancelTimer() {
        countDownTimer?.cancel()
        _sleepTimerText.value = "Sleep Timer: Off"
    }

    override fun onCleared() {
        // We DON'T release player here because it's a Singleton shared with Service
        countDownTimer?.cancel()
        super.onCleared()
    }
}
