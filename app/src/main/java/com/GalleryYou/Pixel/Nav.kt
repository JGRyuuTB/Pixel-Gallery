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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavDest(@StringRes val labelRes: Int, val icon: ImageVector, val iconSelected: ImageVector)

val GalleryNavItems = listOf(
    NavDest(R.string.nav_photos, AppIcons.Image, AppIcons.ImageDuo),
    NavDest(R.string.nav_albums, AppIcons.Collections, AppIcons.CollectionsDuo),
    NavDest(R.string.nav_trash, AppIcons.Trash, AppIcons.TrashDuo),
    NavDest(R.string.nav_settings, AppIcons.Settings, AppIcons.SettingsDuo),
)

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
 * Calibración de la barra de navegación (Ajustes > Barra de navegación). Todos los valores son
 * relativos al tamaño base de cada estilo: 1f = tamaño por defecto.
 */
class NavTuning(
    /** Multiplica el alto de la barra. */
    val height: Float = 1f,
    /** Fracción del ancho disponible (no afecta a los estilos Isla y Completa). */
    val width: Float = 1f,
    /** Distancia entre la barra y el borde inferior (no afecta al estilo Completa). */
    val bottom: Dp = 6.dp,
    /** Multiplica el tamaño de los iconos. */
    val icon: Float = 1f,
    /** 1f = totalmente redondeada (píldora), 0f = esquinas rectas. */
    val round: Float = 1f,
) {
    /** Radio de las esquinas de una barra de alto [h]. */
    fun corner(h: Dp): Dp = h / 2 * round

    /** Forma de los destinos seleccionados, acorde al redondeo. */
    val shape: Shape get() = RoundedCornerShape((50 * round).toInt().coerceIn(0, 50))
}

fun AppSettings.navTuning() = NavTuning(
    height = navHeight / 100f,
    width = navWidth / 100f,
    bottom = navBottom.dp,
    icon = navIcon / 100f,
    round = navRound / 100f,
)

/**
 * Punto de entrada: dibuja la barra de navegación según [style] (ver [AppSettings.navStyle]),
 * con la calibración [tuning]. [badges] asocia el índice de un destino con un contador.
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
    tuning: NavTuning = NavTuning(),
) {
    val haptic = LocalHapticFeedback.current
    // Las barras nuevas vibran al tocar; GlassNavBar (estilos 0 y 1) ya lo hace por su cuenta.
    val pick: (Int) -> Unit = { i ->
        if (i != selected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onSelect(i)
    }
    if (style == 3) {
        FullNavBar(items, selected, animations, pick, onSelect, modifier, backdrop, badges, tuning)
        return
    }
    val sidePad = when (style) {
        2 -> 12.dp
        4 -> 0.dp
        6 -> 24.dp
        else -> 14.dp
    }
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = sidePad, end = sidePad, top = 4.dp, bottom = tuning.bottom),
        contentAlignment = Alignment.Center,
    ) {
        Box(if (style == 4) Modifier else Modifier.fillMaxWidth(tuning.width)) {
            when (style) {
                2 -> TikTokNavBar(items, selected, animations, pick, onSelect, backdrop, badges, tuning)
                4 -> IslandNavBar(items, selected, animations, pick, onSelect, backdrop, badges, tuning)
                5 -> IndicatorNavBar(items, selected, animations, pick, onSelect, backdrop, badges, tuning)
                6 -> BubbleNavBar(items, selected, animations, pick, onSelect, backdrop, badges, tuning)
                else -> GlassNavBar(
                    items, selected, animations, onSelect,
                    backdrop = backdrop, style = style, badges = badges, tuning = tuning,
                )
            }
        }
    }
}

// ------------------------------------------------------------------ Piezas comunes

private val BadgeRed = Color(0xFFFE2C55)

/**
 * Absorbe los toques en toda la barra (los huecos no llegan a las fotos de atrás) y permite arrastrar
 * el dedo por ella para cambiar de destino. Las barras con destinos de igual ancho lo usan.
 */
@Composable
private fun Modifier.navGestures(count: Int, selected: Int, onSelect: (Int) -> Unit): Modifier {
    val haptic = LocalHapticFeedback.current
    val currentSelected by rememberUpdatedState(selected)
    val currentOnSelect by rememberUpdatedState(onSelect)
    var widthPx by remember { mutableIntStateOf(0) }

    fun indexAt(x: Float): Int {
        if (widthPx <= 0 || count <= 0) return currentSelected
        return ((x / widthPx) * count).toInt().coerceIn(0, count - 1)
    }

    return this
        .onSizeChanged { widthPx = it.width }
        .pointerInput(Unit) { detectTapGestures { } }
        .pointerInput(count) {
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
}

@Composable
private fun Modifier.plainClick(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return clickable(interactionSource = source, indication = null, onClick = onClick)
}

@Composable
private fun colorSpec(animations: Boolean): AnimationSpec<Color> =
    if (animations) spring(stiffness = Spring.StiffnessMediumLow) else snap()

@Composable
private fun floatSpec(animations: Boolean): AnimationSpec<Float> =
    if (animations) spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow) else snap()

