package com.GalleryYou.Pixel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val SEGMENT_OUTER_CORNER = 16.dp
private val SEGMENT_INNER_CORNER = 4.dp

fun segmentedRowShape(
    index: Int,
    count: Int,
    continues: Boolean = false,
    rounding: Float = 0f,
): Shape {
    val r = rounding.coerceIn(0f, 1f)
    fun corner(outer: Boolean) =
        if (outer) SEGMENT_OUTER_CORNER
        else SEGMENT_INNER_CORNER + (SEGMENT_OUTER_CORNER - SEGMENT_INNER_CORNER) * r
    val top = corner(index == 0)
    val bottom = corner(index == count - 1 && !continues)
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

@Composable
fun <T> SegmentedGroup(
    items: List<T>,
    modifier: Modifier = Modifier,
    row: @Composable (index: Int, count: Int, item: T) -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        items.forEachIndexed { index, item -> row(index, items.size, item) }
    }
}
