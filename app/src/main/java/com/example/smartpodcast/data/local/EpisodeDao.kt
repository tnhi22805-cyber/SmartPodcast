package com.example.smartpodcast.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    // Lấy tất cả tập podcast đã lưu
    @Query("SELECT * FROM episodes ORDER BY pubDate DESC")
    fun getAllEpisodes(): Flow<List<EpisodeEntity>>

    // Lấy các tập podcast đã đánh dấu yêu thích
    @Query("SELECT * FROM episodes WHERE isFavorite = 1")
    fun getFavoriteEpisodes(): Flow<List<EpisodeEntity>>

    // Lưu danh sách tập podcast mới vào (nếu trùng ID thì ghi đè)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    // Cập nhật trạng thái (ví dụ: vừa nhấn yêu thích hoặc vừa tải xong)
    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    // Xóa một tập podcast khỏi máy
    @Delete
    suspend fun deleteEpisode(episode: EpisodeEntity)
}