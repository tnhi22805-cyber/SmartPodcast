package com.example.smartpodcast.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface PodcastApi {
    @GET("search")
    suspend fun getTrendingPodcasts(
        @Query("term") term: String = "podcast",
        @Query("limit") limit: Int = 50, // Lấy hẳn 50 bài cho "sướng"
        @Query("media") media: String = "podcast"
    ): ApplePodcastResponse
}

data class ApplePodcastResponse(val results: List<ApplePodcastResult>)
data class ApplePodcastResult(
    val trackName: String?,
    val previewUrl: String?,      // Link nhạc MP3 chuẩn Apple
    val artworkUrl100: String?,   // Link ảnh bìa cực nét
    val collectionName: String?   // Mô tả tập nhạc
)