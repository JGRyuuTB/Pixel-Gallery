package com.GalleryYou.Pixel.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

enum class HapticsLevel {
    OFF, SUBTLE, BALANCED, EXPRESSIVE;

    companion object {
        const val DEFAULT = "balanced"

        fun fromPref(value: String?): HapticsLevel = when (value) {
            "off" -> OFF
            "subtle" -> SUBTLE
            "expressive" -> EXPRESSIVE
            else -> BALANCED
        }

        fun toPref(level: HapticsLevel): String = when (level) {
            OFF -> "off"
            SUBTLE -> "subtle"
            EXPRESSIVE -> "expressive"
            BALANCED -> "balanced"
        }
    }
}

val LocalHapticsLevel = compositionLocalOf { HapticsLevel.DEFAULT }

class GalleryHaptics(
    private val feedback: HapticFeedback,
    private val level: () -> HapticsLevel,
) {
    private fun perform(type: HapticFeedbackType) {
        if (level() != HapticsLevel.OFF) {
            feedback.performHapticFeedback(type)
        }
    }

    private fun stepped(heavy: HapticFeedbackType, medium: HapticFeedbackType, light: HapticFeedbackType) {
        when (level()) {
            HapticsLevel.EXPRESSIVE -> perform(heavy)
            HapticsLevel.BALANCED -> perform(medium)
            HapticsLevel.SUBTLE -> perform(light)
            HapticsLevel.OFF -> {}
        }
    }

    private fun atLeast(minimum: HapticsLevel, type: HapticFeedbackType) {
        if (level() >= minimum) perform(type)
    }

    fun tick() = stepped(HapticFeedbackType.Confirm, HapticFeedbackType.ContextClick, HapticFeedbackType.TextHandleMove)
    fun threshold() = atLeast(HapticsLevel.BALANCED, HapticFeedbackType.GestureThresholdActivate)
    fun longPress() = stepped(HapticFeedbackType.LongPress, HapticFeedbackType.LongPress, HapticFeedbackType.ContextClick)
    fun confirm() = stepped(HapticFeedbackType.Confirm, HapticFeedbackType.Confirm, HapticFeedbackType.ContextClick)
    fun reject() = stepped(HapticFeedbackType.Reject, HapticFeedbackType.Reject, HapticFeedbackType.LongPress)
    fun toggle(on: Boolean) = stepped(
        if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
        if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
        HapticFeedbackType.ContextClick,
    )
    fun subtle() = stepped(HapticFeedbackType.ContextClick, HapticFeedbackType.ContextClick, HapticFeedbackType.TextHandleMove)

    fun performHapticFeedback(type: HapticFeedbackType) {
        when (type) {
            HapticFeedbackType.Confirm -> confirm()
            HapticFeedbackType.Reject -> reject()
            HapticFeedbackType.LongPress -> longPress()
            HapticFeedbackType.TextHandleMove, HapticFeedbackType.SegmentFrequentTick -> tick()
            HapticFeedbackType.SegmentTick, HapticFeedbackType.ContextClick -> subtle()
            HapticFeedbackType.GestureThresholdActivate -> threshold()
            HapticFeedbackType.ToggleOn -> toggle(true)
            HapticFeedbackType.ToggleOff -> toggle(false)
            else -> perform(type)
        }
    }
}

@Composable
fun rememberGalleryHaptics(): GalleryHaptics {
    val levelPref = LocalHapticsLevel.current
    val feedback = LocalHapticFeedback.current
    val currentLevel = rememberUpdatedState(levelPref)
    return remember(feedback) {
        GalleryHaptics(feedback) { HapticsLevel.fromPref(currentLevel.value) }
    }
}
