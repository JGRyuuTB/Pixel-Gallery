package com.GalleryYou.Pixel.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.ui.components.LocalInSegmentedColumn
import com.GalleryYou.Pixel.ui.components.SegmentedColumn
import com.GalleryYou.Pixel.util.rememberGalleryHaptics
import kotlin.math.roundToInt

internal data class SettingsInfoData(
    val icon: ImageVector,
    val title: String,
    val text: String,
)

internal val LocalSettingsInfoSink = staticCompositionLocalOf<((SettingsInfoData) -> Unit)?> { null }
internal val LocalSettingsDetailShowsBack = compositionLocalOf { true }

@Stable
internal class SettingsJumpController {
    var pendingTitle by mutableStateOf<String?>(null)
    val targets = mutableStateMapOf<String, LayoutCoordinates>()

    fun request(title: String) {
        pendingTitle = title
    }

    fun clear() {
        pendingTitle = null
    }
}

internal val LocalSettingsJump = staticCompositionLocalOf<SettingsJumpController?> { null }

internal fun Modifier.settingsJumpTarget(title: String): Modifier = composed {
    val jump = LocalSettingsJump.current
    val highlight = remember { Animatable(0f) }
    val isTarget = jump?.pendingTitle == title
    LaunchedEffect(isTarget) {
        if (isTarget) {
            highlight.snapTo(1f)
            highlight.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
            jump?.clear()
        }
    }
    val tint = MaterialTheme.colorScheme.primary
    this
        .onGloballyPositioned { coords ->
            if (jump != null && coords.isAttached) {
                jump.targets[title] = coords
            }
        }
        .drawWithContent {
            drawContent()
            val alpha = highlight.value * 0.22f
            if (alpha > 0.01f) {
                drawRoundRect(
                    color = tint.copy(alpha = alpha),
                    cornerRadius = CornerRadius(18.dp.toPx()),
                )
            }
        }
}

@Composable
internal fun SettingsJumpEffect(
    listState: LazyListState,
    listCoordinates: () -> LayoutCoordinates?,
) {
    val jump = LocalSettingsJump.current ?: return
    val targetTitle = jump.pendingTitle ?: return
    LaunchedEffect(targetTitle) {
        val target = jump.targets[targetTitle]
        val listCoords = listCoordinates()
        if (target != null && target.isAttached && listCoords != null && listCoords.isAttached) {
            val offset = listCoords.localBoundingBoxOf(target, clipBounds = false).top
            if (offset > 0f) {
                listState.animateScrollToItem(0, offset.roundToInt())
            }
        }
    }
}

@Composable
internal fun SettingsInfoDialog(
    info: SettingsInfoData,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = info.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
        },
        title = {
            Text(
                text = info.title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                text = info.text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.si_got_it), fontWeight = FontWeight.SemiBold)
            }
        },
    )
}

object SettingsRowDefaults {
    val destructiveTint: Color
        @Composable get() = MaterialTheme.colorScheme.error
}

@Composable
internal fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 10.dp),
        )
        content()
    }
}

