package com.convx.music.ui.component

import com.convx.music.lyrics.LyricsEntry
import com.convx.music.lyrics.WordTimestamp
import org.junit.Assert.assertEquals
import org.junit.Test

class RedThreadLyricsTest {
    @Test
    fun wordRangesKeepDevanagariGraphemesIntact() {
        val text = "दिल की बातें"
        val ranges = findRedThreadWordRanges(
            text,
            listOf(
                WordTimestamp("दिल", 0.0, 0.4),
                WordTimestamp("की", 0.4, 0.7),
                WordTimestamp("बातें", 0.7, 1.2),
            ),
        )

        assertEquals(listOf("दिल", "की", "बातें"), ranges.map {
            text.substring(it.start, it.endExclusive)
        })
        assertEquals(listOf(0, 1, 2), ranges.map { it.wordIndex })
    }

    @Test
    fun activeWordUsesExistingTimestampBounds() {
        val words = listOf(
            WordTimestamp("पहला", 1.0, 1.4),
            WordTimestamp("दूसरा", 1.5, 2.0),
        )

        assertEquals(0, redThreadActiveWordIndex(words, 1_200))
        assertEquals(-1, redThreadActiveWordIndex(words, 1_450))
        assertEquals(1, redThreadActiveWordIndex(words, 1_700))
    }

    @Test
    fun lineProgressUsesWordTimesOrTheNextLineBoundary() {
        val wordTimed = LyricsEntry(
            time = 1_000,
            text = "hello there",
            words = listOf(
                WordTimestamp("hello", 1.0, 2.0),
                WordTimestamp("there", 2.0, 3.0),
            ),
        )
        val lineTimed = LyricsEntry(time = 1_000, text = "next line")

        assertEquals(0.5f, redThreadLineProgress(wordTimed, 5_000, 2_000), 0.001f)
        assertEquals(0.5f, redThreadLineProgress(lineTimed, 5_000, 3_000), 0.001f)
    }
}
