package com.example.smartpodcast.data.remote

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface PodcastApi {
    @GET
    suspend fun getRawRss(@Url url: String): ResponseBody
}