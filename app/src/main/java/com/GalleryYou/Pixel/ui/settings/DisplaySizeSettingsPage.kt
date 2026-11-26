package com.GalleryYou.Pixel.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.ui.theme.UI_SCALE_DEFAULT
import com.GalleryYou.Pixel.ui.theme.UI_SCALE_MAX
import com.GalleryYou.Pixel.ui.theme.UI_SCALE_MIN
import com.GalleryYou.Pixel.ui.theme.UI_SCALE_STEPS
import com.GalleryYou.Pixel.ui.theme.nearestUiScaleStep
import com.GalleryYou.Pixel.util.rememberGalleryHaptics
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DisplaySizeSettingsPage(
    uiScale: Float,
    onUiScaleChange: (Float) -> Unit,
    onBack: () -> Unit,
) {
    val haptics = rememberGalleryHaptics()
    var pending by remember { mutableFloatStateOf(uiScale) }
    LaunchedEffect(uiScale) { pending = uiScale }

    SettingsDetailScaffold(
        title = stringResource(R.string.sp_display_size),
        onBack = onBack,
    ) {
        item {
            SettingsSection(title = stringResource(R.string.sp_display_size_preview)) {
                SettingsCard {
                    ScalePreview(pending = pending, committed = uiScale)
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.sp_display_size_scale)) {
                SettingsCard {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        ScaleReadout(
                            pending = pending,
                            isDefault = uiScale == UI_SCALE_DEFAULT,
                            onReset = {
                                haptics.confirm()
                                pending = UI_SCALE_DEFAULT
                                onUiScaleChange(UI_SCALE_DEFAULT)
                            },
                        )
                        Spacer(Modifier.height(4.dp))
                        Slider(
                            value = pending,
                            onValueChange = { raw ->
                                val snapped = nearestUiScaleStep(raw)
                                if (snapped != pending) {
                                    pending = snapped
                                    haptics.tick()
                                }
                            },
                            onValueChangeFinished = { onUiScaleChange(pending) },
                            valueRange = UI_SCALE_MIN..UI_SCALE_MAX,
                            steps = UI_SCALE_STEPS.size - 2,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            ScaleTick(stringResource(R.string.sp_display_size_compact))
                            ScaleTick(stringResource(R.string.sp_display_size_default))
                            ScaleTick(stringResource(R.string.sp_display_size_large))
                        }
                        Spacer(Modifier.height(16.dp))
                        val presets = listOf(UI_SCALE_MIN, UI_SCALE_DEFAULT, UI_SCALE_MAX)
                        val presetLabels = listOf(
                            stringResource(R.string.sp_display_size_compact),
                            stringResource(R.string.sp_display_size_default),
                            stringResource(R.string.sp_display_size_large),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                        ) {
                            presets.forEachIndexed { index, preset ->
                                ToggleButton(
                                    checked = pending == preset,
                                    onCheckedChange = {
                                        haptics.confirm()
                                        pending = preset
                                        onUiScaleChange(preset)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        presets.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    colors = ToggleButtonDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                ) {
                                    Text(presetLabels[index], maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.sp_display_size_about)) {
                SettingsCard {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(
                            text = stringResource(R.string.sp_display_size_note_system),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.sp_display_size_note_excluded),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScaleReadout(pending: Float, isDefault: Boolean, onReset: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.FormatSize,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.sp_display_size_percent, (pending * 100f).roundToInt()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        AnimatedVisibility(
            visible = !isDefault,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            TextButton(onClick = onReset) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.sp_display_size_reset))
            }
        }
    }
}

@Composable
private fun ScaleTick(label: String) {
    Text(
        text = label,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ScalePreview(pending: Float, committed: Float) {
    val current = LocalDensity.current
    val animatedScale by animateFloatAsState(
        targetValue = pending / committed.coerceAtLeast(0.5f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scalePreview",
    )
    val previewDensity = Density(
        density = current.density * animatedScale,
        fontScale = current.fontScale,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .padding(14.dp),
    ) {
        CompositionLocalProvider(LocalDensity provides previewDensity) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.sp_display_size_preview_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = stringResource(R.string.sp_display_size_preview_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) { idx ->
                        val color = when (idx) {
                            0 -> MaterialTheme.colorScheme.primaryContainer
                            1 -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(color),
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f)),
                            )
                        }
                    }
                }
            }
        }
    }
}
