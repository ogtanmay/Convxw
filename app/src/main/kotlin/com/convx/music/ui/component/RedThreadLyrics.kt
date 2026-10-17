/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import android.animation.ValueAnimator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.convx.music.R
import com.convx.music.lyrics.LyricsEntry
import kotlin.math.PI

private data class RedThreadFrame(
    val trackId: String,
    val lineIndex: Int,
)

@Composable
fun RedThreadLyrics(
    lines: List<LyricsEntry>,
    activeLineIndex: Int,
    playbackPositionMs: Long,
    isSynced: Boolean,
    isLoading: Boolean,
    lyricsNotFound: Boolean,
    trackId: String,
    trackTitle: String,
    trackArtist: String,
    canSeek: Boolean,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstLyricIndex = remember(lines) { lines.indexOfFirst { it.text.isNotBlank() } }
    val usableLineIndex = activeLineIndex.takeIf {
        it in lines.indices && lines[it].text.isNotBlank()
    } ?: firstLyricIndex
    val sceneIndex = usableLineIndex.coerceAtLeast(0)
    val scene = sceneIndex % 5
    val isNightScene = scene == 2 || scene == 4
    val nightPalette = isNightScene && isSynced && usableLineIndex >= 0
    val animationsEnabled = ValueAnimator.areAnimatorsEnabled()
    val ambientPhase = if (animationsEnabled) {
        rememberInfiniteTransition(label = "redThreadAmbient").animateFloat(
            initialValue = 0f,
            targetValue = (2.0 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "redThreadAmbientPhase",
        ).value
    } else {
        0f
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (nightPalette) RedThreadNight else RedThreadPaper,
        animationSpec = tween(480),
        label = "redThreadPaper",
    )
    val primaryTextColor by animateColorAsState(
        targetValue = if (nightPalette) RedThreadPaperInk else RedThreadInk,
        animationSpec = tween(360),
        label = "redThreadInk",
    )
    val secondaryTextColor = primaryTextColor.copy(alpha = 0.55f)
    val activeLine = lines.getOrNull(usableLineIndex)
    val nextLineTime = lines.getOrNull(usableLineIndex + 1)?.time
    val threadProgress = if (isSynced) {
        activeLine?.let { redThreadLineProgress(it, nextLineTime, playbackPositionMs) } ?: 0f
    } else {
        0f
    }
    val animatedThreadProgress = if (animationsEnabled) {
        animateFloatAsState(
            targetValue = threadProgress,
            animationSpec = tween(durationMillis = 140),
            label = "redThreadPlaybackProgress",
        ).value
    } else {
        threadProgress
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        RedThreadBackdrop(
            scene = scene,
            progress = animatedThreadProgress,
            hasActiveLine = activeLine != null,
            isNightScene = nightPalette,
            animationPhase = ambientPhase,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 25.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "THE RED THREAD",
                        color = primaryTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.2.sp,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = trackArtist.ifBlank { trackTitle },
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                        letterSpacing = 0.3.sp,
                        maxLines = 1,
                    )
                }
                Text(
                    text = if (usableLineIndex >= 0) "%02d".format(usableLineIndex) else "—",
                    color = RedThreadVermilion,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            if (isLoading || lyricsNotFound || usableLineIndex < 0 || activeLine == null) {
                RedThreadEmptyState(
                    title = stringResource(
                        when {
                            isLoading -> R.string.red_thread_loading_title
                            lyricsNotFound -> R.string.red_thread_no_lyrics_title
                            else -> R.string.red_thread_waiting_title
                        }
                    ),
                    detail = stringResource(
                        when {
                            isLoading -> R.string.red_thread_loading_detail
                            lyricsNotFound -> R.string.red_thread_no_lyrics_detail
                            else -> R.string.red_thread_waiting_detail
                        }
                    ),
                    modifier = Modifier.weight(1f),
                )
            } else if (!isSynced) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "UNTIMED PAGE",
                        color = RedThreadVermilion,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.red_thread_unsynced_title),
                        color = RedThreadInk,
                        fontFamily = FontFamily.Serif,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = activeLine.text,
                        color = RedThreadInk.copy(alpha = 0.62f),
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.red_thread_unsynced_detail),
                        color = RedThreadInk.copy(alpha = 0.58f),
                        fontSize = 13.sp,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = when (scene % 3) {
                        0 -> Alignment.CenterStart
                        1 -> Alignment.Center
                        else -> Alignment.CenterEnd
                    },
                ) {
                    AnimatedContent(
                        targetState = RedThreadFrame(trackId, usableLineIndex),
                        transitionSpec = {
                            if (!animationsEnabled) {
                                EnterTransition.None togetherWith ExitTransition.None
                            } else {
                                val forward = targetState.lineIndex >= initialState.lineIndex
                                (fadeIn(tween(380)) + slideInVertically(
                                    animationSpec = spring(
                                        dampingRatio = 0.72f,
                                        stiffness = Spring.StiffnessMediumLow,
                                    ),
                                ) { height -> if (forward) height / 6 else -height / 6 }) togetherWith
                                    (fadeOut(tween(210)) + slideOutVertically(
                                        animationSpec = tween(230),
                                    ) { height -> if (forward) -height / 12 else height / 12 })
                            }
                        },
                        label = "redThreadLyricScene",
                    ) { frame ->
                        val entry = lines.getOrNull(frame.lineIndex)
                        if (entry != null) {
                            val ranges = remember(entry) {
                                findRedThreadWordRanges(entry.text, entry.words.orEmpty())
                            }
                            val annotatedLyric = remember(entry, ranges, playbackPositionMs, nightPalette) {
                                redThreadAnnotatedLyric(
                                    entry = entry,
                                    ranges = ranges,
                                    positionMs = playbackPositionMs,
                                    isNightScene = nightPalette,
                                )
                            }
                            val textSize = when {
                                entry.text.length > 54 -> 30.sp
                                entry.text.length > 35 -> 36.sp
                                else -> 44.sp
                            }
                            val textAlign = when (scene % 3) {
                                0 -> TextAlign.Start
                                1 -> TextAlign.Center
                                else -> TextAlign.End
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .rotate(if (scene == 1) -1.2f else if (scene == 4) 0.8f else 0f)
                                    .clickable(enabled = canSeek) { onSeek(entry.time) }
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = when (textAlign) {
                                    TextAlign.Start -> Alignment.Start
                                    TextAlign.End -> Alignment.End
                                    else -> Alignment.CenterHorizontally
                                },
                            ) {
                                Text(
                                    text = when (scene) {
                                        0 -> "FIELD NOTES  /  ${frame.lineIndex.toString().padStart(2, '0')}"
                                        1 -> "A LINE HELD IN THE AIR"
                                        2 -> "NIGHT RECORD  /  ${frame.lineIndex.toString().padStart(2, '0')}"
                                        3 -> "INK ON PAPER"
                                        else -> "THE QUESTION RETURNS"
                                    },
                                    color = RedThreadVermilion,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.1.sp,
                                )
                                Spacer(Modifier.height(17.dp))
                                Text(
                                    text = annotatedLyric,
                                    color = primaryTextColor,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = textSize,
                                    lineHeight = textSize * 1.13f,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = if (scene == 1) FontStyle.Italic else FontStyle.Normal,
                                    textAlign = textAlign,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = trackTitle.ifBlank { "CONVX  ·  LYRIC STUDY" },
                    color = secondaryTextColor,
                    fontSize = 10.sp,
                    letterSpacing = 0.6.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = if (activeLine?.words?.isNotEmpty() == true) {
                        "WORD-SYNCED"
                    } else if (isSynced) {
                        "LINE-SYNCED"
                    } else {
                        "NO TIMING"
                    },
                    color = secondaryTextColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

@Composable
private fun RedThreadEmptyState(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "THE PAGE IS OPEN",
            color = RedThreadVermilion,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            color = RedThreadInk,
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 38.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = detail,
            color = RedThreadInk.copy(alpha = 0.62f),
            fontSize = 14.sp,
            lineHeight = 21.sp,
        )
    }
}
