package com.project.topakuli.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.topakuli.data.local.entity.HistoryEntity
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanImageEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopakuliDao {

    // History Identifikasi
    @Query("SELECT * FROM identifikasi_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT COUNT(*) FROM identifikasi_history")
    fun getHistoryCount(): Flow<Int>

    @Query("DELETE FROM identifikasi_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM identifikasi_history")
    suspend fun deleteAllHistory()

    // Tanaman Obat
    @Query("SELECT * FROM tanaman_obat")
    fun getAllTanaman(): Flow<List<TanamanObatEntity>>

    @Query("SELECT * FROM tanaman_obat WHERE id = :id")
    suspend fun getTanamanById(id: String): TanamanObatEntity?

    @Query("SELECT * FROM tanaman_obat WHERE (nama LIKE :query OR namaDaerah LIKE :query OR namaLatin LIKE :query) AND (:onlyFavorites = 0 OR isFavorit = 1) ORDER BY nama ASC")
    fun getTanamanPaging(query: String, onlyFavorites: Boolean): PagingSource<Int, TanamanObatEntity>

    @Query("SELECT COUNT(*) FROM tanaman_obat")
    fun getTanamanCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTanaman(tanaman: List<TanamanObatEntity>)

    @Query("SELECT id FROM tanaman_obat WHERE isFavorit = 1")
    suspend fun getFavoriteTanamanIds(): List<String>

    @Update
    suspend fun updateTanaman(tanaman: TanamanObatEntity)

    @Query("DELETE FROM tanaman_obat WHERE id = :id")
    suspend fun deleteTanamanById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleTanaman(tanaman: TanamanObatEntity)

    // Resep
    @Query("SELECT * FROM resep")
    fun getAllResep(): Flow<List<ResepEntity>>

    @Query("SELECT * FROM resep")
    suspend fun getAllResepList(): List<ResepEntity>

    @Query("SELECT * FROM resep WHERE (judul LIKE :query OR kategori LIKE :query OR manfaat LIKE :query) AND (:onlySaved = 0 OR isSaved = 1) ORDER BY judul ASC")
    fun getResepPaging(query: String, onlySaved: Boolean): PagingSource<Int, ResepEntity>

    @Query("SELECT * FROM resep WHERE (judul LIKE :query OR kategori LIKE :query OR manfaat LIKE :query) AND lastReadAt IS NOT NULL ORDER BY lastReadAt DESC")
    fun getResepHistoryPaging(query: String): PagingSource<Int, ResepEntity>

    @Query("SELECT * FROM resep ORDER BY viewCount DESC LIMIT 3")
    fun getPopularResep(): Flow<List<ResepEntity>>

    @Query("SELECT COUNT(*) FROM resep")
    fun getResepCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResep(resep: List<ResepEntity>)

    @Query("SELECT id FROM resep WHERE isSaved = 1")
    suspend fun getSavedResepIds(): List<String>

    @Update
    suspend fun updateResep(resep: ResepEntity)

    @Query("UPDATE resep SET lastReadAt = NULL")
    suspend fun clearResepHistory()

    @Query("SELECT * FROM resep WHERE id = :id")
    suspend fun getResepById(id: String): ResepEntity?

    @Query("SELECT * FROM resep WHERE tanamanIds LIKE '%' || :tanamanId || '%' LIMIT 1")
    suspend fun getResepByTanamanId(tanamanId: String): ResepEntity?

    @Query("DELETE FROM resep WHERE id = :id")
    suspend fun deleteResepById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleResep(resep: ResepEntity)

    // Tanaman Images
    @Query("SELECT * FROM tanaman_images WHERE tanamanId = :tanamanId")
    fun getImagesByTanamanId(tanamanId: String): Flow<List<TanamanImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTanamanImages(images: List<TanamanImageEntity>)

    @Query("DELETE FROM tanaman_images WHERE tanamanId = :tanamanId")
    suspend fun deleteImagesByTanamanId(tanamanId: String)

    @Query("DELETE FROM tanaman_images WHERE id = :imageId")
    suspend fun deleteImageById(imageId: String)
}
