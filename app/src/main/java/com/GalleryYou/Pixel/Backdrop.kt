package com.GalleryYou.Pixel

import android.os.Build
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.annotation.RequiresApi
import android.graphics.RuntimeShader
import android.graphics.RenderEffect
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
    var origin by mutableStateOf(Offset.Zero)
    /** Liquid Glass activado: velo más transparente, fondo más vivo y bordes con reflejos. */
    var liquid by mutableStateOf(false)
    var appliedLiquid: Boolean = false
    var appliedRadius: Float = -1f
    val enabled: Boolean get() = level > 0
}

@Composable
fun rememberBackdropState(settings: AppSettings): BackdropState {
    val source = rememberGraphicsLayer()
    val blurred = rememberGraphicsLayer()
    val density = LocalDensity.current
    val state = remember(source, blurred) { BackdropState(source, blurred) }
    val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) settings.blur else 0
    state.level = level
    state.liquid = settings.liquidGlass
    // Con refracción real el fondo debe verse casi nítido: si está muy borroso no se nota la lente.
    val refractive = settings.liquidGlass && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val baseRadius = if (level >= 2) 30.dp else 14.dp
    state.radius = with(density) { (if (refractive) baseRadius * 0.45f else baseRadius).toPx() }
    return state
}

/** Matriz de color: saturación [sat], ganancia de brillo [gain] y desplazamiento [offset] (0 a 255). */
private fun saturationLift(sat: Float, gain: Float, offset: Float): ColorMatrix {
    val i = 1f - sat
    val r = 0.213f * i
    val g = 0.715f * i
    val b = 0.072f * i
    return ColorMatrix(
        floatArrayOf(
            (r + sat) * gain, g * gain, b * gain, 0f, offset,
            r * gain, (g + sat) * gain, b * gain, 0f, offset,
            r * gain, g * gain, (b + sat) * gain, 0f, offset,
            0f, 0f, 0f, 1f, 0f,
        ),
    )
}

/** Marca el contenido que quedará detrás de las barras translúcidas. */
fun Modifier.backdropSource(state: BackdropState): Modifier = this
    .onGloballyPositioned { state.origin = it.positionInRoot() }
    .drawWithContent {
        if (state.enabled) {
            state.source.record { this@drawWithContent.drawContent() }
            drawLayer(state.source)
            if (state.appliedRadius != state.radius || state.appliedLiquid != state.liquid) {
                state.blurred.renderEffect = BlurEffect(state.radius, state.radius, TileMode.Clamp)
                // Más saturación: el fondo detrás del cristal se ve más vivo. Con Liquid Glass además
                // se aclara un poco, como el vidrio que deja pasar la luz.
                state.blurred.colorFilter = ColorFilter.colorMatrix(
                    if (state.liquid) saturationLift(1.8f, 1.07f, 8f) else saturationLift(1.45f, 1f, 0f),
                )
                state.appliedRadius = state.radius
                state.appliedLiquid = state.liquid
            }
            state.blurred.record { drawLayer(state.source) }
        } else {
            drawContent()
        }
    }

/**
 * Refracción real (Android 13+): shader AGSL que dobla la luz en el borde del cristal, como una
 * lente. Cerca del contorno (SDF de un rectángulo redondeado) el fondo se muestrea desde más adentro,
 * con una pequeña dispersión cromática (rojo y azul se desvían distinto).
 */
