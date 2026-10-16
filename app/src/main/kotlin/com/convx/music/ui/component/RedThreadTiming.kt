/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.convx.music.lyrics.LyricsEntry
import com.convx.music.lyrics.WordTimestamp
import kotlin.math.max

internal data class RedThreadWordRange(
    val wordIndex: Int,
    val start: Int,
    val endExclusive: Int,
)

internal fun findRedThreadWordRanges(
    text: String,
    words: List<WordTimestamp>,
): List<RedThreadWordRange> {
    var searchFrom = 0
    return buildList {
        words.forEachIndexed { wordIndex, word ->
            val token = word.text.trim()
            if (token.isNotEmpty()) {
                val start = text.indexOf(token, searchFrom)
                if (start >= 0) {
                    val end = start + token.length
                    add(RedThreadWordRange(wordIndex, start, end))
                    searchFrom = end
                }
            }
        }
    }
}

internal fun redThreadActiveWordIndex(
    words: List<WordTimestamp>,
    positionMs: Long,
): Int = words.indexOfFirst { word ->
    positionMs >= (word.startTime * 1000).toLong() &&
        positionMs <= (word.endTime * 1000).toLong()
}

internal fun redThreadLineProgress(
    line: LyricsEntry,
    nextLineTimeMs: Long?,
    positionMs: Long,
): Float {
    val timedWords = line.words?.takeIf { it.isNotEmpty() }
    val start = timedWords?.firstOrNull()?.let { (it.startTime * 1000).toLong() } ?: line.time
    val end = timedWords?.lastOrNull()?.let { (it.endTime * 1000).toLong() }
        ?.takeIf { it > start }
        ?: nextLineTimeMs?.takeIf { it > start }
        ?: (start + 2_500L)
    return ((positionMs - start).toFloat() / max(1L, end - start).toFloat()).coerceIn(0f, 1f)
}

internal fun redThreadAnnotatedLyric(
    entry: LyricsEntry,
    ranges: List<RedThreadWordRange>,
    positionMs: Long,
    isNightScene: Boolean,
): AnnotatedString {
    val words = entry.words.orEmpty()
    val activeWordIndex = redThreadActiveWordIndex(words, positionMs)
    val completeColor: Color = if (isNightScene) RedThreadPaperInk else RedThreadInk
    return buildAnnotatedString {
        append(entry.text)
        ranges.forEach { range ->
            val word = words.getOrNull(range.wordIndex) ?: return@forEach
            val wordEndMs = (word.endTime * 1000).toLong()
            val wordColor = when {
                range.wordIndex == activeWordIndex -> RedThreadVermilion
                positionMs > wordEndMs -> completeColor
                else -> completeColor.copy(alpha = 0.42f)
            }
            addStyle(
                style = SpanStyle(color = wordColor),
                start = range.start,
                end = range.endExclusive,
            )
        }
    }
}
