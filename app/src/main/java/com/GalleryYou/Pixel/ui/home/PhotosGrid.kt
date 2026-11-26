package com.GalleryYou.Pixel.ui.home

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.State
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GifBox
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.GalleryYou.Pixel.LocalThumbRegistry
import com.GalleryYou.Pixel.Motion
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.data.AppSettings
import com.GalleryYou.Pixel.data.LocalGalleryLoader
import com.GalleryYou.Pixel.data.MediaFile
import com.GalleryYou.Pixel.data.buildThumbRequest
import com.GalleryYou.Pixel.data.dayLabel
import com.GalleryYou.Pixel.data.formatDuration
import com.GalleryYou.Pixel.data.groupByDay
import com.GalleryYou.Pixel.data.rememberIsAnimated
import com.GalleryYou.Pixel.data.thumbRequest
import com.GalleryYou.Pixel.data.thumbSizeFor
import com.GalleryYou.Pixel.data.thumbUriFor
import com.GalleryYou.Pixel.popIn
import com.GalleryYou.Pixel.popOut
import com.GalleryYou.Pixel.ui.components.AmbientBackground
import com.GalleryYou.Pixel.ui.components.EmptyState
import com.GalleryYou.Pixel.ui.components.SearchEmptyState
import com.GalleryYou.Pixel.ui.components.SearchField
import com.GalleryYou.Pixel.util.rememberGalleryHaptics
import java.util.Calendar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
internal fun greetingText(): String {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val res = when (hour) {
        in 5..11 -> R.string.home_greeting_morning
        in 12..17 -> R.string.home_greeting_afternoon
        in 18..22 -> R.string.home_greeting_evening
        else -> R.string.home_greeting_night
    }
    return stringResource(res)
}

