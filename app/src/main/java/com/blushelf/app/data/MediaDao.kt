package com.blushelf.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("""
        SELECT owned_copies.id AS copy_id, works.title, works.original_title, works.kind,
               editions.format, works.year, editions.barcode, owned_copies.location,
               owned_copies.rating, owned_copies.favorite, owned_copies.played,
               owned_copies.in_watchlist, owned_copies.notes
        FROM owned_copies
        JOIN editions ON editions.id = owned_copies.edition_id
        JOIN works ON works.id = editions.work_id
        ORDER BY works.sort_key, works.title
    """)
    fun observeAll(): Flow<List<MediaRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertWork(work: WorkEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertEdition(edition: EditionEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCopy(copy: OwnedCopyEntity)
    @Query("DELETE FROM owned_copies") suspend fun clearCopies()
    @Query("DELETE FROM editions") suspend fun clearEditions()
    @Query("DELETE FROM works") suspend fun clearWorks()
    @Query("UPDATE owned_copies SET favorite = :value WHERE id = :id") suspend fun setFavorite(id: String, value: Boolean)
    @Query("UPDATE owned_copies SET played = :value WHERE id = :id") suspend fun setPlayed(id: String, value: Boolean)
    @Query("UPDATE owned_copies SET in_watchlist = :value WHERE id = :id") suspend fun setWatchlist(id: String, value: Boolean)

    @Transaction
    suspend fun replaceAll(items: List<MediaItem>) {
        clearCopies(); clearEditions(); clearWorks()
        items.forEach { insertMedia(it) }
    }

    @Transaction
    suspend fun insertMedia(item: MediaItem) {
        val workId = "work-${item.id}"
        val editionId = "edition-${item.id}"
        insertWork(WorkEntity(workId, item.title, item.originalTitle, item.kind.name, titleSortKey(item.title), item.year))
        insertEdition(EditionEntity(editionId, workId, item.format, item.barcode))
        insertCopy(OwnedCopyEntity(item.id, editionId, item.location, item.rating, item.favorite, item.played, item.inWatchlist, item.notes, System.currentTimeMillis()))
    }
}
