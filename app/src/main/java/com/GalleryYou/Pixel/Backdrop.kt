package com.GalleryYou.Pixel

import android.os.Build
import androidx.annotation.StringRes
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
    var appliedLevel: Int = -1
    var appliedRadius: Float = -1f
    val enabled: Boolean get() = level > 0

    /** Tipo de desenfoque elegido (null = desactivado). */
    val style: BlurStyle? get() = BlurStyles.getOrNull(level - 1)
}

/** Cómo se tiñe el cristal: con el color del tema, oscuro o claro. */
enum class TintMode { Theme, Dark, Light }

/**
 * Un tipo de desenfoque: radio, saturación y brillo del fondo borroso y el velo de color que lleva encima.
 * [tintAlpha] es la opacidad del velo en los modos Dark/Light; en Theme multiplica la opacidad que pide la barra.
 */
class BlurStyle(
    @StringRes val nameRes: Int,
    val radiusDp: Float,
    val saturation: Float,
    val brightness: Float,
    val tintMode: TintMode,
    val tintAlpha: Float,
    /** Sube el negro del fondo borroso (0 a 255): da aspecto de cristal esmerilado aunque detrás todo sea oscuro. */
    val lift: Float = 0f,
    /** Fuerza de la refracción (1 = normal). Se aplica con Liquid Glass activado en Android 13+. */
    val refraction: Float = 1f,
    /** true = refracta siempre (en Android 13+), aunque Liquid Glass esté apagado. */
    val forceRefraction: Boolean = false,
    /** Dispersión cromática de la lente: separación de rojo y azul en el borde. */
    val dispersion: Float = 0.12f,
) {
    fun resolveTint(base: Color): Color = when (tintMode) {
        TintMode.Theme -> base.copy(alpha = (base.alpha * tintAlpha).coerceIn(0f, 1f))
        TintMode.Dark -> Color.Black.copy(alpha = tintAlpha)
        TintMode.Light -> Color.White.copy(alpha = tintAlpha)
    }
}

/** Los 7 tipos de desenfoque. El valor guardado en ajustes es el índice + 1 (0 = desactivado). */
val BlurStyles = listOf(
    BlurStyle(R.string.blur_soft, 14f, 1.45f, 1.00f, TintMode.Theme, 1.0f),     // 1 Suave
    BlurStyle(R.string.blur_strong, 30f, 1.45f, 1.00f, TintMode.Theme, 1.0f),   // 2 Fuerte
    BlurStyle(R.string.blur_dark, 22f, 1.75f, 1.05f, TintMode.Dark, 0.46f),     // 3 Oscuro (como la barra de TikTok)
    BlurStyle(R.string.blur_frosted, 40f, 1.00f, 1.10f, TintMode.Light, 0.22f, 16f), // 4 Escarcha
    BlurStyle(R.string.blur_vivid, 20f, 2.30f, 1.00f, TintMode.Theme, 0.55f),   // 5 Vibrante
    BlurStyle(R.string.blur_smoke, 28f, 0.70f, 0.85f, TintMode.Dark, 0.55f),    // 6 Humo
    // 7 Acrílico: cristal esmerilado claro con la refracción al máximo (lente gruesa y dispersión cromática fuerte).
    BlurStyle(
        R.string.blur_acrylic, 14f, 1.30f, 1.15f, TintMode.Light, 0.30f, 34f,
        refraction = 2.6f, forceRefraction = true, dispersion = 0.34f,
    ),
)

