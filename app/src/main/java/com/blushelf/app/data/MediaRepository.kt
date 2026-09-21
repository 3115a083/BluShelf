package com.blushelf.app.data
import androidx.compose.runtime.mutableStateListOf
class MediaRepository {
    val items = mutableStateListOf<MediaItem>()
    fun addAll(values: List<MediaItem>) { items += values }
    fun clear() = items.clear()
}
