package com.GalleryYou.Pixel

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.ui.composed
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.layout.fillMaxSize
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

// ------------------------------------------------------------------ Estilos de barra

/** Nombres de los estilos, en el orden de [AppSettings.navStyle]. 0 y 1 usan [GlassNavBar]. */
val NavStyleNames = listOf(
    R.string.nav_style_classic,
    R.string.nav_style_stacked,
    R.string.navstyle_tiktok,
    R.string.navstyle_full,
    R.string.navstyle_island,
    R.string.navstyle_indicator,
    R.string.navstyle_bubble,
)

/**
 * Punto de entrada: dibuja la barra de navegación según [style] (ver [AppSettings.navStyle]) con su
 * propio margen. [badges] asocia el índice de un destino con un contador (se muestra como globito rojo).
 */
@Composable
fun GalleryNavBar(
    style: Int,
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdrop: BackdropState? = null,
    badges: Map<Int, Int> = emptyMap(),
) {
    when (style) {
        2 -> Box(modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp)) {
            TikTokNavBar(items, selected, animations, onSelect, backdrop, badges)
        }

        3 -> FullNavBar(items, selected, animations, onSelect, modifier, backdrop, badges)

        4 -> Box(
            modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            IslandNavBar(items, selected, animations, onSelect, backdrop, badges)
        }

        5 -> Box(modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 6.dp)) {
            IndicatorNavBar(items, selected, animations, onSelect, backdrop, badges)
        }

        6 -> Box(modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp)) {
            BubbleNavBar(items, selected, animations, onSelect, backdrop, badges)
        }

        else -> Box(modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 6.dp)) {
            GlassNavBar(items, selected, animations, onSelect, backdrop = backdrop, style = style, badges = badges)
        }
    }
}

/** Globito con los colores del tema (el mismo de las barras Clásica y Apilada). */
@Composable
private fun ThemedBadge(count: Int, modifier: Modifier = Modifier) {
    if (count > 0) NavBadge(count, modifier)
}

private val BadgeRed = Color(0xFFFE2C55)

