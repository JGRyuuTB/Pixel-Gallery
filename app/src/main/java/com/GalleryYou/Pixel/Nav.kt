package com.GalleryYou.Pixel

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class NavDest(@StringRes val labelRes: Int, val icon: ImageVector, val iconSelected: ImageVector)

val GalleryNavItems = listOf(
    NavDest(R.string.nav_photos, AppIcons.Image, AppIcons.ImageDuo),
    NavDest(R.string.nav_albums, AppIcons.Collections, AppIcons.CollectionsDuo),
    NavDest(R.string.nav_trash, AppIcons.Trash, AppIcons.TrashDuo),
    NavDest(R.string.nav_settings, AppIcons.Settings, AppIcons.SettingsDuo),
)

/**
 * Barra de navegación flotante de cristal, compacta, con dos estilos que se eligen en Ajustes:
 *  - [style] 0, clásica: el destino activo muestra icono y nombre dentro de su píldora.
 *  - [style] 1, apilada: icono sobre el nombre, el activo resaltado con una píldora de color.
 * Ambas admiten una insignia con número ([badges], por ejemplo la papelera) y arrastrar el dedo
 * por la barra para cambiar de pestaña. El cristal sigue el desenfoque y el Liquid Glass de Ajustes.
 */
@Composable
fun GlassNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BackdropState? = null,
    style: Int = 0,
    badges: Map<Int, Int> = emptyMap(),
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelect by rememberUpdatedState(onSelect)
    var widthPx by remember { mutableIntStateOf(0) }
    val stacked = style == 1
    val barHeight = if (stacked) 60.dp else 52.dp

    fun indexAt(x: Float): Int {
        if (widthPx <= 0) return currentSelected
        return ((x / widthPx) * items.size).toInt().coerceIn(0, items.lastIndex)
    }

    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .onSizeChanged { widthPx = it.width }
                .glassSurface(
                    state = backdrop,
                    tint = scheme.surfaceContainerHigh.copy(alpha = 0.42f),
                    fallback = scheme.surfaceContainerHigh.copy(alpha = 0.95f),
                    corner = barHeight / 2,
                )
                // Absorbe los toques en toda la cápsula: los huecos entre destinos no llegan
                // a las fotos o álbumes que hay detrás.
                .pointerInput(Unit) { detectTapGestures { } }
                // Arrastrar el dedo por la barra cambia de pestaña.
                .pointerInput(items.size) {
                    detectHorizontalDragGestures(
                        onDragStart = { pos ->
                            val i = indexAt(pos.x)
                            if (i != currentSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentOnSelect(i)
                            }
                        },
                        onHorizontalDrag = { change, _ ->
                            val i = indexAt(change.position.x)
                            if (i != currentSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                currentOnSelect(i)
                            }
                        },
                    )
                }
                .padding(5.dp),
            horizontalArrangement = if (stacked) Arrangement.spacedBy(2.dp) else Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { i, dest ->
                val click = {
                    if (i != selected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(i)
                }
                if (stacked) {
                    StackedNavItem(dest, i == selected, animations, badges[i] ?: 0, Modifier.weight(1f), click)
                } else {
                    ClassicNavItem(dest, i == selected, animations, badges[i] ?: 0, click)
                }
            }
        }
    }
}

@Composable
private fun NavBadge(count: Int, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .widthIn(min = 15.dp)
            .height(15.dp)
            .background(scheme.primary, CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (count > 99) "99+" else count.toString(),
            color = scheme.onPrimary,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

/** Estilo clásico: el activo muestra icono y nombre dentro de su propia píldora. */
@Composable
private fun ClassicNavItem(dest: NavDest, selected: Boolean, animations: Boolean, badge: Int, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colorSpec: AnimationSpec<Color> =
        if (animations) spring(stiffness = Spring.StiffnessMediumLow) else snap()
    val bg by animateColorAsState(
        targetValue = if (selected) scheme.primaryContainer else Color.Transparent,
        animationSpec = colorSpec,
        label = "navBg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        animationSpec = colorSpec,
        label = "navFg",
    )
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = if (selected) dest.iconSelected else dest.icon,
                contentDescription = stringResource(dest.labelRes),
                tint = fg,
                modifier = Modifier.size(22.dp),
            )
            if (badge > 0) NavBadge(badge, Modifier.padding(start = 12.dp))
        }
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(if (animations) 200 else 0)) + expandHorizontally(
                animationSpec = if (animations) spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow) else snap(),
            ),
            exit = fadeOut(tween(if (animations) 100 else 0)) + shrinkHorizontally(
                animationSpec = if (animations) tween(180) else snap(),
            ),
        ) {
            Text(
                stringResource(dest.labelRes),
                color = fg,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

/** Estilo apilado: icono sobre el nombre; el activo se resalta con una píldora y el icono "salta". */
@Composable
private fun StackedNavItem(
    dest: NavDest,
    selected: Boolean,
    animations: Boolean,
    badge: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val colorSpec: AnimationSpec<Color> =
        if (animations) spring(stiffness = Spring.StiffnessMediumLow) else snap()
    val bg by animateColorAsState(
        targetValue = if (selected) scheme.primaryContainer.copy(alpha = 0.88f) else Color.Transparent,
        animationSpec = colorSpec,
        label = "navBgStacked",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        animationSpec = colorSpec,
        label = "navFgStacked",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = if (animations) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) else snap(),
        label = "navIconScale",
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = if (selected) dest.iconSelected else dest.icon,
                contentDescription = stringResource(dest.labelRes),
                tint = fg,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
            )
            if (badge > 0) NavBadge(badge, Modifier.padding(start = 12.dp))
        }
        Text(
            stringResource(dest.labelRes),
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(top = 1.dp),
        )
    }
}
