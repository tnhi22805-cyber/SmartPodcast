package com.example.smartpodcast.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface PodcastApi {

    @GET("search")
    suspend fun getTrendingPodcasts(
        @Query("term") term: String = "podcast",
        @Query("limit") limit: Int = 50,
        @Query("media") media: String = "podcast"
    ): ApplePodcastResponse

    @GET
    suspend fun getRawRss(@retrofit2.http.Url url: String): okhttp3.ResponseBody
}

data class ApplePodcastResponse(val results: List<ApplePodcastResult>)
data class ApplePodcastResult(
    val trackName: String?,
    val previewUrl: String?,
    val artworkUrl100: String?,
    val collectionName: String?
)



