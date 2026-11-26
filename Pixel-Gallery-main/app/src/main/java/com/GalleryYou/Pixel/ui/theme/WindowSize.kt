package com.GalleryYou.Pixel.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize

@Composable
fun windowDpSize(): DpSize {
    val px = LocalWindowInfo.current.containerSize
    return with(LocalDensity.current) { DpSize(px.width.toDp(), px.height.toDp()) }
}
