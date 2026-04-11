package com.example.smartpodcast

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.smartpodcast.service.PodcastService
import com.example.smartpodcast.ui.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Xin quyền thông báo cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        // 2. Khởi động MediaController để kết nối với PodcastService giúp hiển thị thông báo
        val sessionToken = androidx.media3.session.SessionToken(
            this,
            android.content.ComponentName(this, PodcastService::class.java)
        )
        val controllerFuture = androidx.media3.session.MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            // Không cần làm gì thêm, việc kết nối đã đủ để kích hoạt MediaSession
        }, androidx.core.content.ContextCompat.getMainExecutor(this))

        if (savedInstanceState == null) {
            // Luôn đặt HomeFragment làm nền
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()

            // Nếu khởi động thẳng từ thông báo, đẩy PlayerFragment lên trên cùng
            if (intent.getBooleanExtra("OPEN_PLAYER", false)) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, com.example.smartpodcast.ui.player.PlayerFragment())
                    .addToBackStack(null) // Cho phép bấm nút Back để quay về Trang chủ
                    .commit()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra("OPEN_PLAYER", false) == true) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            // Nếu người dùng đang không ở màn hình Player thì mới đè lên, tránh tạo ra 2 trang Player
            if (currentFragment !is com.example.smartpodcast.ui.player.PlayerFragment) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, com.example.smartpodcast.ui.player.PlayerFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}