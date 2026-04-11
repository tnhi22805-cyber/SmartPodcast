package com.example.smartpodcast.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE userId = :userId ORDER BY pubDate DESC")
    fun getAllEpisodes(userId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getEpisodeById(id: String, userId: String): EpisodeEntity?

    // Lấy các tập podcast đã đánh dấu yêu thích
    @Query("SELECT * FROM episodes WHERE isFavorite = 1 AND userId = :userId")
    fun getFavoriteEpisodes(userId: String): Flow<List<EpisodeEntity>>

    // Lưu danh sách tập podcast mới vào (nếu trùng ID thì bỏ qua để không xóa Lịch sử/Yêu Thích)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    // Cập nhật trạng thái (ví dụ: vừa nhấn yêu thích hoặc vừa tải xong)
    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    @Delete
    suspend fun deleteEpisode(episode: EpisodeEntity)

    @Query("UPDATE episodes SET lastListenedAt = :time WHERE id = :id AND userId = :userId")
    suspend fun updateLastListenedAt(id: String, userId: String, time: Long)
}