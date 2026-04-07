package com.example.smartpodcast.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface PodcastApi {
    // Sửa lại: Dùng @GET không có đường dẫn khi kết hợp với @Url
    @GET
    suspend fun getRawRss(@Url url: String): ResponseBody
}