private const val REFRACTION_AGSL = """
uniform shader content;
uniform float2 size;
uniform float radius;
uniform float depth;
uniform float rim;

float sdRoundRect(float2 p, float2 halfSize, float r) {
    float2 q = abs(p) - halfSize + float2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0))) - r;
}

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 p = coord - halfSize;
    float inside = -sdRoundRect(p, halfSize, radius);
    float t = clamp(1.0 - inside / rim, 0.0, 1.0);
    if (t <= 0.0) {
        return content.eval(coord);
    }
    float2 ex = float2(1.0, 0.0);
    float2 ey = float2(0.0, 1.0);
    float2 grad = float2(
        sdRoundRect(p + ex, halfSize, radius) - sdRoundRect(p - ex, halfSize, radius),
        sdRoundRect(p + ey, halfSize, radius) - sdRoundRect(p - ey, halfSize, radius));
    float len = length(grad);
    float2 n = len > 0.0001 ? grad / len : float2(0.0);
    float bend = t * t * depth;
    float2 shift = -n * bend;
    float2 lo = float2(0.5);
    float2 hi = size - float2(0.5);
    half4 g = content.eval(clamp(coord + shift, lo, hi));
    half red = content.eval(clamp(coord + shift * 1.12, lo, hi)).r;
    half blue = content.eval(clamp(coord + shift * 0.88, lo, hi)).b;
    return half4(red, g.g, blue, g.a);
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class RefractionEffect {
    private val shader = RuntimeShader(REFRACTION_AGSL)
    private var w = -1f
    private var h = -1f
    private var r = -1f
    private var applied = false

    /** Aplica (o actualiza si cambió el tamaño) la lente a la capa del cristal. */
    fun apply(layer: GraphicsLayer, width: Float, height: Float, radius: Float) {
        if (applied && w == width && h == height && r == radius) return
        val rim = minOf(width, height) * 0.42f
        shader.setFloatUniform("size", width, height)
        shader.setFloatUniform("radius", radius)
        shader.setFloatUniform("rim", rim)
        shader.setFloatUniform("depth", rim * 0.85f)
        layer.renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
        w = width
        h = height
        r = radius
        applied = true
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
                drawRect(if (state.liquid) tint.copy(alpha = tint.alpha * 0.6f) else tint)
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
    // Capa propia de este cristal, donde se aplica la refracción (solo Android 13+).
    val refractLayer = rememberGraphicsLayer()
    val refractor = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) RefractionEffect() else null
    }
    return this
        .onGloballyPositioned { pos = it.positionInRoot() }
        .drawBehind {
            val liquid = state?.liquid == true
            val r = corner.toPx()
            val h = if (extendBottom) size.height + 48.dp.toPx() else size.height
            val path = Path().apply {
                addRoundRect(RoundRect(0f, 0f, size.width, h, CornerRadius(r)))
            }
            if (blurOn && state != null) {
                clipPath(path) {
                    if (liquid && refractor != null && !extendBottom && size.width > 1f && h > 1f) {
                        refractLayer.record {
                            clipPath(path) {
                                translate(left = state.origin.x - pos.x, top = state.origin.y - pos.y) {
                                    drawLayer(state.blurred)
                                }
                            }
                        }
                        refractor.apply(refractLayer, size.width, h, r)
                        drawLayer(refractLayer)
                    } else {
                        translate(left = state.origin.x - pos.x, top = state.origin.y - pos.y) {
                            drawLayer(state.blurred)
                        }
                    }
                    // Liquid Glass: el velo de color es la mitad de opaco, así se ve el fondo a través.
                    drawRect(
                        if (liquid) tint.copy(alpha = tint.alpha * 0.5f) else tint,
                        size = androidx.compose.ui.geometry.Size(size.width, h),
                    )
                }
            } else {
                drawPath(path, fallback)
            }
            // Reflejo suave en la mitad superior.
            clipPath(path) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (liquid) 0.26f else 0.16f), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.55f,
                    ),
                    size = androidx.compose.ui.geometry.Size(size.width, h),
                )
                if (liquid) {
                    // Luz que sube desde el borde inferior, como en el cristal líquido.
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.12f)),
                            startY = h * 0.6f,
                            endY = h,
                        ),
                        size = androidx.compose.ui.geometry.Size(size.width, h),
                    )
                }
            }
            if (border) {
                val sw = (if (liquid) 1.0f else 1.3f).dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (liquid) 0.85f else 0.60f),
                            Color.White.copy(alpha = if (liquid) 0.10f else 0.06f),
                            Color.White.copy(alpha = if (liquid) 0.55f else 0.32f),
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, h),
                    ),
                    topLeft = Offset(sw / 2f, sw / 2f),
                    size = androidx.compose.ui.geometry.Size(size.width - sw, h - sw),
                    cornerRadius = CornerRadius(r - sw / 2f),
                    style = Stroke(width = sw),
                )
                if (liquid) {
                    // Segundo filo de luz hacia adentro: da grosor al cristal.
                    val inset = 2.5.dp.toPx()
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.14f),
                        topLeft = Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(size.width - inset * 2f, h - inset * 2f),
                        cornerRadius = CornerRadius((r - inset).coerceAtLeast(0f)),
                        style = Stroke(width = 0.8.dp.toPx()),
                    )
                }
            }
        }
}
