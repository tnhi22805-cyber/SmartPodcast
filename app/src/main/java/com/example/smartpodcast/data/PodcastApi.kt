package com.example.smartpodcast.data
import retrofit2.http.GET
import retrofit2.http.Url

interface PodcastApi {
    @GET
    suspend fun getPodcastFeed(@Url url: String): Any // Sau này sẽ parse XML ở đây
}