/** Índice (en [BlurStyles] + 1) del tipo oscuro que usa la barra estilo TikTok. */
const val BLUR_DARK = 3

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
    val refractive = (settings.liquidGlass || BlurStyles.getOrNull(level - 1)?.forceRefraction == true) &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val baseRadius = (BlurStyles.getOrNull(level - 1)?.radiusDp ?: 14f).dp
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
            if (state.appliedRadius != state.radius || state.appliedLiquid != state.liquid ||
                state.appliedLevel != state.level
            ) {
                state.blurred.renderEffect = BlurEffect(state.radius, state.radius, TileMode.Clamp)
                // Saturación y brillo según el tipo de desenfoque. Con Liquid Glass además se
                // aclara un poco, como el vidrio que deja pasar la luz.
                val st = state.style
                val sat = st?.saturation ?: 1.45f
                val gain = st?.brightness ?: 1f
                val lift = st?.lift ?: 0f
                state.blurred.colorFilter = ColorFilter.colorMatrix(
                    if (state.liquid) {
                        saturationLift(sat * 1.24f, gain * 1.07f, 8f + lift)
                    } else {
                        saturationLift(sat, gain, lift)
                    },
                )
                state.appliedRadius = state.radius
                state.appliedLiquid = state.liquid
                state.appliedLevel = state.level
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
uniform float disp;

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
    half red = content.eval(clamp(coord + shift * (1.0 + disp), lo, hi)).r;
    half blue = content.eval(clamp(coord + shift * (1.0 - disp), lo, hi)).b;
    return half4(red, g.g, blue, g.a);
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class RefractionEffect {
    private val shader = RuntimeShader(REFRACTION_AGSL)
    private var w = -1f
    private var h = -1f
    private var r = -1f
    private var mr = -1f
    private var appliedStrength = -1f
    private var appliedDisp = -1f
    private var applied = false

    /** Aplica (o actualiza si cambió el tamaño) la lente a la capa del cristal. */
    fun apply(
        layer: GraphicsLayer,
        width: Float,
        height: Float,
        radius: Float,
        maxRim: Float,
        strength: Float = 1f,
        dispersion: Float = 0.12f,
    ) {
        if (applied && w == width && h == height && r == radius && mr == maxRim && appliedStrength == strength && appliedDisp == dispersion) return
        // El grosor del borde que refracta tiene tope: una barra y un panel alto se ven igual.
        // Con fuerza > 1 la lente crece hasta ocupar la mitad del lado corto (refracción máxima).
        val share = 0.42f + 0.08f * (strength - 1f).coerceIn(0f, 1f)
        val rim = minOf(minOf(width, height) * share, maxRim * strength)
        shader.setFloatUniform("size", width, height)
        shader.setFloatUniform("radius", radius)
        shader.setFloatUniform("rim", rim)
        shader.setFloatUniform("depth", rim * (0.85f * strength).coerceAtMost(2.4f))
        shader.setFloatUniform("disp", dispersion)
        layer.renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
        w = width
        h = height
        r = radius
        mr = maxRim
        appliedStrength = strength
        appliedDisp = dispersion
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
                val base = state.style?.resolveTint(tint) ?: tint
                drawRect(if (state.liquid) base.copy(alpha = base.alpha * 0.6f) else base)
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
    /** Sin velo de color: solo desenfoque, refracción y brillos de borde (el fondo "desaparece"). */
    clear: Boolean = false,
    /** Fuerza la refracción aunque Liquid Glass esté apagado (Android 13+). */
    refract: Boolean = false,
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
            val style = state?.style
            val strength = if (liquid || refract || style?.forceRefraction == true) (style?.refraction ?: 1f) else 0f
            val r = corner.toPx()
            val h = if (extendBottom) size.height + 48.dp.toPx() else size.height
            val path = Path().apply {
                addRoundRect(RoundRect(0f, 0f, size.width, h, CornerRadius(r)))
            }
            if (blurOn && state != null) {
                clipPath(path) {
                    if (strength > 0f && refractor != null && !extendBottom && size.width > 1f && h > 1f) {
                        refractLayer.record {
                            clipPath(path) {
                                translate(left = state.origin.x - pos.x, top = state.origin.y - pos.y) {
                                    drawLayer(state.blurred)
                                }
                            }
                        }
                        refractor.apply(refractLayer, size.width, h, r, 24.dp.toPx(), strength, style?.dispersion ?: 0.12f)
                        drawLayer(refractLayer)
                    } else {
                        translate(left = state.origin.x - pos.x, top = state.origin.y - pos.y) {
                            drawLayer(state.blurred)
                        }
                    }
                    // Liquid Glass: el velo de color es la mitad de opaco, así se ve el fondo a través.
                    if (!clear) {
                        val base = state?.style?.resolveTint(tint) ?: tint
                        drawRect(
                            if (liquid) base.copy(alpha = base.alpha * 0.5f) else base,
                            size = androidx.compose.ui.geometry.Size(size.width, h),
                        )
                    }
                }
            } else {
                drawPath(path, fallback)
            }
            // Reflejo suave en la mitad superior.
            clipPath(path) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (clear) 0.07f else if (liquid) 0.26f else 0.16f), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.55f,
                    ),
                    size = androidx.compose.ui.geometry.Size(size.width, h),
                )
                if (liquid && !clear) {
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
