package com.example.smartpodcast.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.CommandButton
import com.google.common.collect.ImmutableList
import com.example.smartpodcast.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi // Thêm ở đây để bao phủ toàn bộ class, sửa lỗi dòng 38, 39
@AndroidEntryPoint
class PodcastService : MediaSessionService() {

    @Inject lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        // 1. Cấu hình Audio Attributes
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player.setAudioAttributes(audioAttributes, true)
        player.setHandleAudioBecomingNoisy(true)

        // 2. Tạo Intent mở App (Mở thẳng màn hình Podcast)
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("OPEN_PLAYER", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Sử dụng ForwardingPlayer để ép hệ thống hiểu Tua 15s luôn khả dụng
        val forwardingPlayer = object : ForwardingPlayer(player) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .remove(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_BACK)
                    .add(Player.COMMAND_SEEK_FORWARD)
                    .build()
            }

            override fun isCommandAvailable(command: Int): Boolean {
                return when (command) {
                    Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_SEEK_TO_NEXT -> false
                    // Quan trọng: Ép luôn luôn True để Màn hình khóa không bị mất nút lúc đệm nhạc
                    Player.COMMAND_SEEK_BACK, Player.COMMAND_SEEK_FORWARD -> true
                    else -> super.isCommandAvailable(command)
                }
            }

            // Xử lý an toàn để tránh crash khi bấm tua lúc chưa load nhạc xong
            override fun seekBack() {
                if (player.isCommandAvailable(Player.COMMAND_SEEK_BACK)) {
                    player.seekBack()
                }
            }

            override fun seekForward() {
                if (player.isCommandAvailable(Player.COMMAND_SEEK_FORWARD)) {
                    player.seekForward()
                }
            }

            // Xử lý tai nghe Bluetooth
            override fun seekToNext() { seekForward() }
            override fun seekToPrevious() { seekBack() }
        }

        // 4. Khởi tạo MediaSession
        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setSessionActivity(pendingIntent) // Quan trọng để hiện thông báo
            .build()

        // 5. Ép thanh thông báo ưu tiên hiển thị Lùi 15s và Tua 15s (Thay vì mặc định là Chuyển Bài)
        setMediaNotificationProvider(object : DefaultMediaNotificationProvider(this) {
            override fun getMediaButtons(
                session: MediaSession,
                playerCommands: Player.Commands,
                customLayout: ImmutableList<CommandButton>,
                showPauseButton: Boolean
            ): ImmutableList<CommandButton> {
                val rewindBtn = CommandButton.Builder()
                    .setPlayerCommand(Player.COMMAND_SEEK_BACK)
                    .setIconResId(android.R.drawable.ic_media_rew)
                    .setDisplayName("Lùi 15s")
                    .build()
                val playPauseBtn = CommandButton.Builder()
                    .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                    .setIconResId(if (showPauseButton) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
                    .setDisplayName(if (showPauseButton) "Dừng" else "Phát")
                    .build()
                val fastForwardBtn = CommandButton.Builder()
                    .setPlayerCommand(Player.COMMAND_SEEK_FORWARD)
                    .setIconResId(android.R.drawable.ic_media_ff)
                    .setDisplayName("Tua 15s")
                    .build()
                return ImmutableList.of(rewindBtn, playPauseBtn, fastForwardBtn)
            }
        })
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}