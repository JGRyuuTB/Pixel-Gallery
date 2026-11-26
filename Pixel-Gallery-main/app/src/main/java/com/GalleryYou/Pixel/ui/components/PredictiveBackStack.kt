package com.GalleryYou.Pixel.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun PredictiveBackStack(
    childOpen: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = childOpen,
    previewable: Boolean = childOpen,
    background: @Composable () -> Unit,
    foreground: @Composable (committedByGesture: Boolean) -> Unit,
) {
    val peel = remember { Animatable(0f) }
    var isPeeling by remember { mutableStateOf(false) }
    var committed by remember { mutableStateOf(false) }
    var peelSign by remember { mutableFloatStateOf(1f) }
    var containerWidth by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    PredictiveBackHandler(enabled = enabled) { events ->
        var dragged = false
        try {
            events.collect { event ->
                if (!previewable) return@collect
                dragged = true
                isPeeling = true
                peelSign = if (event.swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f
                peel.snapTo(event.progress.coerceIn(0f, 1f))
            }
            if (dragged) {
                committed = true
                peel.animateTo(
                    targetValue = 2f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }
            onBack()
        } catch (_: CancellationException) {
            committed = false
            if (dragged) {
                scope.launch {
                    peel.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                    isPeeling = false
                }
            } else {
                isPeeling = false
            }
        }
    }

    LaunchedEffect(childOpen) {
        if (!childOpen && committed) {
            committed = false
            isPeeling = false
            peel.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerWidth = it.width.toFloat() },
    ) {
        val dragFraction = peel.value.coerceIn(0f, 1f)
        val leavingFraction = (peel.value - 1f).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isPeeling) {
                        Modifier.graphicsLayer {
                            val scale = lerp(0.94f, 1f, dragFraction)
                            scaleX = scale
                            scaleY = scale
                            translationX = -peelSign * containerWidth * 0.08f * (1f - dragFraction)
                        }
                    } else {
                        Modifier
                    }
                )
                .coveredBy(childOpen),
        ) {
            background()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isPeeling) {
                        Modifier.graphicsLayer {
                            val scale = lerp(1f, 0.90f, dragFraction)
                            scaleX = scale
                            scaleY = scale
                            translationX = peelSign * containerWidth * (0.10f * dragFraction + 0.95f * leavingFraction)
                            alpha = 1f - leavingFraction
                            shape = RoundedCornerShape(lerp(0f, 28f, dragFraction).dp)
                            clip = true
                        }
                    } else {
                        Modifier
                    }
                ),
        ) {
            foreground(committed)
        }
    }
}

internal fun Modifier.coveredBy(covered: Boolean): Modifier =
    if (!covered) {
        this
    } else {
        this
            .clearAndSetSemantics { }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                    }
                }
            }
    }
