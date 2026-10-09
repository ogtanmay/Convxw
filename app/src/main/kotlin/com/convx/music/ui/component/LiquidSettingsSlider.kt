package com.convx.music.ui.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.convx.music.ui.component.backdrop.catalog.components.LiquidSlider
import kotlin.math.roundToInt

/**
 * Settings slider that uses the vendored AndroidLiquidGlass control when glass is
 * enabled, while preserving the standard Material slider on unsupported devices.
 */
@Composable
fun LiquidSettingsSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
) {
    val config = LocalGlassEffectConfig.current
    val useLiquidSlider = enabled &&
        config.globalEnabled &&
        config.style == GlassStyle.LIQUID &&
        isGlassAllowed()

    val safeSteps = steps.coerceAtLeast(0)
    val safeValue = value.coerceIn(valueRange)
    val segmentCount = safeSteps + 1
    val stepSize = (valueRange.endInclusive - valueRange.start) / segmentCount

    fun snapToStep(candidate: Float): Float {
        val clamped = candidate.coerceIn(valueRange)
        if (safeSteps == 0 || stepSize <= 0f) return clamped
        val stepIndex = ((clamped - valueRange.start) / stepSize).roundToInt()
        return (valueRange.start + stepIndex * stepSize).coerceIn(valueRange)
    }

    if (!useLiquidSlider) {
        Slider(
            value = snapToStep(safeValue),
            onValueChange = { onValueChange(snapToStep(it)) },
            modifier = modifier,
            enabled = enabled,
            valueRange = valueRange,
            steps = safeSteps,
        )
        return
    }

    val backdrop = LocalAppBackdrop.current
    LiquidSlider(
        value = { snapToStep(safeValue) },
        onValueChange = { onValueChange(snapToStep(it)) },
        valueRange = valueRange,
        visibilityThreshold = ((valueRange.endInclusive - valueRange.start) / 1000f)
            .coerceAtLeast(0.0001f),
        backdrop = backdrop,
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                role = Role.Slider
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = snapToStep(safeValue),
                    range = valueRange,
                    steps = safeSteps,
                )
                setProgress { requested ->
                    onValueChange(snapToStep(requested))
                    true
                }
            },
    )
}
