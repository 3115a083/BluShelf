package com.blushelf.app.data

object CsvImporter {
    fun parse(text: String): Result<List<MediaItem>> = runCatching {
        val rows = rows(text.removePrefix("\uFEFF")).filterNot { row -> row.all(String::isBlank) }
        require(rows.isNotEmpty()) { "CSV is empty" }
        val headers = rows.first().map { it.trim().lowercase() }
        require(headers.none(String::isBlank)) { "CSV contains an empty header" }
        require(headers.distinct().size == headers.size) { "CSV contains duplicate headers" }
        require(listOf("title", "kind", "format").all(headers::contains)) {
            "Required columns: title, kind, format"
        }
        rows.drop(1).mapIndexed { index, values ->
            val rowNumber = index + 2
            require(values.size <= headers.size) { "Invalid row $rowNumber" }
            val data = headers.mapIndexed { i, key -> key to values.getOrElse(i) { "" }.trim() }.toMap()
            val title = data["title"].orEmpty()
            require(title.isNotBlank()) { "Missing title in row $rowNumber" }
            MediaItem(
                title = title,
                originalTitle = data["original_title"].orEmpty(),
                kind = when (data["kind"]?.uppercase()) {
                    "VIDEO" -> MediaKind.VIDEO
                    "AUDIO" -> MediaKind.AUDIO
                    else -> error("Invalid kind in row $rowNumber")
                },
                format = data["format"].orEmpty().also {
                    require(it.isNotBlank()) { "Missing format in row $rowNumber" }
                },
                year = data["year"]?.takeIf(String::isNotBlank)?.toIntOrNull().also {
                    require(data["year"].isNullOrBlank() || it != null) { "Invalid year in row $rowNumber" }
                },
                barcode = data["barcode"].orEmpty(),
                location = data["location"].orEmpty(),
                rating = rating(data["rating"], rowNumber),
                favorite = bool(data["favorite"]),
                played = bool(data["played"]),
                notes = data["notes"].orEmpty()
            )
        }
    }

    private fun rating(value: String?, rowNumber: Int): Float? {
        if (value.isNullOrBlank()) return null
        val parsed = value.replace(',', '.').toFloatOrNull()
        require(parsed != null && parsed in 0.5f..5f && parsed * 2f % 1f == 0f) {
            "Invalid rating in row $rowNumber (expected 0.5 to 5 in half-star steps)"
        }
        return parsed
    }

    private fun bool(value: String?) = when (value?.trim()?.lowercase()) {
        "true", "1", "yes", "ja" -> true
        else -> false
    }

    private fun rows(text: String): List<List<String>> {
        if (text.isEmpty()) return emptyList()
        val result = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var quoteClosed = false
        var i = 0
        while (i < text.length) {
            val char = text[i]
            when {
                char == '"' && quoted && i + 1 < text.length && text[i + 1] == '"' -> {
                    cell.append('"')
                    i++
                }
                char == '"' && quoted -> {
                    quoted = false
                    quoteClosed = true
                }
                char == '"' && cell.isEmpty() && !quoteClosed -> quoted = true
                char == ',' && !quoted -> {
                    row += cell.toString()
                    cell.clear()
                    quoteClosed = false
                }
                (char == '\n' || char == '\r') && !quoted -> {
                    row += cell.toString()
                    cell.clear()
                    result += row.toList()
                    row.clear()
                    quoteClosed = false
                    if (char == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                }
                quoteClosed && !char.isWhitespace() -> error("Unexpected character after closing quote")
                else -> cell.append(char)
            }
            i++
        }
        require(!quoted) { "Unclosed quote" }
        if (cell.isNotEmpty() || row.isNotEmpty() || text.last() == ',') {
            row += cell.toString()
            result += row
        }
        return result
    }
}
