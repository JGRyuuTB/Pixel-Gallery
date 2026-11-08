package com.GalleryYou.Pixel

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
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
 * Barra de navegación flotante de cristal, al estilo de la de Telegram: una cápsula con el icono
 * encima del nombre; el destino activo se resalta con una "píldora" de color y puede mostrar una
 * insignia con un número (por ejemplo, los elementos de la papelera).
 *
 * Gestos: toque para cambiar de pestaña, o arrastra el dedo sobre la barra para recorrerlas con
 * una vibración suave en cada cambio. El desenfoque lo define el tipo de blur elegido en Ajustes.
 */
@Composable
fun GlassNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BackdropState? = null,
    badges: Map<Int, Int> = emptyMap(),
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelect by rememberUpdatedState(onSelect)
    var widthPx by remember { mutableIntStateOf(0) }

    fun indexAt(x: Float): Int {
        if (widthPx <= 0) return currentSelected
        return ((x / widthPx) * items.size).toInt().coerceIn(0, items.lastIndex)
    }

    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .onSizeChanged { widthPx = it.width }
                .glassSurface(
                    state = backdrop,
                    tint = scheme.surfaceContainerHigh.copy(alpha = 0.42f),
                    fallback = scheme.surfaceContainerHigh.copy(alpha = 0.95f),
                    corner = 36.dp,
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
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { i, dest ->
                NavItem(
                    dest = dest,
                    selected = i == selected,
                    animations = animations,
                    badge = badges[i] ?: 0,
                    modifier = Modifier.weight(1f),
                ) {
                    if (i != selected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(i)
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    dest: NavDest,
    selected: Boolean,
    animations: Boolean,
    badge: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val colorSpec: AnimationSpec<Color> =
        if (animations) spring(stiffness = Spring.StiffnessMediumLow) else snap()
    val bg by animateColorAsState(
        targetValue = if (selected) scheme.primaryContainer.copy(alpha = 0.88f) else Color.Transparent,
        animationSpec = colorSpec,
        label = "navBg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        animationSpec = colorSpec,
        label = "navFg",
    )
    // El icono activo "salta" un poco al seleccionarse (resorte de Material 3 Expressive).
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = if (animations) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) else snap(),
        label = "navIconScale",
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(28.dp))
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
                    .size(26.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
            )
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .widthIn(min = 16.dp)
                        .height(16.dp)
                        .background(scheme.primary, CircleShape)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (badge > 99) "99+" else badge.toString(),
                        color = scheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
        }
        Text(
            stringResource(dest.labelRes),
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
