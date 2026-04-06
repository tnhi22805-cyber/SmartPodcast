package com.example.smartpodcast.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.example.smartpodcast.data.local.AppDatabase
import com.example.smartpodcast.data.local.EpisodeDao
import com.example.smartpodcast.data.remote.PodcastApi
import com.example.smartpodcast.data.repository.PodcastRepository
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_podcast_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideEpisodeDao(db: AppDatabase): EpisodeDao {
        return db.episodeDao()
    }

    @Provides
    @Singleton
    fun providePodcastApi(): PodcastApi {
        val tikXml = TikXml.Builder()
            .exceptionOnUnreadXml(false)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://vnexpress.net/")
            .addConverterFactory(TikXmlConverterFactory.create(tikXml))
            .build()
            .create(PodcastApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(api: PodcastApi, dao: EpisodeDao): PodcastRepository {
        return PodcastRepository(api, dao)
    }
}