/** Globito rojo con el número (99+ si es mayor), como el de la barra de TikTok. */
@Composable
private fun RedBadge(count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    Box(
        modifier
            .height(18.dp)
            .widthIn(min = 18.dp)
            .background(BadgeRed, CircleShape)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (count > 99) "99+" else count.toString(),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private fun Modifier.absorbTaps(): Modifier = this.pointerInput(Unit) { detectTapGestures { } }

private fun Modifier.plainClick(onClick: () -> Unit): Modifier = composed {
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
}

@Composable
private fun colorSpec(animations: Boolean): AnimationSpec<Color> =
    if (animations) spring(stiffness = Spring.StiffnessMediumLow) else snap()

@Composable
private fun floatSpec(animations: Boolean): AnimationSpec<Float> =
    if (animations) spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow) else snap()

// --- 1. Estilo TikTok: píldora oscura de cristal líquido, icono + nombre siempre visibles ---

@Composable
private fun TikTokNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
) {
    val capsule = RoundedCornerShape(26.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .glassSurface(
                state = backdrop,
                tint = Color.Black.copy(alpha = 0.38f),
                fallback = Color(0xFF17151B).copy(alpha = 0.94f),
                corner = 31.dp,
            )
            .absorbTaps()
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { i, dest ->
            val on = i == selected
            val fill by animateColorAsState(
                if (on) Color.Black.copy(alpha = 0.42f) else Color.Transparent,
                colorSpec(animations),
                label = "ttFill",
            )
            val rim by animateColorAsState(
                if (on) Color.White.copy(alpha = 0.30f) else Color.Transparent,
                colorSpec(animations),
                label = "ttRim",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(capsule)
                    .background(fill)
                    .border(1.dp, rim, capsule)
                    .plainClick { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Icon(
                            imageVector = if (on) dest.iconSelected else dest.icon,
                            contentDescription = stringResource(dest.labelRes),
                            tint = Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                        RedBadge(badges[i] ?: 0, Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-6).dp))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(dest.labelRes),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

// --- 3. Completa: barra de Material pegada abajo, a todo el ancho ---

@Composable
private fun FullNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier
            .fillMaxWidth()
            .glassSurface(
                state = backdrop,
                tint = scheme.surfaceContainer.copy(alpha = 0.55f),
                fallback = scheme.surfaceContainer,
                corner = 0.dp,
                border = false,
            )
            .absorbTaps(),
    ) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { i, dest ->
                val on = i == selected
                val pill by animateColorAsState(
                    if (on) scheme.secondaryContainer else Color.Transparent,
                    colorSpec(animations),
                    label = "clPill",
                )
                val fg by animateColorAsState(
                    if (on) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                    colorSpec(animations),
                    label = "clFg",
                )
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().plainClick { onSelect(i) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier.width(60.dp).height(32.dp).clip(CircleShape).background(pill),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box {
                            Icon(
                                imageVector = if (on) dest.iconSelected else dest.icon,
                                contentDescription = stringResource(dest.labelRes),
                                tint = fg,
                                modifier = Modifier.size(24.dp),
                            )
                            ThemedBadge(badges[i] ?: 0, Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-6).dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(dest.labelRes),
                        color = fg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

// --- 3. Isla: píldora compacta, solo iconos, centrada ---

@Composable
private fun IslandNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .height(54.dp)
            .glassSurface(
                state = backdrop,
                tint = scheme.surfaceContainerHigh.copy(alpha = 0.45f),
                fallback = scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                corner = 27.dp,
            )
            .absorbTaps()
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { i, dest ->
            val on = i == selected
            val bg by animateColorAsState(
                if (on) scheme.primary else Color.Transparent,
                colorSpec(animations),
                label = "isBg",
            )
            val fg by animateColorAsState(
                if (on) scheme.onPrimary else scheme.onSurfaceVariant,
                colorSpec(animations),
                label = "isFg",
            )
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(bg).plainClick { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                Box {
                    Icon(
                        imageVector = if (on) dest.iconSelected else dest.icon,
                        contentDescription = stringResource(dest.labelRes),
                        tint = fg,
                        modifier = Modifier.size(24.dp),
                    )
                    ThemedBadge(badges[i] ?: 0, Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-6).dp))
                }
            }
        }
    }
}

// --- 4. Indicador: iconos con nombre y una rayita que se desliza bajo el activo ---

@Composable
private fun IndicatorNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .height(62.dp)
            .glassSurface(
                state = backdrop,
                tint = scheme.surfaceContainerHigh.copy(alpha = 0.45f),
                fallback = scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                corner = 31.dp,
            )
            .absorbTaps(),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            val itemW = maxWidth / items.size
            val pos by animateFloatAsState(selected.toFloat(), floatSpec(animations), label = "indPos")
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = itemW * pos + (itemW - 22.dp) / 2, y = (-7).dp)
                    .size(width = 22.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(scheme.primary),
            )
            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { i, dest ->
                    val on = i == selected
                    val fg by animateColorAsState(
                        if (on) scheme.primary else scheme.onSurfaceVariant,
                        colorSpec(animations),
                        label = "indFg",
                    )
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight().plainClick { onSelect(i) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box {
                            Icon(
                                imageVector = if (on) dest.iconSelected else dest.icon,
                                contentDescription = stringResource(dest.labelRes),
                                tint = fg,
                                modifier = Modifier.size(24.dp),
                            )
                            ThemedBadge(badges[i] ?: 0, Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-6).dp))
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            stringResource(dest.labelRes),
                            color = fg,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                        // Hueco para la rayita que se desliza por encima.
                        Spacer(Modifier.height(7.dp))
                    }
                }
            }
        }
    }
}

// --- 5. Burbuja: un círculo de color se desliza hasta el destino activo ---

@Composable
private fun BubbleNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    onSelect: (Int) -> Unit,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .glassSurface(
                state = backdrop,
                tint = scheme.surfaceContainerHigh.copy(alpha = 0.45f),
                fallback = scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                corner = 29.dp,
            )
            .absorbTaps(),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(6.dp)) {
            val itemW = maxWidth / items.size
            val bubble = 46.dp
            val pos by animateFloatAsState(selected.toFloat(), floatSpec(animations), label = "bubPos")
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = itemW * pos + (itemW - bubble) / 2)
                    .size(bubble)
                    .clip(CircleShape)
                    .background(scheme.primary),
            )
            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { i, dest ->
                    val on = i == selected
                    val fg by animateColorAsState(
                        if (on) scheme.onPrimary else scheme.onSurfaceVariant,
                        colorSpec(animations),
                        label = "bubFg",
                    )
                    Box(
                        Modifier.weight(1f).fillMaxHeight().plainClick { onSelect(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box {
                            Icon(
                                imageVector = if (on) dest.iconSelected else dest.icon,
                                contentDescription = stringResource(dest.labelRes),
                                tint = fg,
                                modifier = Modifier.size(26.dp),
                            )
                            ThemedBadge(badges[i] ?: 0, Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-6).dp))
                        }
                    }
                }
            }
        }
    }
}
