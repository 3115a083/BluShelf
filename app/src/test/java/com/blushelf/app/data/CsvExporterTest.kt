package com.blushelf.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {
    @Test
    fun exportedCsvRoundTripsQuotedFieldsAndState() {
        val original = MediaItem(
            title = "Title, \"quoted\"",
            kind = MediaKind.VIDEO,
            format = "UHD Blu-ray",
            year = 2024,
            location = "Room, Shelf",
            rating = 4.5f,
            favorite = true,
            played = true,
            inWatchlist = true,
            notes = "Line one\nLine two"
        )

        val csv = CsvExporter.write(listOf(original))
        val parsed = CsvImporter.parse(csv).getOrThrow().single()

        assertEquals(original.title, parsed.title)
        assertEquals(original.location, parsed.location)
        assertEquals(original.notes, parsed.notes)
        assertEquals(original.rating, parsed.rating)
        assertTrue(parsed.favorite)
        assertTrue(parsed.played)
        assertTrue(parsed.inWatchlist)
    }
}
