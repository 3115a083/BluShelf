package com.blushelf.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvImporterTest {
    @Test fun validCsvParsesVideoAndAudio() {
        val items = parse("title,kind,format\nAlien,VIDEO,Blu-ray\nKind of Blue,AUDIO,Vinyl")
        assertEquals(MediaKind.VIDEO, items[0].kind)
        assertEquals(MediaKind.AUDIO, items[1].kind)
    }

    @Test fun emptyFileFails() = assertTrue(CsvImporter.parse("").isFailure)

    @Test fun missingRequiredColumnFails() = assertTrue(CsvImporter.parse("title,kind\nAlien,VIDEO").isFailure)

    @Test fun invalidKindFails() = assertTrue(CsvImporter.parse("title,kind,format\nAlien,BOOK,Blu-ray").isFailure)

    @Test fun quotedValuesAndEscapedQuotesParse() {
        val item = parse("title,kind,format,notes\n\"Once, Twice\",VIDEO,DVD,\"He said \"\"hello\"\"\"").single()
        assertEquals("Once, Twice", item.title)
        assertEquals("He said \"hello\"", item.notes)
    }

    @Test fun quotedMultilineValueParses() {
        val item = parse("title,kind,format,notes\nAlien,VIDEO,DVD,\"line one\nline two\"").single()
        assertEquals("line one\nline two", item.notes)
    }

    @Test fun emptyOptionalValuesRemainEmpty() {
        val item = parse("title,kind,format,year,rating,notes\nAlien,VIDEO,DVD,,,").single()
        assertNull(item.year)
        assertNull(item.rating)
        assertEquals("", item.notes)
    }

    @Test fun ratingsFromHalfToFiveAreAccepted() {
        val items = parse("title,kind,format,rating\nLow,VIDEO,DVD,0.5\nHigh,AUDIO,CD,5")
        assertEquals(0.5f, items[0].rating)
        assertEquals(5f, items[1].rating)
    }

    @Test fun invalidRatingsFail() {
        assertTrue(CsvImporter.parse("title,kind,format,rating\nAlien,VIDEO,DVD,5.5").isFailure)
        assertTrue(CsvImporter.parse("title,kind,format,rating\nAlien,VIDEO,DVD,1.2").isFailure)
        assertTrue(CsvImporter.parse("title,kind,format,rating\nAlien,VIDEO,DVD,great").isFailure)
    }

    @Test fun germanAndEnglishBooleansParse() {
        val items = parse("title,kind,format,favorite,played\nA,VIDEO,DVD,ja,nein\nB,AUDIO,CD,true,false")
        assertTrue(items[0].favorite)
        assertFalse(items[0].played)
        assertTrue(items[1].favorite)
        assertFalse(items[1].played)
    }

    @Test fun malformedQuotesFail() = assertTrue(CsvImporter.parse("title,kind,format\n\"Alien,VIDEO,DVD").isFailure)

    private fun parse(csv: String) = CsvImporter.parse(csv).getOrThrow()
}
