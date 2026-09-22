package com.blushelf.app.data

object CsvExporter {
    private val headers = listOf(
        "title", "original_title", "kind", "format", "year", "barcode",
        "location", "rating", "favorite", "played", "watchlist", "notes"
    )

    fun write(items: List<MediaItem>): String = buildString {
        appendLine(headers.joinToString(","))
        items.forEach { item ->
            appendLine(
                listOf(
                    item.title,
                    item.originalTitle,
                    item.kind.name,
                    item.format,
                    item.year?.toString().orEmpty(),
                    item.barcode,
                    item.location,
                    item.rating?.let { value -> if (value % 1f == 0f) value.toInt().toString() else value.toString() }.orEmpty(),
                    item.favorite.toString(),
                    item.played.toString(),
                    item.inWatchlist.toString(),
                    item.notes
                ).joinToString(",") { escape(it) }
            )
        }
    }

    private fun escape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + escaped + "\"" else escaped
    }
}
