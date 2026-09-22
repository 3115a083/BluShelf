package com.blushelf.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleSortKeyTest {
    @Test fun ignoresCommonLeadingArticles() {
        assertEquals("matrix", titleSortKey("The Matrix"))
        assertEquals("pate", titleSortKey("Der Pate"))
        assertEquals("clockwork orange", titleSortKey("A Clockwork Orange"))
    }

    @Test fun doesNotMistakeEnglishDieForGermanArticle() {
        assertEquals("die hard", titleSortKey("Die Hard"))
    }
}
