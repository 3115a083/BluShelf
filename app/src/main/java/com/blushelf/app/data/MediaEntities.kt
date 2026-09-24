package com.blushelf.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "shelves")
data class ShelfEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kind: String,
    @ColumnInfo(name = "built_in") val builtIn: Boolean
)

@Entity(tableName = "works", indices = [Index("sort_key")])
data class WorkEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "original_title") val originalTitle: String,
    val kind: String,
    @ColumnInfo(name = "sort_key") val sortKey: String,
    val year: Int?
)

@Entity(
    tableName = "editions",
    foreignKeys = [ForeignKey(entity = WorkEntity::class, parentColumns = ["id"], childColumns = ["work_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("work_id"), Index("barcode")]
)
data class EditionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "work_id") val workId: String,
    val format: String,
    val barcode: String
)

@Entity(
    tableName = "owned_copies",
    foreignKeys = [ForeignKey(entity = EditionEntity::class, parentColumns = ["id"], childColumns = ["edition_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("edition_id"), Index("location"), Index("shelf_id")]
)
data class OwnedCopyEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "edition_id") val editionId: String,
    val location: String,
    val rating: Float?,
    val favorite: Boolean,
    val played: Boolean,
    @ColumnInfo(name = "in_watchlist") val inWatchlist: Boolean,
    val notes: String,
    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "shelf_id", defaultValue = "'shelf-video'") val shelfId: String
)

data class MediaRow(
    @ColumnInfo(name = "copy_id") val copyId: String,
    val title: String,
    @ColumnInfo(name = "original_title") val originalTitle: String,
    val kind: String,
    val format: String,
    val year: Int?,
    val barcode: String,
    val location: String,
    val rating: Float?,
    val favorite: Boolean,
    val played: Boolean,
    @ColumnInfo(name = "in_watchlist") val inWatchlist: Boolean,
    val notes: String,
    @ColumnInfo(name = "shelf_id") val shelfId: String
) {
    fun toMediaItem() = MediaItem(copyId, title, originalTitle, MediaKind.valueOf(kind), format, year, barcode, location, rating, favorite, played, inWatchlist, notes, shelfId)
}
