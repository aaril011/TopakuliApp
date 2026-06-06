package com.project.topakuli.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopakuliDao {

    // Tanaman Obat
    @Query("SELECT * FROM tanaman_obat")
    fun getAllTanaman(): Flow<List<TanamanObatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTanaman(tanaman: List<TanamanObatEntity>)

    // Resep
    @Query("SELECT * FROM resep")
    fun getAllResep(): Flow<List<ResepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResep(resep: List<ResepEntity>)
}
