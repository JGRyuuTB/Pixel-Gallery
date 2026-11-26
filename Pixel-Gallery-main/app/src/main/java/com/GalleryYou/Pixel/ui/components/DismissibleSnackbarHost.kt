package com.GalleryYou.Pixel.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.GalleryYou.Pixel.util.rememberGalleryHaptics
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val DISMISS_DISTANCE = 56.dp
private const val DISMISS_VELOCITY = 600f

@Composable
fun DismissibleSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        SwipeAwaySnackbar(data)
    }
}

@Composable
private fun SwipeAwaySnackbar(data: SnackbarData) {
    val scope = rememberCoroutineScope()
    val haptics = rememberGalleryHaptics()
    val density = LocalDensity.current
    val dismissDistancePx = with(density) { DISMISS_DISTANCE.toPx() }

    val offsetX = remember(data) { Animatable(0f) }
    val offsetY = remember(data) { Animatable(0f) }
    var size by remember(data) { mutableStateOf(IntSize.Zero) }

    Snackbar(
        snackbarData = data,
        modifier = Modifier
            .onSizeChanged { size = it }
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .alpha(
                1f - (maxOf(abs(offsetX.value), abs(offsetY.value)) / (dismissDistancePx * 3f)).coerceIn(0f, 0.75f)
            )
            .semantics {
                dismiss {
                    data.dismiss()
                    true
                }
            }
            .pointerInput(data, dismissDistancePx) {
                val tracker = VelocityTracker()
                var axis = 0
                detectDragGestures(
                    onDragStart = {
                        tracker.resetTracking()
                        axis = 0
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        tracker.addPosition(change.uptimeMillis, change.position)
                        if (axis == 0 && (abs(dragAmount.x) > 1f || abs(dragAmount.y) > 1f)) {
                            axis = if (abs(dragAmount.x) >= abs(dragAmount.y)) 1 else 2
                        }
                        scope.launch {
                            when (axis) {
                                1 -> offsetX.snapTo(offsetX.value + dragAmount.x)
                                2 -> {
                                    val next = offsetY.value + dragAmount.y
                                    offsetY.snapTo(if (next < 0f) next * 0.25f else next)
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        val velocity = tracker.calculateVelocity()
                        scope.launch {
                            val dismissX = axis == 1 && (
                                abs(offsetX.value) >= dismissDistancePx || abs(velocity.x) >= DISMISS_VELOCITY
                                )
                            val dismissY = axis == 2 && (
                                offsetY.value >= dismissDistancePx || velocity.y >= DISMISS_VELOCITY
                                )
                            when {
                                dismissX -> {
                                    haptics.subtle()
                                    val sign = if (offsetX.value != 0f) if (offsetX.value > 0f) 1f else -1f else if (velocity.x > 0f) 1f else -1f
                                    offsetX.animateTo(sign * (size.width.takeIf { it > 0 } ?: 600).toFloat(), tween(160))
                                    data.dismiss()
                                }
                                dismissY -> {
                                    haptics.subtle()
                                    offsetY.animateTo((size.height.takeIf { it > 0 } ?: 200).toFloat() * 1.5f, tween(160))
                                    data.dismiss()
                                }
                                else -> {
                                    launch { offsetX.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
                                    launch { offsetY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            launch { offsetX.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
                            launch { offsetY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
                        }
                    },
                )
            },
    )
}
