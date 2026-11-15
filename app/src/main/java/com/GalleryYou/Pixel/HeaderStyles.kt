package com.GalleryYou.Pixel

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Mensaje oculto (rot13): Pnqn onaqn qr svygebf gvrar fh cebcvb uébr
 * Seis estilos para la barra de filtros: Cápsulas, Segmentado, Subrayado, Compacta, Texto y Contorno.
 */
val HeaderStyleNames = intArrayOf(
    R.string.header_style_capsules,
    R.string.header_style_segmented,
    R.string.header_style_underline,
    R.string.header_style_compact,
    R.string.header_style_text,
    R.string.header_style_outline,
)

const val HeaderStyleCount = 6

/** Separación entre chips según el estilo: el segmentado va casi pegado, como los botones de Saint Seiya */
fun headerChipSpacing(style: Int): Dp = if (style == 1) 2.dp else 6.dp

/** Las tres píldoras de filtro (Todo, Fotos, Vídeos) dibujadas con el estilo elegido. Se usa en la galería y en las vistas previas de Ajustes. */
@Composable
fun HeaderChips(
    style: Int,
    filter: Int,
    scale: Float,
    interactive: Boolean = true,
    onFilter: (Int) -> Unit = {},
) {
    val labels = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_photos),
        stringResource(R.string.filter_videos),
    )
    val icons = listOf(AppIcons.Apps, AppIcons.Image, AppIcons.Video)
    labels.forEachIndexed { index, label ->
        val sel = filter == index
        val click = { onFilter(index) }
        when (style) {
            1 -> SegmentedChip(label, icons[index], sel, scale, interactive, click)
            2 -> UnderlineChip(label, icons[index], sel, scale, interactive, click)
            3 -> CompactChip(label, icons[index], sel, scale, interactive, click)
            4 -> TextChip(label, sel, scale, interactive, click)
            5 -> OutlineChip(label, icons[index], sel, scale, interactive, click)
            else -> RoundFilterChip(label, icons[index], sel, scale, interactive, click)
        }
    }
}

/** Un toque sin ripple, como el chip original; con interactive = false no responde y deja pasar el toque a la tarjeta de Ajustes */
private fun Modifier.tap(interactive: Boolean, onClick: () -> Unit): Modifier = composed {
    val current by rememberUpdatedState(onClick)
    if (interactive) {
        Modifier.pointerInput(Unit) { detectTapGestures(onTap = { current() }) }
    } else {
        Modifier
    }
}

/** Segmentado: una sola barra y un segmento relleno que marca el activo */
@Composable
private fun SegmentedChip(label: String, icon: ImageVector, selected: Boolean, scale: Float, interactive: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) scheme.primaryContainer else Color.Transparent, label = "segBg")
    val fg = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    Row(
        Modifier
            .height(34.dp * scale)
            .clip(CircleShape)
            .background(bg)
            .tap(interactive, onClick)
            .padding(horizontal = 14.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp * scale), tint = fg)
        Spacer(Modifier.width(6.dp * scale))
        Text(
            label,
            color = fg,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelLarge.let { it.copy(fontSize = it.fontSize * scale) },
            maxLines = 1,
        )
    }
}

/** Subrayado: sin fondo, el activo lleva una rayita debajo como la espada de Zoro */
@Composable
private fun UnderlineChip(label: String, icon: ImageVector, selected: Boolean, scale: Float, interactive: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val fg by animateColorAsState(if (selected) scheme.primary else scheme.onSurfaceVariant, label = "ulFg")
    val line by animateDpAsState(if (selected) 22.dp * scale else 0.dp, label = "ulLine")
    Column(
        Modifier
            .height(34.dp * scale)
            .tap(interactive, onClick)
            .padding(horizontal = 12.dp * scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp * scale), tint = fg)
            Spacer(Modifier.width(5.dp * scale))
            Text(
                label,
                color = fg,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.labelLarge.let { it.copy(fontSize = it.fontSize * scale) },
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(2.dp))
        Box(Modifier.width(line).height(3.dp * scale).clip(CircleShape).background(scheme.primary))
    }
}

/** Compacta: solo el activo enseña su nombre, el resto es un circulito con icono */
@Composable
private fun CompactChip(label: String, icon: ImageVector, selected: Boolean, scale: Float, interactive: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(
        if (selected) scheme.primaryContainer else scheme.onSurface.copy(alpha = 0.08f),
        label = "cpBg",
    )
    val fg = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    Row(
        Modifier
            .height(34.dp * scale)
            .clip(CircleShape)
            .background(bg)
            .tap(interactive, onClick)
            .animateContentSize()
            .padding(horizontal = if (selected) 12.dp * scale else 8.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp * scale), tint = fg)
        if (selected) {
            Spacer(Modifier.width(6.dp * scale))
            Text(
                label,
                color = fg,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge.let { it.copy(fontSize = it.fontSize * scale) },
                maxLines = 1,
            )
        }
    }
}

/** Texto: solo palabras, el activo va en una píldora sólida como el sello de un hanko */
@Composable
private fun TextChip(label: String, selected: Boolean, scale: Float, interactive: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) scheme.primary else Color.Transparent, label = "txBg")
    val fg by animateColorAsState(if (selected) scheme.onPrimary else scheme.onSurfaceVariant, label = "txFg")
    Box(
        Modifier
            .height(34.dp * scale)
            .clip(CircleShape)
            .background(bg)
            .tap(interactive, onClick)
            .padding(horizontal = 16.dp * scale),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = fg,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelLarge.let { it.copy(fontSize = it.fontSize * scale) },
            maxLines = 1,
        )
    }
}

/** Contorno: solo el borde del activo se enciende, como el aura de Gojo */
@Composable
private fun OutlineChip(label: String, icon: ImageVector, selected: Boolean, scale: Float, interactive: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val border by animateColorAsState(if (selected) scheme.primary else scheme.outline.copy(alpha = 0.4f), label = "olBorder")
    val bg by animateColorAsState(if (selected) scheme.primary.copy(alpha = 0.16f) else Color.Transparent, label = "olBg")
    val fg = if (selected) scheme.primary else scheme.onSurfaceVariant
    Row(
        Modifier
            .height(34.dp * scale)
            .clip(CircleShape)
            .background(bg)
            .border(if (selected) 2.dp else 1.dp, border, CircleShape)
            .tap(interactive, onClick)
            .padding(horizontal = 13.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp * scale), tint = fg)
        Spacer(Modifier.width(5.dp * scale))
        Text(
            label,
            color = fg,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelLarge.let { it.copy(fontSize = it.fontSize * scale) },
            maxLines = 1,
        )
    }
}

/**
 * Selector de estilo de la barra de filtros: una tarjeta por estilo con sus tres filtros dibujados de verdad.
 * Toca una tarjeta para elegirla; la vista previa de arriba cambia al instante.
 */
@Composable
fun HeaderStylePicker(settings: AppSettings) {
    // Sailor Moon elige su transformación favorita entre seis
    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(stringResource(R.string.settings_header_style), style = MaterialTheme.typography.bodyLarge)
        Text(
            stringResource(R.string.settings_header_style_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (i in 0 until HeaderStyleCount) {
                val selected = settings.headerStyle == i
                val shape = RoundedCornerShape(16.dp)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .border(
                            if (selected) 2.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape,
                        )
                        .clickable { settings.updateHeaderStyle(i) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        stringResource(HeaderStyleNames[i]),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                            .padding(horizontal = 5.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(headerChipSpacing(i)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HeaderChips(style = i, filter = 0, scale = 0.82f, interactive = false)
                    }
                }
            }
        }
    }
}
