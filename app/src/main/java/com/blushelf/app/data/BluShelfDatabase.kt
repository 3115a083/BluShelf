package com.blushelf.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [WorkEntity::class, EditionEntity::class, OwnedCopyEntity::class, ShelfEntity::class], version = 2, exportSchema = true)
abstract class BluShelfDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS shelves (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, kind TEXT NOT NULL, built_in INTEGER NOT NULL)")
                db.execSQL("INSERT OR IGNORE INTO shelves (id, name, kind, built_in) VALUES ('shelf-video', 'Video', 'VIDEO', 1)")
                db.execSQL("INSERT OR IGNORE INTO shelves (id, name, kind, built_in) VALUES ('shelf-audio', 'Audio', 'AUDIO', 1)")
                db.execSQL("ALTER TABLE owned_copies ADD COLUMN shelf_id TEXT NOT NULL DEFAULT 'shelf-video'")
                db.execSQL("UPDATE owned_copies SET shelf_id = CASE WHEN (SELECT works.kind FROM editions JOIN works ON works.id = editions.work_id WHERE editions.id = owned_copies.edition_id) = 'AUDIO' THEN 'shelf-audio' ELSE 'shelf-video' END")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_owned_copies_shelf_id ON owned_copies(shelf_id)")
            }
        }

        @Volatile private var instance: BluShelfDatabase? = null
        fun get(context: Context): BluShelfDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, BluShelfDatabase::class.java, "blushelf.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { instance = it }
        }
    }
}
