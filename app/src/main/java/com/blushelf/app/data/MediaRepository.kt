package com.blushelf.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaRepository(private val dao: MediaDao) {
    val items: Flow<List<MediaItem>> = dao.observeAll().map { rows -> rows.map(MediaRow::toMediaItem) }

    suspend fun replaceAll(values: List<MediaItem>) = dao.replaceAll(values)
    suspend fun add(value: MediaItem) = dao.insertMedia(value)
    suspend fun setFavorite(id: String, value: Boolean) = dao.setFavorite(id, value)
    suspend fun setPlayed(id: String, value: Boolean) = dao.setPlayed(id, value)
    suspend fun setWatchlist(id: String, value: Boolean) = dao.setWatchlist(id, value)
}

fun titleSortKey(title: String): String {
    val normalized = title.trim().lowercase()
    val articles = listOf("the ", "a ", "an ", "der ", "das ", "ein ", "eine ")
    return articles.firstOrNull(normalized::startsWith)?.let(normalized::removePrefix) ?: normalized
}
