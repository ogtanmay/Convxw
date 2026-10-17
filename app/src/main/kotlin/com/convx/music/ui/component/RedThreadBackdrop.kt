/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun RedThreadBackdrop(
    scene: Int,
    progress: Float,
    hasActiveLine: Boolean,
    isNightScene: Boolean,
    animationPhase: Float = 0f,
) {
    val grain = remember {
        List(112) { index ->
            Offset(
                x = ((index * 97 + 31) % 997) / 997f,
                y = ((index * 193 + 71) % 991) / 991f,
            )
        }
    }

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val inkColor = if (isNightScene) {
            RedThreadPaperInk.copy(alpha = 0.14f)
        } else {
            RedThreadInk.copy(alpha = 0.12f)
        }
        val motion = sin(animationPhase.toDouble()).toFloat()
        val counterMotion = cos(animationPhase.toDouble()).toFloat()

        grain.forEachIndexed { index, point ->
            drawCircle(
                color = inkColor.copy(alpha = if (index % 3 == 0) 0.12f else 0.07f),
                radius = if (index % 7 == 0) 1.1.dp.toPx() else 0.65.dp.toPx(),
                center = Offset(point.x * w, point.y * h),
            )
        }

        drawLine(
            color = inkColor,
            start = Offset(w * 0.075f, h * 0.13f),
            end = Offset(w * 0.925f, h * 0.13f),
            strokeWidth = 0.8.dp.toPx(),
        )
        when (scene) {
            0 -> {
                drawLine(
                    RedThreadVermilion.copy(alpha = 0.68f),
                    Offset(w * 0.075f, h * 0.17f),
                    Offset(w * 0.075f, h * 0.31f),
                    strokeWidth = 2.dp.toPx(),
                )
                drawCircle(
                    color = RedThreadVermilion.copy(alpha = 0.72f),
                    radius = w * 0.08f,
                    center = Offset(w * 0.85f, h * 0.34f),
                    style = Stroke(width = 1.2.dp.toPx()),
                )
            }
            1 -> {
                drawLine(
                    RedThreadVermilion.copy(alpha = 0.7f),
                    Offset(w * 0.78f, h * 0.2f),
                    Offset(w * 0.92f, h * 0.34f),
                    strokeWidth = 1.6.dp.toPx(),
                )
                drawLine(
                    inkColor,
                    Offset(w * 0.08f, h * 0.84f),
                    Offset(w * 0.23f, h * 0.84f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 4.dp.toPx())),
                )
            }
            2 -> {
                val orbitCenter = Offset(
                    x = w * (0.78f + 0.025f * motion),
                    y = h * (0.71f + 0.014f * counterMotion),
                )
                drawCircle(
                    color = RedThreadVermilion.copy(alpha = 0.28f),
                    radius = w * (0.26f + 0.012f * motion),
                    center = orbitCenter,
                    style = Stroke(width = 1.1.dp.toPx()),
                )
                drawCircle(
                    color = RedThreadVermilion.copy(alpha = 0.55f + 0.25f * counterMotion),
                    radius = (2.3f + 0.9f * (motion + 1f)).dp.toPx(),
                    center = orbitCenter,
                )
            }
            3 -> {
                drawLine(
                    RedThreadVermilion.copy(alpha = 0.7f),
                    Offset(w * 0.86f, h * 0.18f),
                    Offset(w * 0.86f, h * 0.3f),
                    strokeWidth = 1.dp.toPx(),
                )
                drawLine(
                    inkColor,
                    Offset(w * 0.08f, h * 0.26f),
                    Offset(w * 0.24f, h * 0.26f),
                    strokeWidth = 0.8.dp.toPx(),
                )
            }
            else -> {
                drawLine(
                    RedThreadVermilion.copy(alpha = 0.68f),
                    Offset(w * 0.1f, h * 0.31f),
                    Offset(w * 0.25f, h * 0.2f),
                    strokeWidth = 1.8.dp.toPx(),
                )
                drawLine(
                    inkColor,
                    Offset(w * 0.78f, h * 0.24f),
                    Offset(w * 0.9f, h * 0.24f),
                    strokeWidth = 0.8.dp.toPx(),
                )
            }
        }

        val y = h * (0.79f + 0.007f * motion)
        val threadPoints = listOf(
            Offset(w * 0.075f, y),
            Offset(w * 0.31f, y - h * 0.018f),
            Offset(w * 0.57f, y + h * 0.012f),
            Offset(w * 0.925f, y - h * 0.008f),
        )
        val threadPath = Path().apply {
            moveTo(threadPoints.first().x, threadPoints.first().y)
            cubicTo(
                w * 0.2f, y - h * 0.04f,
                w * 0.26f, y + h * 0.03f,
                threadPoints[1].x, threadPoints[1].y,
            )
            cubicTo(
                w * 0.4f, y - h * 0.04f,
                w * 0.47f, y + h * 0.04f,
                threadPoints[2].x, threadPoints[2].y,
            )
            cubicTo(
                w * 0.72f, y - h * 0.035f,
                w * 0.82f, y + h * 0.035f,
                threadPoints[3].x, threadPoints[3].y,
            )
        }
        drawPath(
            path = threadPath,
            color = RedThreadVermilion.copy(alpha = if (hasActiveLine) 0.18f else 0.35f),
            style = Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )

        val segmentCount = threadPoints.lastIndex
        val completedSegments = (progress * segmentCount).toInt().coerceIn(0, segmentCount)
        for (segment in 0 until completedSegments) {
            drawLine(
                RedThreadVermilion,
                threadPoints[segment],
                threadPoints[segment + 1],
                strokeWidth = 2.2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
        if (completedSegments < segmentCount) {
            val localProgress = (progress * segmentCount - completedSegments).coerceIn(0f, 1f)
            val start = threadPoints[completedSegments]
            val end = threadPoints[completedSegments + 1]
            drawLine(
                RedThreadVermilion,
                start,
                Offset(
                    x = start.x + (end.x - start.x) * localProgress,
                    y = start.y + (end.y - start.y) * localProgress,
                ),
                strokeWidth = 2.2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
}
