package com.project.topakuli.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.topakuli.data.local.dao.TopakuliDao
import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity

@Database(entities = [TanamanObatEntity::class, ResepEntity::class], version = 2, exportSchema = false)
abstract class TopakuliDatabase : RoomDatabase() {
    abstract fun topakuliDao(): TopakuliDao

    companion object {
        @Volatile
        private var INSTANCE: TopakuliDatabase? = null

        fun getDatabase(context: Context): TopakuliDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TopakuliDatabase::class.java,
                    "topakuli_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