/**
 * Expressive header for the Photos feed: greeting + live item count + search toggle +
 * connected Expressive ToggleButton filter pills (All / Photos / Videos).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PhotosHomeHeader(
    showGreeting: Boolean,
    itemCount: Int,
    filterMode: Int,
    onFilterChange: (Int) -> Unit,
    searchActive: Boolean,
    searchQuery: String,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberGalleryHaptics()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (showGreeting) greetingText() else stringResource(R.string.nav_photos),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.home_collection_summary, itemCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = {
                    haptics.subtle()
                    onSearchToggle()
                },
                shapes = IconButtonDefaults.shapes(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (searchActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (searchActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                ),
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.home_search_photos))
            }
        }

        AnimatedVisibility(visible = searchActive) {
            SearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = stringResource(R.string.home_search_photos),
            )
        }

        val filters = listOf(
            Triple(0, stringResource(R.string.filter_all), Icons.Rounded.Apps),
            Triple(1, stringResource(R.string.filter_photos), Icons.Rounded.Image),
            Triple(2, stringResource(R.string.filter_videos), Icons.Rounded.Videocam),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            filters.forEachIndexed { index, (id, label, icon) ->
                val checked = filterMode == id
                ToggleButton(
                    checked = checked,
                    onCheckedChange = {
                        if (!checked) {
                            haptics.tick()
                            onFilterChange(id)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = ToggleButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun Modifier.pinchColumns(columns: Int, onChange: (Int) -> Unit): Modifier {
    val currentColumns by rememberUpdatedState(columns)
    val currentOnChange by rememberUpdatedState(onChange)
    val haptics = rememberGalleryHaptics()
    return pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var cols = currentColumns
            var acc = 1f
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.count { it.pressed } >= 2) {
                    acc *= event.calculateZoom()
                    if (acc > 1.2f && cols > 2) {
                        cols -= 1
                        haptics.tick()
                        currentOnChange(cols)
                        acc = 1f
                    } else if (acc < 0.83f && cols < 5) {
                        cols += 1
                        haptics.tick()
                        currentOnChange(cols)
                        acc = 1f
                    }
                    event.changes.forEach { it.consume() }
                }
            } while (event.changes.any { it.pressed })
        }
    }
}

@Composable
fun MediaGrid(
    items: List<MediaFile>,
    padding: PaddingValues,
    settings: AppSettings,
    selected: Set<Uri>,
    header: (@Composable () -> Unit)? = null,
    gridState: LazyGridState = rememberLazyGridState(),
    emptySearchQuery: String? = null,
    onClick: (MediaFile, Int) -> Unit,
    onLongClick: (MediaFile) -> Unit,
) {
    val groups = remember(items) { groupByDay(items) }
    val selectionMode = selected.isNotEmpty()
    val gap = settings.gap.dp
    val context = LocalContext.current
    val loader = LocalGalleryLoader.current
    val thumbPx = thumbSizeFor(settings.photoColumns)
    val todayText = stringResource(R.string.day_today)
    val yesterdayText = stringResource(R.string.day_yesterday)
    val prefetched = remember(items, thumbPx) { HashSet<Int>() }
    val seen = remember { HashSet<Uri>() }
    val bornAt = remember { System.currentTimeMillis() }
    val dayLabels = remember(groups, todayText, yesterdayText) { HashMap<Int, String>() }

    LaunchedEffect(gridState, items, thumbPx) {
        val ahead = settings.photoColumns * 4
        snapshotFlow {
            val keys = gridState.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? Int }
            if (keys.isEmpty()) null else keys.min() to keys.max()
        }.distinctUntilChanged().collectLatest { range ->
            if (range != null) {
                delay(70)
                for (i in range.first..range.second) prefetched.add(i)
                val from = (range.first - ahead).coerceAtLeast(0)
                val to = (range.second + ahead).coerceAtMost(items.lastIndex)
                for (i in range.second + 1..to) {
                    if (prefetched.add(i)) {
                        loader.enqueue(buildThumbRequest(context, thumbUriFor(items[i]), thumbPx))
                    }
                }
                for (i in range.first - 1 downTo from) {
                    if (prefetched.add(i)) {
                        loader.enqueue(buildThumbRequest(context, thumbUriFor(items[i]), thumbPx))
                    }
                }
            }
        }
    }

    var ambientUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    if (settings.ambientBg) {
        LaunchedEffect(gridState, items) {
            snapshotFlow {
                val keys = gridState.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? Int }
                if (keys.isEmpty()) emptyList<Int>() else listOf(keys.first(), keys[keys.size / 2], keys.last())
            }
                .distinctUntilChanged()
                .collectLatest { idx ->
                    if (ambientUris.isNotEmpty()) delay(300)
                    ambientUris = idx.mapNotNull { items.getOrNull(it) }.map { thumbUriFor(it) }.distinct()
                }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (settings.ambientBg) AmbientBackground(ambientUris)
        LazyVerticalGrid(
            columns = GridCells.Fixed(settings.photoColumns),
            modifier = Modifier
                .fillMaxSize()
                .pinchColumns(settings.photoColumns) { settings.updatePhotoColumns(it) },
            state = gridState,
            contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            if (header != null) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "header", contentType = "header") { header() }
            }
            if (items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    if (!emptySearchQuery.isNullOrBlank()) {
                        SearchEmptyState(
                            title = stringResource(R.string.home_search_empty_title, emptySearchQuery),
                            hint = stringResource(R.string.home_search_empty_hint),
                        )
                    } else {
                        Box(Modifier.fillMaxWidth().height(360.dp)) {
                            EmptyState(PaddingValues(0.dp))
                        }
                    }
                }
            } else {
                groups.forEach { group ->
                    val first = group.first()
                    item(span = { GridItemSpan(maxLineSpan) }, key = "day-${first.index}", contentType = "day") {
                        Text(
                            text = dayLabels.getOrPut(first.index) {
                                dayLabel(first.value.dateMillis, todayText, yesterdayText)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = (if (settings.animations) Modifier.animateItem() else Modifier)
                                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
                        )
                    }
                    items(group, key = { it.index }, contentType = { "thumb" }) { indexed ->
                        val firstTime = remember { seen.add(indexed.value.uri) }
                        MediaThumb(
                            modifier = if (settings.animations) Modifier.animateItem() else Modifier,
                            animateIn = settings.animations && firstTime && System.currentTimeMillis() - bornAt < 1500L,
                            enterDelay = (indexed.index % settings.photoColumns) * 32,
                            m = indexed.value,
                            selectionMode = selectionMode,
                            isSelected = indexed.value.uri in selected,
                            animations = settings.animations,
                            roundedCorners = settings.roundedCorners,
                            thumbPx = thumbPx,
                            onClick = { onClick(indexed.value, indexed.index) },
                            onLongClick = { onLongClick(indexed.value) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaThumb(
    m: MediaFile,
    selectionMode: Boolean,
    isSelected: Boolean,
    animations: Boolean,
    roundedCorners: Boolean,
    thumbPx: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    animateIn: Boolean = false,
    enterDelay: Int = 0,
) {
    val haptics = rememberGalleryHaptics()
    val interaction = remember { MutableInteractionSource() }
    val registry = LocalThumbRegistry.current

    val enter = remember(m.uri) { if (animateIn) Animatable(0f) else null }
    if (enter != null) {
        LaunchedEffect(enter) {
            delay(enterDelay.toLong())
            enter.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
        }
    }
    val pressAnim = remember { Animatable(1f) }
    val pressScope = rememberCoroutineScope()

    val scale: State<Float>? = if (selectionMode) {
        animateFloatAsState(
            targetValue = if (isSelected) 0.86f else 1f,
            animationSpec = if (animations) tween(Motion.Medium, easing = Motion.Emphasized) else snap(),
            label = "thumbSelect",
        )
    } else {
        null
    }
    val cornerTarget = when {
        isSelected -> 20.dp
        roundedCorners -> 16.dp
        else -> 4.dp
    }
    val corner: Dp = if (selectionMode) {
        animateDpAsState(
            targetValue = cornerTarget,
            animationSpec = if (animations) tween(Motion.Medium, easing = Motion.Emphasized) else snap(),
            label = "thumbCorner",
        ).value
    } else {
        cornerTarget
    }
    val registered = remember(m.uri) { BooleanArray(1) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onGloballyPositioned {
                if (!registered[0]) {
                    registered[0] = true
                    registry?.put(m.uri, it)
                }
            }
            .graphicsLayer {
                val e = enter?.value ?: 1f
                alpha = e
                val s = (0.84f + 0.16f * e) * pressAnim.value * (scale?.value ?: 1f)
                scaleX = s
                scaleY = s
                translationY = (1f - e) * 24.dp.toPx()
            }
            .clip(RoundedCornerShape(corner))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .pointerInput(animations) {
                if (animations) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        pressScope.launch {
                            pressAnim.animateTo(0.95f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                        }
                        waitForUpOrCancellation()
                        pressScope.launch {
                            pressAnim.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
                        }
                    }
                }
            }
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = {
                    haptics.longPress()
                    onLongClick()
                },
            ),
    ) {
        val thumbUri = remember(m.uri, m.expiresMillis) { thumbUriFor(m) }
        AsyncImage(
            model = thumbRequest(thumbUri, thumbPx),
            imageLoader = LocalGalleryLoader.current,
            contentDescription = m.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        if (m.isVideo) {
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White,
                    )
                    Text(
                        formatDuration(m.durationMs),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        if (rememberIsAnimated(m)) {
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            ) {
                Icon(
                    Icons.Rounded.GifBox,
                    contentDescription = stringResource(R.string.cd_animated_photo),
                    modifier = Modifier.padding(4.dp).size(16.dp),
                    tint = Color.White,
                )
            }
        }

        if (m.expiresMillis > 0L) {
            val days = ((m.expiresMillis - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
            ) {
                Text(
                    stringResource(R.string.days_short, days),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }

        if (selectionMode) {
            val badgeState = remember {
                androidx.compose.animation.core.MutableTransitionState(!animations).apply { targetState = true }
            }
            AnimatedVisibility(
                visibleState = badgeState,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                enter = popIn(0.4f),
                exit = popOut(0.4f),
            ) {
                val primary = MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) primary else Color.Black.copy(alpha = 0.35f))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = popIn(0.3f),
                        exit = popOut(0.3f),
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}
