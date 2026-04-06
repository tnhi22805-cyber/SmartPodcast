package com.example.smartpodcast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartpodcast.ui.home.HomeFragment // Dòng này sẽ hết lỗi đỏ
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Bắt buộc phải có dòng này để Hilt chạy được
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
}