/** Globito con el número (99+ si es mayor). Rojo (como el de TikTok) o con los colores del tema. */
@Composable
private fun NavBadge(count: Int, modifier: Modifier = Modifier, red: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .widthIn(min = 16.dp)
            .height(16.dp)
            .background(if (red) BadgeRed else scheme.primary, CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (count > 99) "99+" else count.toString(),
            color = if (red) Color.White else scheme.onPrimary,
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/**
 * Icono del destino con su globito. El globito se dibuja encima, sin ocupar espacio: el icono queda
 * centrado y nada lo recorta, aunque el destino tenga fondo redondeado.
 */
@Composable
private fun NavIcon(
    dest: NavDest,
    selected: Boolean,
    tint: Color,
    size: Dp,
    badge: Int,
    modifier: Modifier = Modifier,
    red: Boolean = false,
) {
    Box(modifier) {
        Icon(
            imageVector = if (selected) dest.iconSelected else dest.icon,
            contentDescription = stringResource(dest.labelRes),
            tint = tint,
            modifier = Modifier.size(size),
        )
        if (badge > 0) {
            NavBadge(
                badge,
                Modifier.align(Alignment.TopEnd).offset(x = size * 0.45f, y = -(size * 0.25f)),
                red,
            )
        }
    }
}

// ------------------------------------------------------------------ Estilos 0 y 1: Clásica y Apilada

/**
 * Barra de navegación flotante de cristal, compacta, con dos estilos que se eligen en Ajustes:
 *  - [style] 0, clásica: el destino activo muestra icono y nombre dentro de su píldora.
 *  - [style] 1, apilada: icono sobre el nombre, el activo resaltado con una píldora de color.
 * Admiten una insignia con número ([badges]) y arrastrar el dedo por la barra para cambiar de pestaña.
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
    tuning: NavTuning = NavTuning(),
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val stacked = style == 1
    val barHeight = (if (stacked) 54.dp else 48.dp) * tuning.height

    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .glassSurface(
                    state = backdrop,
                    tint = scheme.surfaceContainerHigh.copy(alpha = 0.42f),
                    fallback = scheme.surfaceContainerHigh.copy(alpha = 0.95f),
                    corner = tuning.corner(barHeight),
                )
                .navGestures(items.size, selected, onSelect)
                .padding(4.dp),
            horizontalArrangement = if (stacked) Arrangement.spacedBy(2.dp) else Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { i, dest ->
                val click = {
                    if (i != selected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(i)
                }
                if (stacked) {
                    StackedNavItem(dest, i == selected, animations, badges[i] ?: 0, tuning, Modifier.weight(1f), click)
                } else {
                    ClassicNavItem(dest, i == selected, animations, badges[i] ?: 0, tuning, click)
                }
            }
        }
    }
}

/** Estilo clásico: el activo muestra icono y nombre dentro de su propia píldora. */
@Composable
private fun ClassicNavItem(
    dest: NavDest,
    selected: Boolean,
    animations: Boolean,
    badge: Int,
    tuning: NavTuning,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(
        targetValue = if (selected) scheme.primaryContainer else Color.Transparent,
        animationSpec = colorSpec(animations),
        label = "navBg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        animationSpec = colorSpec(animations),
        label = "navFg",
    )
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .background(bg, tuning.shape)
            .plainClick(onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavIcon(dest, selected, fg, 22.dp * tuning.icon, badge)
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
    tuning: NavTuning,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(
        targetValue = if (selected) scheme.primaryContainer.copy(alpha = 0.88f) else Color.Transparent,
        animationSpec = colorSpec(animations),
        label = "navBgStacked",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        animationSpec = colorSpec(animations),
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
            .background(bg, tuning.shape)
            .plainClick(onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NavIcon(
            dest, selected, fg, 22.dp * tuning.icon, badge,
            Modifier.graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
            },
        )
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

// ------------------------------------------------------------------ Estilo 2: TikTok

/** Píldora oscura de cristal, icono y nombre siempre visibles, destino activo en una cápsula oscura. */
@Composable
private fun TikTokNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    pick: (Int) -> Unit,
    rawSelect: (Int) -> Unit,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
    tuning: NavTuning,
) {
    val barHeight = 54.dp * tuning.height
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .glassSurface(
                state = backdrop,
                tint = Color.Black.copy(alpha = 0.38f),
                fallback = Color(0xFF17151B).copy(alpha = 0.94f),
                corner = tuning.corner(barHeight),
            )
            .navGestures(items.size, selected, rawSelect)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
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
                    .background(fill, tuning.shape)
                    .border(1.dp, rim, tuning.shape)
                    .plainClick { pick(i) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NavIcon(dest, on, Color.White, 22.dp * tuning.icon, badges[i] ?: 0, red = true)
                    Text(
                        stringResource(dest.labelRes),
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Estilo 3: Completa

/** Barra de Material pegada abajo, a todo el ancho (no usa el ancho ni el margen inferior de la calibración). */
@Composable
private fun FullNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    pick: (Int) -> Unit,
    rawSelect: (Int) -> Unit,
    modifier: Modifier,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
    tuning: NavTuning,
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
            .navGestures(items.size, selected, rawSelect),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(56.dp * tuning.height)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { i, dest ->
                val on = i == selected
                val pill by animateColorAsState(
                    if (on) scheme.secondaryContainer else Color.Transparent,
                    colorSpec(animations),
                    label = "fuPill",
                )
                val fg by animateColorAsState(
                    if (on) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                    colorSpec(animations),
                    label = "fuFg",
                )
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().plainClick { pick(i) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier.width(56.dp).height(28.dp).background(pill, tuning.shape),
                        contentAlignment = Alignment.Center,
                    ) {
                        NavIcon(dest, on, fg, 22.dp * tuning.icon, badges[i] ?: 0)
                    }
                    Text(
                        stringResource(dest.labelRes),
                        color = fg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Estilo 4: Isla

/** Píldora compacta, centrada, con solo iconos (no usa el ancho de la calibración: se ajusta a su contenido). */
@Composable
private fun IslandNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    pick: (Int) -> Unit,
    rawSelect: (Int) -> Unit,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
    tuning: NavTuning,
) {
    val scheme = MaterialTheme.colorScheme
    val barHeight = 48.dp * tuning.height
    val itemSize = barHeight - 8.dp
    Row(
        modifier = Modifier
            .height(barHeight)
            .glassSurface(
                state = backdrop,
                tint = scheme.surfaceContainerHigh.copy(alpha = 0.45f),
                fallback = scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                corner = tuning.corner(barHeight),
            )
            .navGestures(items.size, selected, rawSelect)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
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
                Modifier.size(width = itemSize + 6.dp, height = itemSize).background(bg, tuning.shape).plainClick { pick(i) },
                contentAlignment = Alignment.Center,
            ) {
                NavIcon(dest, on, fg, 22.dp * tuning.icon, badges[i] ?: 0)
            }
        }
    }
}

// ------------------------------------------------------------------ Estilo 5: Indicador

/** Iconos con nombre y una rayita que se desliza bajo el destino activo. */
@Composable
private fun IndicatorNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    pick: (Int) -> Unit,
    rawSelect: (Int) -> Unit,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
    tuning: NavTuning,
) {
    val scheme = MaterialTheme.colorScheme
    val barHeight = 54.dp * tuning.height
    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .glassSurface(
                state = backdrop,
                tint = scheme.surfaceContainerHigh.copy(alpha = 0.45f),
                fallback = scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                corner = tuning.corner(barHeight),
            )
            .navGestures(items.size, selected, rawSelect),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            val itemW = maxWidth / items.size
            val pos by animateFloatAsState(selected.toFloat(), floatSpec(animations), label = "indPos")
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = itemW * pos + (itemW - 20.dp) / 2, y = (-5).dp)
                    .size(width = 20.dp, height = 3.dp)
                    .background(scheme.primary, CircleShape),
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
                        modifier = Modifier.weight(1f).fillMaxHeight().plainClick { pick(i) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        NavIcon(dest, on, fg, 22.dp * tuning.icon, badges[i] ?: 0)
                        Text(
                            stringResource(dest.labelRes),
                            color = fg,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                        // Hueco para la rayita que se desliza por debajo.
                        Spacer(Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ Estilo 6: Burbuja

/** Un círculo de color se desliza hasta el destino activo. */
@Composable
private fun BubbleNavBar(
    items: List<NavDest>,
    selected: Int,
    animations: Boolean,
    pick: (Int) -> Unit,
    rawSelect: (Int) -> Unit,
    backdrop: BackdropState?,
    badges: Map<Int, Int>,
    tuning: NavTuning,
) {
    val scheme = MaterialTheme.colorScheme
    val barHeight = 50.dp * tuning.height
    Box(
        Modifier
            .fillMaxWidth()
            .height(barHeight)
            .glassSurface(
                state = backdrop,
                tint = scheme.surfaceContainerHigh.copy(alpha = 0.45f),
                fallback = scheme.surfaceContainerHigh.copy(alpha = 0.96f),
                corner = tuning.corner(barHeight),
            )
            .navGestures(items.size, selected, rawSelect),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(5.dp)) {
            val itemW = maxWidth / items.size
            val bubble = maxHeight
            val pos by animateFloatAsState(selected.toFloat(), floatSpec(animations), label = "bubPos")
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = itemW * pos + (itemW - bubble) / 2)
                    .size(bubble)
                    .background(scheme.primary, tuning.shape),
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
                        Modifier.weight(1f).fillMaxHeight().plainClick { pick(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        NavIcon(dest, on, fg, 24.dp * tuning.icon, badges[i] ?: 0)
                    }
                }
            }
        }
    }
}
