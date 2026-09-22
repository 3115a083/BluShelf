package com.blushelf.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WorkEntity::class, EditionEntity::class, OwnedCopyEntity::class], version = 1, exportSchema = true)
abstract class BluShelfDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile private var instance: BluShelfDatabase? = null
        fun get(context: Context): BluShelfDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, BluShelfDatabase::class.java, "blushelf.db")
                .build().also { instance = it }
        }
    }
}
