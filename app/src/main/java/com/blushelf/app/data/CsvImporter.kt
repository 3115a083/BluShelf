package com.blushelf.app.data

object CsvImporter {
    fun parse(text: String): Result<List<MediaItem>> = runCatching {
        val lines = text.lineSequence().filter { it.isNotBlank() }.toList()
        require(lines.isNotEmpty()) { "CSV is empty" }
        val headers = row(lines.first()).map { it.trim().lowercase() }
        require(listOf("title", "kind", "format").all(headers::contains)) { "Required columns: title, kind, format" }
        lines.drop(1).mapIndexed { n, line ->
            val values = row(line)
            require(values.size <= headers.size) { "Invalid row " + (n + 2) }
            val data = headers.mapIndexed { i, key -> key to values.getOrElse(i) { "" }.trim() }.toMap()
            val title = data["title"].orEmpty()
            require(title.isNotBlank()) { "Missing title in row " + (n + 2) }
            MediaItem(
                title = title,
                originalTitle = data["original_title"].orEmpty(),
                kind = when (data["kind"]?.uppercase()) {
                    "VIDEO" -> MediaKind.VIDEO
                    "AUDIO" -> MediaKind.AUDIO
                    else -> error("Invalid kind in row " + (n + 2))
                },
                format = data["format"].orEmpty().also { require(it.isNotBlank()) },
                year = data["year"]?.toIntOrNull(),
                barcode = data["barcode"].orEmpty(),
                location = data["location"].orEmpty(),
                rating = data["rating"]?.replace(',', '.')?.toFloatOrNull()?.takeIf { it in 0.5f..5f },
                favorite = bool(data["favorite"]),
                played = bool(data["played"]),
                notes = data["notes"].orEmpty()
            )
        }
    }

    private fun bool(value: String?) = when (value?.trim()?.lowercase()) {
        "true", "1", "yes", "ja" -> true
        else -> false
    }

    private fun row(line: String): List<String> {
        val result = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            when {
                line[i] == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> { cell.append('"'); i++ }
                line[i] == '"' -> quoted = !quoted
                line[i] == ',' && !quoted -> { result += cell.toString(); cell.clear() }
                else -> cell.append(line[i])
            }
            i++
        }
        require(!quoted) { "Unclosed quote" }
        result += cell.toString()
        return result
    }
}
