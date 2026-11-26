package com.GalleryYou.Pixel.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class FloatingTopBarState {
    var hidden by mutableStateOf(false)
        internal set

    private var downTravelPx = 0f
    internal var hideAfterPx = 0f

    internal val connection = object : NestedScrollConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            val dy = consumed.y
            when {
                dy < 0f -> {
                    downTravelPx -= dy
                    if (downTravelPx >= hideAfterPx) hidden = true
                }
                dy > 0f -> {
                    downTravelPx = 0f
                    hidden = false
                }
            }
            return Offset.Zero
        }
    }

    fun show() {
        downTravelPx = 0f
        hidden = false
    }
}

@Composable
fun FloatingTopBarHost(
    scrolledPastTop: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    state: FloatingTopBarState = remember { FloatingTopBarState() },
    bar: @Composable () -> Unit,
    content: @Composable BoxScope.(barHeight: Dp) -> Unit,
) {
    val density = LocalDensity.current
    state.hideAfterPx = with(density) { HIDE_AFTER.toPx() }

    LaunchedEffect(scrolledPastTop) {
        if (!scrolledPastTop) state.show()
    }

    var barHeightPx by remember { mutableIntStateOf(0) }
    val barHeight = if (barHeightPx > 0) with(density) { barHeightPx.toDp() } else DEFAULT_BAR_HEIGHT

    val hideProgress = animateFloatAsState(
        targetValue = if (state.hidden && scrolledPastTop) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "FloatingTopBarHide",
    )
    val backdropAlpha = animateFloatAsState(
        targetValue = if (scrolledPastTop) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "FloatingTopBarBackdrop",
    )
    val backdrop = MaterialTheme.colorScheme.background
    val layoutDirection = LocalLayoutDirection.current

    Box(modifier = modifier.nestedScroll(state.connection)) {
        content(barHeight)

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = -hideProgress.value * size.height
                }
                .drawBehind {
                    val alpha = backdropAlpha.value
                    if (alpha > 0f) drawRect(backdrop.copy(alpha = alpha))
                }
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                ),
        ) {
            Box(modifier = Modifier.onSizeChanged { barHeightPx = it.height }) {
                bar()
            }
        }
    }
}

private val HIDE_AFTER = 24.dp
private val DEFAULT_BAR_HEIGHT = 76.dp
