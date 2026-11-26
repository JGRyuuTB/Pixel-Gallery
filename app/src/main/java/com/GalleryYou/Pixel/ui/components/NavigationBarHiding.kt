package com.GalleryYou.Pixel.ui.components

import androidx.compose.material3.FloatingToolbarState

internal fun FloatingToolbarState.hiddenFraction(): Float {
    val limit = offsetLimit
    if (limit == 0f) return 0f
    return (offset / limit).coerceIn(0f, 1f)
}
