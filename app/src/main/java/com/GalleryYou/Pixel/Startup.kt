package com.GalleryYou.Pixel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.delay

/**
 * Opening de anime: cuatro orbes bailan alrededor del centro antes de que empiece la función
 * La coreografía imita el arranque del vídeo de referencia: las formas brotan del centro, esperan respirando,
 * giran un cuarto de vuelta y se encogen hasta desaparecer. Cada icono de la app trae su propia temporada
 * (colores, estilo de dibujo y fondo), así que el opening cambia según el icono elegido en Ajustes.
 */

/** Cómo se rellena cada orbe: degradado lineal, esfera con brillo radial o color liso */
private enum class Shading { Linear, Radial, Solid }

/** Cómo se dibuja la forma: relleno, contorno, cristal, neón con resplandor o píxeles */
private enum class OrbStyle { Fill, Outline, Glass, Neon, Pixel }

/**
 * Una temporada del opening, calcada del icono de la app: colores en el orden arriba, derecha, abajo e izquierda,
 * el estilo, la opacidad, un fondo propio (null = el del tema) y si lleva el puntito blanco central del icono Sakura.
 */
private class OrbTheme(
    val colors: List<Color>,
    val highlights: List<Color> = emptyList(),
    val shading: Shading = Shading.Linear,
    val style: OrbStyle = OrbStyle.Fill,
    val fillAlpha: Float = 1f,
    val background: Brush? = null,
    val centerDot: Boolean = false,
)

private fun hexColors(vararg values: Long) = values.map { Color(it) }

private val DefaultOrbColors = hexColors(0xFF4285F4, 0xFF34C9A0, 0xFF9C6EF0, 0xFFF25C8A)

/**
 * Sailor Moon elige su transformación: la temporada que corresponde al icono seleccionado (índice de [AppIconOptions]).
 * 0 Por defecto, 1 Material You, 2 Contorno, 3 Pastel, 4 Cristal, 5 Mono, 6 Atardecer, 7 Sakura, 8 Neón, 9 Plano, 10 Pixel Style, 11 Pixel Art.
 */
@Composable
private fun orbThemeFor(icon: Int): OrbTheme = when (icon) {
    // Material You: los mismos cuatro colores del sistema que usa el icono (se resuelven solos en Android 12 o superior)
    1 -> OrbTheme(
        colors = listOf(
            colorResource(R.color.ic_you_p1),
            colorResource(R.color.ic_you_p2),
            colorResource(R.color.ic_you_p3),
            colorResource(R.color.ic_you_p4),
        ),
        fillAlpha = 0.95f,
    )
    2 -> OrbTheme(DefaultOrbColors, style = OrbStyle.Outline, shading = Shading.Solid)
    3 -> OrbTheme(
        hexColors(0xFFA8C7FA, 0xFF9FE5CF, 0xFFD9C2FA, 0xFFFAB8CC),
        shading = Shading.Solid,
        fillAlpha = 0.9f,
    )
    4 -> OrbTheme(
        hexColors(0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF),
        shading = Shading.Solid,
        style = OrbStyle.Glass,
        background = Brush.verticalGradient(listOf(Color(0xFF1B1F3B), Color(0xFF4A2A6B))),
    )
    5 -> OrbTheme(
        hexColors(0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF),
        shading = Shading.Solid,
        fillAlpha = 0.5f,
        background = SolidColor(Color(0xFF1C1B1F)),
    )
    6 -> OrbTheme(
        hexColors(0xFFFFE29A, 0xFFFFB199, 0xFFFF7EB3, 0xFFFFFFFF),
        fillAlpha = 0.92f,
        background = Brush.verticalGradient(listOf(Color(0xFFFF9966), Color(0xFF6A3093))),
    )
    7 -> OrbTheme(
        hexColors(0xFFFF9EBB, 0xFFFFC1D6, 0xFFF77AA3, 0xFFFFB3C9),
        fillAlpha = 0.9f,
        centerDot = true,
    )
    8 -> OrbTheme(
        hexColors(0xFF00E5FF, 0xFF39FF88, 0xFFB388FF, 0xFFFF4081),
        shading = Shading.Solid,
        style = OrbStyle.Neon,
        background = SolidColor(Color(0xFF0A0A14)),
    )
    9 -> OrbTheme(DefaultOrbColors, shading = Shading.Solid)
    // Pixel Style: rojo arriba, amarillo a la derecha, verde abajo y azul a la izquierda, con brillo de esfera
    10 -> OrbTheme(
        colors = hexColors(0xFFFF2D1F, 0xFFFFC400, 0xFF00D95F, 0xFF1F7BFF),
        highlights = hexColors(0xFFFF8A7A, 0xFFFFF07A, 0xFF7DFFB0, 0xFF8CC4FF),
        shading = Shading.Radial,
        fillAlpha = 0.96f,
    )
    11 -> OrbTheme(DefaultOrbColors, shading = Shading.Solid, style = OrbStyle.Pixel)
    else -> OrbTheme(DefaultOrbColors)
}

