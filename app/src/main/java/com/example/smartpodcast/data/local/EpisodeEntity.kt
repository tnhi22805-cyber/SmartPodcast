package com.example.smartpodcast.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes", primaryKeys = ["id", "userId"])
data class EpisodeEntity(
    val id: String, // ID duy nhất lấy từ link nhạc
    val userId: String = "", // Khóa sở hữu User
    val title: String,
    val description: String,
    val audioUrl: String,
    val imageUrl: String,
    val pubDate: String,
    val isFavorite: Boolean = false, // Đánh dấu yêu thích
    val isDownloaded: Boolean = false, // Đánh dấu đã tải về máy chưa
    val localPath: String? = null, // Đường dẫn lưu file mp3 trong máy
    val lastListenedAt: Long = 0L // Thời điểm nghe gần nhất (Lịch sử)
)