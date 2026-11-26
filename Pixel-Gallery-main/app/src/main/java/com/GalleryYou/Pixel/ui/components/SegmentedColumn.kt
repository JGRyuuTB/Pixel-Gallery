package com.GalleryYou.Pixel.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

val LocalInSegmentedColumn = staticCompositionLocalOf { false }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SegmentedColumn(
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentInset: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val segments = remember { mutableStateOf<List<Rect>>(emptyList()) }
    val press = remember { mutableStateMapOf<Int, Animatable<Float, AnimationVector1D>>() }
    val scope = rememberCoroutineScope()
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    fun shapeOf(index: Int, count: Int): Shape =
        segmentedRowShape(index, count, rounding = press[index]?.value ?: 0f)

    CompositionLocalProvider(LocalInSegmentedColumn provides true) {
        Layout(
            content = { SegmentedColumnScope.content() },
            modifier = modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        val index = segments.value.indexOfFirst { it.contains(down.position) }
                        if (index < 0) return@awaitEachGesture
                        val progress = press.getOrPut(index) { Animatable(0f) }
                        val pressing = scope.launch { progress.animateTo(1f, spec) }
                        val slop = viewConfiguration.touchSlop
                        while (true) {
                            val change = awaitPointerEvent(PointerEventPass.Initial)
                                .changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            if ((change.position - down.position).getDistance() > slop) break
                        }
                        pressing.cancel()
                        scope.launch { progress.animateTo(0f, spec) }
                    }
                }
                .drawBehind {
                    val rects = segments.value
                    rects.forEachIndexed { index, rect ->
                        val outline = shapeOf(index, rects.size).createOutline(rect.size, layoutDirection, this)
                        translate(rect.left, rect.top) { drawOutline(outline, containerColor) }
                    }
                },
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val inset = contentInset.roundToPx()
            val childWidth = (width - inset * 2).coerceAtLeast(0)
            val childConstraints = Constraints(
                minWidth = childWidth,
                maxWidth = childWidth,
                minHeight = 0,
                maxHeight = Constraints.Infinity,
            )
            val visible = measurables.map { it.measure(childConstraints) }.filter { it.height > 0 }
            val gap = ListItemDefaults.SegmentedGap.roundToPx()
            val height = visible.sumOf { it.height } + gap * (visible.size - 1).coerceAtLeast(0)
            var y = 0
            val rects = visible.map { placeable ->
                Rect(0f, y.toFloat(), width.toFloat(), (y + placeable.height).toFloat())
                    .also { y += placeable.height + gap }
            }
            segments.value = rects
            layout(width, height.coerceAtLeast(0)) {
                rects.forEachIndexed { index, rect ->
                    visible[index].placeWithLayer(inset, rect.top.toInt()) {
                        shape = shapeOf(index, rects.size)
                        clip = true
                    }
                }
            }
        }
    }
}

private object SegmentedColumnScope : ColumnScope {
    override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = this
    override fun Modifier.align(alignment: Alignment.Horizontal): Modifier = this
    override fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine): Modifier = this
    override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier = this
}
