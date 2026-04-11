package com.example.smartpodcast.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.example.smartpodcast.data.local.AppDatabase
import com.example.smartpodcast.data.local.EpisodeDao
import com.example.smartpodcast.data.remote.PodcastApi
import com.example.smartpodcast.data.repository.PodcastRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

import com.example.smartpodcast.data.repository.FirebaseSyncRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setWakeMode(androidx.media3.common.C.WAKE_MODE_NETWORK)
            .setSeekForwardIncrementMs(15000)
            .setSeekBackIncrementMs(15000)
            .build()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "smart_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideEpisodeDao(db: AppDatabase): EpisodeDao = db.episodeDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // Giả danh trình duyệt để máy chủ không chặn
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    fun providePodcastApi(okHttpClient: OkHttpClient): PodcastApi {
        return Retrofit.Builder()
            .baseUrl("https://google.com/")
            .client(okHttpClient) // Sử dụng client đã có User-Agent
            .build()
            .create(PodcastApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFirebaseSyncRepository(): FirebaseSyncRepository {
        return FirebaseSyncRepository()
    }

    @Provides
    @Singleton
    fun provideRepository(
        api: PodcastApi,
        dao: EpisodeDao,
        @ApplicationContext context: Context,
        syncRepo: FirebaseSyncRepository
    ): PodcastRepository {
        return PodcastRepository(api, dao, context, syncRepo)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
        return com.google.firebase.auth.FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): com.google.firebase.firestore.FirebaseFirestore {
        return com.google.firebase.firestore.FirebaseFirestore.getInstance()
    }
}