@Composable
internal fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    SegmentedColumn(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentInset = 6.dp,
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    showChevron: Boolean = false,
    explanation: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rowScale",
    )
    val haptics = rememberGalleryHaptics()
    val infoSink = LocalSettingsInfoSink.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .settingsJumpTarget(title)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.subtle()
                    onClick()
                },
                onLongClick = if (explanation != null && infoSink != null) {
                    {
                        haptics.longPress()
                        infoSink(SettingsInfoData(icon, title, explanation))
                    }
                } else null,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsRowIcon(icon = icon, tint = tint)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = titleColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary,
    explanation: String? = null,
    available: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "toggleScale",
    )
    val haptics = rememberGalleryHaptics()
    val infoSink = LocalSettingsInfoSink.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .settingsJumpTarget(title)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (available) {
                        haptics.toggle(!enabled)
                        onToggle(!enabled)
                    }
                },
                onLongClick = if (explanation != null && infoSink != null) {
                    {
                        haptics.longPress()
                        infoSink(SettingsInfoData(icon, title, explanation))
                    }
                } else null,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val contentAlpha = if (available) 1f else 0.38f
        SettingsRowIcon(icon = icon, tint = tint.copy(alpha = tint.alpha * contentAlpha))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    fontSize = 13.sp,
                )
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = {
                haptics.toggle(it)
                onToggle(it)
            },
            enabled = available,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = tint,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun settingsChoiceButtonColors(
    accent: Color = MaterialTheme.colorScheme.primary,
) = ToggleButtonDefaults.colors(
    containerColor = accent.copy(alpha = 0.1f),
    checkedContainerColor = accent,
    contentColor = MaterialTheme.colorScheme.onSurface,
    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsChoiceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
) {
    val haptics = rememberGalleryHaptics()
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .settingsJumpTarget(title)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsRowIcon(icon = icon, tint = tint)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            labels.forEachIndexed { index, label ->
                ToggleButton(
                    checked = selectedIndex == index,
                    enabled = enabled,
                    onCheckedChange = {
                        if (selectedIndex != index) {
                            haptics.confirm()
                            onSelect(index)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    colors = settingsChoiceButtonColors(tint),
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        labels.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                ) {
                    Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
internal fun SettingsSliderRow(
    icon: ImageVector,
    title: String,
    valueText: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    subtitle: String = "",
    tint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
) {
    val haptics = rememberGalleryHaptics()
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .settingsJumpTarget(title)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsRowIcon(icon = icon, tint = tint)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = valueText,
                        style = MaterialTheme.typography.labelLarge,
                        color = tint,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { raw ->
                val snapped = raw.roundToInt().coerceIn(range.first, range.last)
                if (snapped != value) {
                    haptics.tick()
                    onChange(snapped)
                }
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = tint,
                activeTrackColor = tint,
            ),
        )
    }
}

@Composable
internal fun SettingsRowIcon(
    icon: ImageVector,
    tint: Color,
    shape: Shape = RoundedCornerShape(14.dp),
) {
    val scheme = MaterialTheme.colorScheme
    val solid = tint.copy(alpha = 1f)
    val (container, glyph) = when (solid) {
        scheme.primary, scheme.secondary -> scheme.secondaryContainer to scheme.onSecondaryContainer
        scheme.tertiary -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        scheme.error -> scheme.errorContainer to scheme.onErrorContainer
        else -> solid.copy(alpha = 0.12f) to solid
    }
    val fade = tint.alpha
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(container.copy(alpha = container.alpha * fade)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = glyph.copy(alpha = glyph.alpha * fade),
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
internal fun SettingsDivider() {
    if (LocalInSegmentedColumn.current) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsHubRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary,
    iconShape: Shape = RoundedCornerShape(14.dp),
    explanation: String? = null,
    selected: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "hubScale",
    )
    val selectedFill by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "hubSelected",
    )
    val haptics = rememberGalleryHaptics()
    val infoSink = LocalSettingsInfoSink.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(selectedFill)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptics.subtle()
                    onClick()
                },
                onLongClick = if (explanation != null && infoSink != null) {
                    {
                        haptics.longPress()
                        infoSink(SettingsInfoData(icon, title, explanation))
                    }
                } else null,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsRowIcon(icon = icon, tint = tint, shape = iconShape)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}

internal val SettingsBottomClearance = 136.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsDetailScaffold(
    title: String,
    onBack: () -> Unit,
    header: (@Composable () -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    itemSpacing: Dp = 20.dp,
    content: LazyListScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                if (LocalSettingsDetailShowsBack.current) {
                    IconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        )

        if (header != null) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                header()
            }
        }

        val listCoordinates = remember { arrayOfNulls<LayoutCoordinates>(1) }
        SettingsJumpEffect(listState = listState, listCoordinates = { listCoordinates[0] })
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { listCoordinates[0] = it },
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = SettingsBottomClearance,
            ),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
        ) {
            content()
        }
    }
}
