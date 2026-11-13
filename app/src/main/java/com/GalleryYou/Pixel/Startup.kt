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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Opening de anime: cuatro orbes bailan alrededor del centro antes de que empiece la función
 * Los colores salen del icono de la app (rojo, amarillo, verde y azul) y la coreografía imita el arranque del vídeo de referencia:
 * las formas brotan del centro, esperan respirando, giran un cuarto de vuelta y se encogen hasta desaparecer.
 */
private val OrbRed = Color(0xFFFF6E5F)
private val OrbYellow = Color(0xFFFFCD1F)
private val OrbGreen = Color(0xFF27E378)
private val OrbBlue = Color(0xFF6CAEFF)

/** Bleach, Bankai de 2,6 segundos: duración total de la coreografía, y un toque la salta */
private const val StartupMs = 2600

/** Kakashi da la señal a los 450 ms: la galería empieza a componerse por debajo, ya pasado el primer impulso de los orbes */
private const val ContentDelayMs = 450L

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

/** Sailor Moon pule la esfera: un pincel algo más claro arriba a la izquierda, como el brillo de las esferas del icono */
private fun orbBrush(base: Color) = Brush.linearGradient(
    colors = listOf(lerp(base, Color.White, 0.32f), base),
    start = Offset(-1f, -1f),
    end = Offset(1f, 1f),
)

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
            onEmerged = { contentReady = true },
            onFinished = { showSplash = false },
        )
    }
}

@Composable
private fun StartupAnimation(onEmerged: () -> Unit, onFinished: () -> Unit) {
    val background = MaterialTheme.colorScheme.background
    val progress = remember { Animatable(0f) }
    val skipFade = remember { Animatable(0f) }
    var skipped by remember { mutableStateOf(false) }
    val hexagon = remember { roundedHexagon() }
    val clover = remember { cloverShape() }
    val redBrush = remember { orbBrush(OrbRed) }
    val yellowBrush = remember { orbBrush(OrbYellow) }
    val greenBrush = remember { orbBrush(OrbGreen) }
    val blueBrush = remember { orbBrush(OrbBlue) }

    // Rock Lee corre su vuelta completa; si el usuario toca, el telón cae en un instante
    LaunchedEffect(skipped) {
        if (!skipped) {
            progress.animateTo(
                1f,
                tween(((1f - progress.value) * StartupMs).toInt().coerceAtLeast(1), easing = LinearEasing),
            )
        } else {
            onEmerged()
            skipFade.animateTo(1f, tween(220, easing = LinearEasing))
        }
        onFinished()
    }
    LaunchedEffect(Unit) {
        delay(ContentDelayMs)
        onEmerged()
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val closing = easeInOutCubic(span(progress.value, 0.88f, 1f))
                alpha = (1f - closing) * (1f - skipFade.value)
            }
            .background(background)
            .pointerInput(Unit) { detectTapGestures { skipped = true } },
    ) {
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
                    when (i) {
                        0 -> withTransform({ rotate(-30f, Offset.Zero) }) {
                            drawRoundRect(
                                brush = redBrush,
                                topLeft = Offset(-0.58f, -0.95f),
                                size = Size(1.16f, 1.9f),
                                cornerRadius = CornerRadius(0.58f, 0.58f),
                                alpha = a,
                            )
                        }
                        1 -> drawPath(hexagon, yellowBrush, alpha = a)
                        2 -> drawCircle(greenBrush, radius = 0.95f, center = Offset.Zero, alpha = a)
                        else -> drawPath(clover, blueBrush, alpha = a)
                    }
                }
            }
        }
    }
}