/** Bleach, Bankai exprés de 1,7 segundos (antes 2,6): duración total de la coreografía, y un toque la salta */
private const val StartupMs = 1700

/** Kakashi espera un respiro (120 ms) tras el final de los orbes para que la galería se componga bajo el telón antes de que caiga */
private const val ContentDelayMs = 120L

// ------------------------------------------------------------------ Curvas de movimiento

private fun clamp01(x: Float) = x.coerceIn(0f, 1f)

/** Naruto carga el Rasengan entre dos instantes: de 0 a 1 dentro del tramo [from, to] */
private fun span(t: Float, from: Float, to: Float) = clamp01((t - from) / (to - from))

private fun easeOutCubic(x: Float): Float {
    val u = 1f - x
    return 1f - u * u * u
}

private fun easeInCubic(x: Float) = x * x * x

private fun easeInOutCubic(x: Float): Float {
    if (x < 0.5f) return 4f * x * x * x
    val u = -2f * x + 2f
    return 1f - u * u * u / 2f
}

/** Luffy se estira y rebota: sobrepasa el 1 antes de asentarse */
private fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val u = x - 1f
    return 1f + c3 * u * u * u + c1 * u * u
}

// ------------------------------------------------------------------ Formas (radio 1, centradas en el origen)

/** Pikachu en forma de panal: hexágono de esquinas redondeadas, el orbe amarillo del arranque */
private fun roundedHexagon(): Path {
    val n = 6
    val rounding = 0.26f
    val rad = (PI / 180.0).toFloat()
    val vx = FloatArray(n) { cos((-90f + it * 60f) * rad) }
    val vy = FloatArray(n) { sin((-90f + it * 60f) * rad) }
    val path = Path()
    for (i in 0 until n) {
        val prev = (i + n - 1) % n
        val next = (i + 1) % n
        val inX = vx[i] + (vx[prev] - vx[i]) * rounding
        val inY = vy[i] + (vy[prev] - vy[i]) * rounding
        val outX = vx[i] + (vx[next] - vx[i]) * rounding
        val outY = vy[i] + (vy[next] - vy[i]) * rounding
        if (i == 0) path.moveTo(inX, inY) else path.lineTo(inX, inY)
        path.quadraticBezierTo(vx[i], vy[i], outX, outY)
    }
    path.close()
    return path
}

