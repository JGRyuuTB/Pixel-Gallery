package com.GalleryYou.Pixel.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.abs

class NavBarScrubState {
    private var bar: LayoutCoordinates? = null
    private val items = mutableMapOf<Int, LayoutCoordinates>()

    internal var tabIds: List<Int> = emptyList()
    internal var onHoverChange: () -> Unit = {}
    internal var onCommit: (Int) -> Unit = {}

    var hoveredTab by mutableStateOf<Int?>(null)
        private set

    internal fun registerBar(coordinates: LayoutCoordinates) {
        bar = coordinates
    }

    internal fun registerItem(tabId: Int, coordinates: LayoutCoordinates) {
        items[tabId] = coordinates
    }

    internal fun tabAt(position: Float, vertical: Boolean = false): Int? {
        val bar = bar?.takeIf { it.isAttached } ?: return null
        return tabIds.mapNotNull { id ->
            val item = items[id]?.takeIf { it.isAttached } ?: return@mapNotNull null
            val centre = bar.localBoundingBoxOf(item).center
            id to abs((if (vertical) centre.y else centre.x) - position)
        }.minByOrNull { it.second }?.first
    }

    internal fun hover(tabId: Int?) {
        hoveredTab = tabId
    }
}

@Composable
fun rememberNavBarScrubState(
    tabIds: List<Int>,
    onHoverChange: () -> Unit,
    onCommit: (Int) -> Unit,
): NavBarScrubState = remember { NavBarScrubState() }.also {
    it.tabIds = tabIds
    it.onHoverChange = onHoverChange
    it.onCommit = onCommit
}

fun Modifier.navBarScrubItem(state: NavBarScrubState, tabId: Int): Modifier =
    onGloballyPositioned { state.registerItem(tabId, it) }

fun Modifier.navBarScrub(state: NavBarScrubState, vertical: Boolean = false): Modifier = this
    .onGloballyPositioned { state.registerBar(it) }
    .pointerInput(state, vertical) {
        if (vertical) {
            detectVerticalDragGestures(
                onDragStart = { offset -> state.hover(state.tabAt(offset.y, vertical = true)) },
                onDragEnd = {
                    val target = state.hoveredTab
                    state.hover(null)
                    if (target != null) state.onCommit(target)
                },
                onDragCancel = { state.hover(null) },
                onVerticalDrag = { change, _ ->
                    change.consume()
                    val target = state.tabAt(change.position.y, vertical = true)
                    if (target != null && target != state.hoveredTab) {
                        state.hover(target)
                        state.onHoverChange()
                    }
                },
            )
            return@pointerInput
        }
        detectHorizontalDragGestures(
            onDragStart = { offset -> state.hover(state.tabAt(offset.x)) },
            onDragEnd = {
                val target = state.hoveredTab
                state.hover(null)
                if (target != null) state.onCommit(target)
            },
            onDragCancel = { state.hover(null) },
            onHorizontalDrag = { change, _ ->
                change.consume()
                val target = state.tabAt(change.position.x)
                if (target != null && target != state.hoveredTab) {
                    state.hover(target)
                    state.onHoverChange()
                }
            },
        )
    }
