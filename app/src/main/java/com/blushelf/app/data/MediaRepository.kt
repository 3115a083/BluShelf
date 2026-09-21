package com.blushelf.app.data

import androidx.compose.runtime.mutableStateListOf

class MediaRepository {
    val items = mutableStateListOf<MediaItem>()

    fun replaceAll(values: List<MediaItem>) {
        items.clear()
        items += values
    }

    fun clear() = items.clear()
}
