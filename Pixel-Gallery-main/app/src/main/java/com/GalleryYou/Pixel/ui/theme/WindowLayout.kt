package com.GalleryYou.Pixel.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class WindowLayout(val width: Dp, val height: Dp) {

    val widthClass: WindowWidthClass = when {
        width < MEDIUM_WIDTH -> WindowWidthClass.COMPACT
        width < EXPANDED_WIDTH -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.EXPANDED
    }

    val isLandscape: Boolean get() = width > height
    val isShort: Boolean get() = height < SHORT_HEIGHT
    val usesNavigationRail: Boolean get() = width >= MEDIUM_WIDTH
    val supportsTwoPanes: Boolean get() = width >= EXPANDED_WIDTH || (width >= MEDIUM_WIDTH && isLandscape)

    companion object {
        val MEDIUM_WIDTH = 600.dp
        val EXPANDED_WIDTH = 840.dp
        val SHORT_HEIGHT = 480.dp
    }
}

enum class WindowWidthClass { COMPACT, MEDIUM, EXPANDED }

@Composable
fun currentWindowLayout(): WindowLayout {
    val size = windowDpSize()
    return remember(size) { WindowLayout(size.width, size.height) }
}

@Composable
fun safeHorizontalInsets(): WindowInsets =
    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