/** Trébol de la suerte de Asta (Black Clover): cuatro lóbulos en las diagonales, el orbe azul del arranque */
private fun cloverShape(): Path {
    val steps = 96
    val path = Path()
    for (i in 0..steps) {
        val a = 2f * PI.toFloat() * i / steps
        val r = 1f - 0.13f * cos(4f * a)
        val x = r * cos(a)
        val y = r * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/** Sailor Moon pule la esfera: el pincel de cada orbe según la temporada (brillo arriba a la izquierda, como en el icono) */
private fun orbBrush(theme: OrbTheme, i: Int): Brush {
    val base = theme.colors[i]
    return when (theme.shading) {
        Shading.Linear -> Brush.linearGradient(
            colors = listOf(lerp(base, Color.White, 0.32f), base),
            start = Offset(-1f, -1f),
            end = Offset(1f, 1f),
        )
        Shading.Radial -> Brush.radialGradient(
            colors = listOf(theme.highlights.getOrElse(i) { lerp(base, Color.White, 0.4f) }, base),
            center = Offset(-0.3f, -0.4f),
            radius = 1.5f,
        )
        Shading.Solid -> SolidColor(base)
    }
}

/** Dibuja una de las cuatro formas (0 píldora, 1 hexágono, 2 círculo, 3 trébol) con el pincel y el estilo dados */
private fun DrawScope.drawOrb(index: Int, brush: Brush, alpha: Float, style: DrawStyle, hexagon: Path, clover: Path) {
    when (index) {
        0 -> withTransform({ rotate(-30f, Offset.Zero) }) {
            drawRoundRect(
                brush = brush,
                topLeft = Offset(-0.58f, -0.95f),
                size = Size(1.16f, 1.9f),
                cornerRadius = CornerRadius(0.58f, 0.58f),
                alpha = alpha,
                style = style,
            )
        }
        1 -> drawPath(hexagon, brush, alpha = alpha, style = style)
        2 -> drawCircle(brush, radius = 0.95f, center = Offset.Zero, alpha = alpha, style = style)
        else -> drawPath(clover, brush, alpha = alpha, style = style)
    }
}

/** Todoroki comprueba qué casillas toca el hielo: ¿está el punto (x, y) dentro de la forma [shape]? Sirve para el estilo Pixel Art */
private fun insideShape(shape: Int, x: Float, y: Float): Boolean = when (shape) {
    0 -> {
        val a = 30f * (PI / 180.0).toFloat()
        val xr = x * cos(a) - y * sin(a)
        val yr = x * sin(a) + y * cos(a)
        val dy = maxOf(abs(yr) - 0.37f, 0f)
        xr * xr + dy * dy <= 0.58f * 0.58f
    }
    1 -> abs(x) <= 0.866f && abs(x * 0.5f + y * 0.866f) <= 0.866f && abs(x * 0.5f - y * 0.866f) <= 0.866f
    2 -> x * x + y * y <= 0.95f * 0.95f
    else -> sqrt(x * x + y * y) <= 1f - 0.13f * cos(4f * atan2(y, x))
}

private const val PixelGrid = 11
private const val PixelHalf = 1.05f

/** Casillas (pares x, y) de una forma, listas para pintar píxeles: se calculan una sola vez */
private fun pixelCells(shape: Int): FloatArray {
    val cell = 2f * PixelHalf / PixelGrid
    val out = ArrayList<Float>()
    for (gy in 0 until PixelGrid) {
        for (gx in 0 until PixelGrid) {
            val x = -PixelHalf + (gx + 0.5f) * cell
            val y = -PixelHalf + (gy + 0.5f) * cell
            if (insideShape(shape, x, y)) {
                out.add(x)
                out.add(y)
            }
        }
    }
    return out.toFloatArray()
}

// ------------------------------------------------------------------ Pantalla

/**
 * Sailor Moon: ¡por el poder del prisma lunar, la galería se abre!
 * Pone la animación por encima de [content] al arrancar. La galería empieza a componerse a los 450 ms (para no atascar
 * los primeros fotogramas) y la animación se retira sola. Solo se muestra si está activa en Ajustes y las animaciones están encendidas.
 */
@Composable
fun StartupGate(settings: AppSettings, content: @Composable () -> Unit) {
    var showSplash by rememberSaveable { mutableStateOf(settings.startupAnim && settings.animations) }
    var contentReady by rememberSaveable { mutableStateOf(!showSplash) }
    if (contentReady) content()
    if (showSplash) {
        StartupAnimation(
            iconIndex = settings.appIcon,
            onEmerged = { contentReady = true },
            onFinished = { showSplash = false },
        )
    }
}

@Composable
private fun StartupAnimation(iconIndex: Int, onEmerged: () -> Unit, onFinished: () -> Unit) {
    val themeBackground = MaterialTheme.colorScheme.background
    val progress = remember { Animatable(0f) }
    val skipFade = remember { Animatable(0f) }
    var skipped by remember { mutableStateOf(false) }
    val hexagon = remember { roundedHexagon() }
    val clover = remember { cloverShape() }
    // Temporada del icono elegido: colores, estilo y fondo propios
    val theme = orbThemeFor(iconIndex)
    val brushes = remember(iconIndex, theme.colors) { List(4) { orbBrush(theme, it) } }
    val pixels = remember { Array(4) { pixelCells(it) } }

    // Rock Lee corre su vuelta completa; si el usuario toca, el telón cae en un instante
    LaunchedEffect(skipped) {
        if (!skipped) {
            progress.animateTo(
                1f,
                tween(((1f - progress.value) * StartupMs).toInt().coerceAtLeast(1), easing = LinearEasing),
            )
            // La coreografía termina completa, sin que la galería le robe fotogramas; solo entonces se compone la app bajo el telón
            onEmerged()
            delay(ContentDelayMs)
            skipFade.animateTo(1f, tween(260, easing = LinearEasing))
        } else {
            onEmerged()
            skipFade.animateTo(1f, tween(220, easing = LinearEasing))
        }
        onFinished()
    }
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 1f - skipFade.value
            }
            // Siempre empieza con el fondo normal de la app (el mismo del telón del sistema); el degradado propio del icono se funde encima
            .background(themeBackground)
            .pointerInput(Unit) { detectTapGestures { skipped = true } },
    ) {
        theme.background?.let { bg ->
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = easeInOutCubic(span(progress.value, 0f, 0.2f)) }
                    .background(bg),
            )
        }
        Canvas(Modifier.fillMaxSize()) {
            val t = progress.value
            val m = minOf(size.width, size.height)
            val center = Offset(size.width / 2f, size.height / 2f)
            val unit = m * 0.052f
            val orbit = m * 0.125f

            val leave = span(t, 0.62f, 0.93f)
            val turn = easeInOutCubic(leave) * 90f
            val hold = span(t, 0.20f, 0.62f)
            val breathe = 1f + 0.035f * sin(hold * 2f * PI.toFloat() * 2f) * (1f - leave)

            // Equipo 7 en formación: arriba el rojo, a la derecha el amarillo, abajo el verde y a la izquierda el azul (como en el icono)
            val baseAngles = floatArrayOf(-90f, 0f, 90f, 180f)
            for (i in 0 until 4) {
                val e = span(t, 0.03f * i, 0.03f * i + 0.17f)
                val grow = easeOutBack(e)
                val shrink = 1f - easeInCubic(span(leave, 0.15f, 1f))
                val s = unit * grow * shrink * breathe
                if (s < 0.5f) continue
                val dist = orbit * easeOutCubic(e) * (1f - 0.2f * easeInOutCubic(leave))
                val angle = (baseAngles[i] + turn) * (PI / 180.0).toFloat()
                val px = center.x + dist * cos(angle)
                val py = center.y + dist * sin(angle)
                val a = clamp01(e * 3f)
                withTransform({
                    translate(px, py)
                    rotate(turn + 14f * hold * (1f - leave) * (if (i % 2 == 0) 1f else -1f), Offset.Zero)
                    scale(s, s, Offset.Zero)
                }) {
                    val brush = brushes[i]
                    val fa = a * theme.fillAlpha
                    when (theme.style) {
                        OrbStyle.Fill -> drawOrb(i, brush, fa, Fill, hexagon, clover)
                        OrbStyle.Outline -> drawOrb(i, brush, a, Stroke(0.14f), hexagon, clover)
                        OrbStyle.Glass -> {
                            drawOrb(i, brush, a * 0.2f, Fill, hexagon, clover)
                            drawOrb(i, brush, a * 0.75f, Stroke(0.07f), hexagon, clover)
                        }
                        // Neón: relleno apenas visible, un halo ancho y suave y el filo brillante encima
                        OrbStyle.Neon -> {
                            drawOrb(i, brush, a * 0.12f, Fill, hexagon, clover)
                            drawOrb(i, brush, a * 0.28f, Stroke(0.34f), hexagon, clover)
                            drawOrb(i, brush, a, Stroke(0.12f), hexagon, clover)
                        }
                        // Pixel Art: cada forma se arma con casillas cuadradas, como el icono
                        OrbStyle.Pixel -> {
                            val cell = 2f * PixelHalf / PixelGrid
                            val side = cell * 0.92f
                            val cells = pixels[i]
                            var k = 0
                            while (k < cells.size) {
                                drawRect(
                                    brush = brush,
                                    topLeft = Offset(cells[k] - side / 2f, cells[k + 1] - side / 2f),
                                    size = Size(side, side),
                                    alpha = fa,
                                )
                                k += 2
                            }
                        }
                    }
                }
            }
            // Puntito blanco del icono Sakura: aparece con los orbes y se encoge con ellos
            if (theme.centerDot) {
                val dot = unit * 0.42f * easeOutBack(span(t, 0.10f, 0.30f)) * (1f - easeInCubic(span(leave, 0.15f, 1f))) * breathe
                if (dot > 0.5f) {
                    drawCircle(Color.White, radius = dot, center = center, alpha = clamp01(span(t, 0.10f, 0.20f) * 3f))
                }
            }
        }
    }
}
