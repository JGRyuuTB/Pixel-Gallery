package com.GalleryYou.Pixel

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fondo desenfocado "de cristal": el contenido marcado con [backdropSource] se copia a una capa,
 * y las barras que usan [backdropBlur] dibujan esa copia borrosa detrás de sí mismas.
 * Solo funciona en Android 12 (API 31) o superior.
 */
class BackdropState(val source: GraphicsLayer, val blurred: GraphicsLayer) {
    var level by mutableIntStateOf(0)
    var radius by mutableFloatStateOf(0f)
    var saturation by mutableFloatStateOf(1.45f)
    var tintScale by mutableFloatStateOf(1f)
    var shine by mutableFloatStateOf(1f)
    var origin by mutableStateOf(Offset.Zero)
    var appliedRadius: Float = -1f
    var appliedSaturation: Float = -1f
    val enabled: Boolean get() = level > 0

    /** Aplica el factor de velo del estilo de blur activo al color de cada superficie. */
    fun scaleTint(c: Color): Color = c.copy(alpha = (c.alpha * tintScale).coerceIn(0f, 0.95f))
}

/**
 * Los 5 tipos de blur (nivel 1 a 5; 0 = desactivado):
 * radio del desenfoque, saturación del fondo, intensidad del velo de color y del brillo del borde.
 */
class BlurStyle(val radiusDp: Float, val saturation: Float, val tintScale: Float, val shine: Float)

val BlurStyles = listOf(
    BlurStyle(radiusDp = 14f, saturation = 1.25f, tintScale = 1.0f, shine = 1.0f), // 1 Suave
    BlurStyle(radiusDp = 24f, saturation = 1.45f, tintScale = 1.0f, shine = 1.0f), // 2 Media
    BlurStyle(radiusDp = 40f, saturation = 1.45f, tintScale = 1.0f, shine = 0.9f), // 3 Fuerte
    BlurStyle(radiusDp = 20f, saturation = 1.90f, tintScale = 0.55f, shine = 1.7f), // 4 Cristal: vivo y transparente
    BlurStyle(radiusDp = 32f, saturation = 0.70f, tintScale = 1.6f, shine = 0.5f), // 5 Escarcha: mate y lechoso
)

fun blurStyleFor(level: Int): BlurStyle = BlurStyles[(level - 1).coerceIn(0, BlurStyles.lastIndex)]

@Composable
fun rememberBackdropState(settings: AppSettings): BackdropState {
    val source = rememberGraphicsLayer()
    val blurred = rememberGraphicsLayer()
    val density = LocalDensity.current
    val state = remember(source, blurred) { BackdropState(source, blurred) }
    val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) settings.blur.coerceIn(0, BlurStyles.size) else 0
    val style = blurStyleFor(level)
    state.level = level
    state.radius = with(density) { style.radiusDp.dp.toPx() }
    state.saturation = style.saturation
    state.tintScale = style.tintScale
    state.shine = style.shine
    return state
}

/** Marca el contenido que quedará detrás de las barras translúcidas. */
fun Modifier.backdropSource(state: BackdropState): Modifier = this
    .onGloballyPositioned { state.origin = it.positionInRoot() }
    .drawWithContent {
        if (state.enabled) {
            state.source.record { this@drawWithContent.drawContent() }
            drawLayer(state.source)
            if (state.appliedRadius != state.radius || state.appliedSaturation != state.saturation) {
                state.blurred.renderEffect = BlurEffect(state.radius, state.radius, TileMode.Clamp)
                // La saturación depende del estilo: Cristal vivo, Escarcha casi apagada.
                state.blurred.colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(state.saturation) })
                state.appliedRadius = state.radius
                state.appliedSaturation = state.saturation
            }
            state.blurred.record { drawLayer(state.source) }
        } else {
            drawContent()
        }
    }

/** Pinta detrás del elemento el fondo desenfocado y un velo de color. Sin efecto si está desactivado. */
@Composable
fun Modifier.backdropBlur(state: BackdropState?, tint: Color, corner: Dp = 0.dp): Modifier {
    if (state == null || !state.enabled) return this
    var pos by remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { pos = it.positionInRoot() }
        .drawBehind {
            val path = Path().apply {
                addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(corner.toPx())))
            }
            clipPath(path) {
                translate(left = state.origin.x - pos.x, top = state.origin.y - pos.y) {
                    drawLayer(state.blurred)
                }
                drawRect(state.scaleTint(tint))
            }
        }
}

/**
 * Superficie de "cristal líquido": fondo desenfocado y saturado, velo de color, borde con brillo
 * especular y un reflejo suave arriba. Sin desenfoque activado usa [fallback], casi opaco, con
 * el mismo borde y reflejo para conservar el aspecto.
 */
@Composable
fun Modifier.glassSurface(
    state: BackdropState?,
    tint: Color,
    fallback: Color,
    corner: Dp,
    border: Boolean = true,
    extendBottom: Boolean = false,
): Modifier {
    val blurOn = state != null && state.enabled
    var pos by remember { mutableStateOf(Offset.Zero) }
    return this
        .onGloballyPositioned { pos = it.positionInRoot() }
        .drawBehind {
            val shine = if (blurOn && state != null) state.shine else 1f
            val r = corner.toPx()
            val h = if (extendBottom) size.height + 48.dp.toPx() else size.height
            val path = Path().apply {
                addRoundRect(RoundRect(0f, 0f, size.width, h, CornerRadius(r)))
            }
            if (blurOn && state != null) {
                clipPath(path) {
                    translate(left = state.origin.x - pos.x, top = state.origin.y - pos.y) {
                        drawLayer(state.blurred)
                    }
                    drawRect(state.scaleTint(tint), size = androidx.compose.ui.geometry.Size(size.width, h))
                }
            } else {
                drawPath(path, fallback)
            }
            // Reflejo suave en la mitad superior.
            clipPath(path) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = (0.16f * shine).coerceAtMost(0.5f)), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.55f,
                    ),
                    size = androidx.compose.ui.geometry.Size(size.width, h),
                )
            }
            if (border) {
                val sw = 1.3.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = (0.60f * shine).coerceAtMost(0.95f)),
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = (0.32f * shine).coerceAtMost(0.8f)),
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, h),
                    ),
                    topLeft = Offset(sw / 2f, sw / 2f),
                    size = androidx.compose.ui.geometry.Size(size.width - sw, h - sw),
                    cornerRadius = CornerRadius(r - sw / 2f),
                    style = Stroke(width = sw),
                )
            }
        }
}
