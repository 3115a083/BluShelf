package com.blushelf.app.data

import java.util.UUID

enum class MediaKind { VIDEO, AUDIO }

data class MediaItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val originalTitle: String = "",
    val kind: MediaKind,
    val format: String,
    val year: Int? = null,
    val barcode: String = "",
    val location: String = "",
    val rating: Float? = null,
    val favorite: Boolean = false,
    val played: Boolean = false,
    val inWatchlist: Boolean = false,
    val notes: String = "",
    val shelfId: String = if (kind == MediaKind.VIDEO) "shelf-video" else "shelf-audio"
)
