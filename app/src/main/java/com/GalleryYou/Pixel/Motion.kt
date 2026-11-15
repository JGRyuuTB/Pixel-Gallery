package com.GalleryYou.Pixel

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Mensaje oculto (rot13): Zngrevny Qrfvta ab rf zntvn, rf Rnfvat ohra cynargnqn
 * Motion de Material 3: las mismas curvas y duraciones en toda la app, como el mismo opening en cada episodio.
 */
object Motion {
    /** Easing enfatizado: sale rápido y aterriza suave (movimientos que cambian de sitio) */
    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    /** Enfatizado al entrar: llega a pantalla frenando */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    /** Enfatizado al salir: se va acelerando */
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Transformación de contenedor (abrir foto o vídeo): arranca con calma, no se come todo el movimiento al principio y aterriza suave */
    val Container = CubicBezierEasing(0.3f, 0f, 0.1f, 1f)
    /** Cerrar: simétrica y suave, para que se vea el viaje de vuelta a la miniatura */
    val ContainerExit = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    const val ContainerOpen = 480
    const val ContainerClose = 380

    const val Short = 200
    const val Medium = 300
    const val Long = 450
}

/**
 * Progreso de apertura del visor (0 = miniatura, 1 = pantalla completa). MainActivity lo publica y los paneles
 * del reproductor lo leen para desvanecerse antes de que la tarjeta se encoja, como las capas de un jutsu que se disipan.
 */
object ViewerMotion {
    var progress: () -> Float = { 1f }

    /** Los paneles solo se ven en la segunda mitad del viaje: aparecen al final al abrir y se van primero al cerrar */
    fun chromeAlpha(): Float = ((progress() - 0.55f) / 0.45f).coerceIn(0f, 1f)
}

/**
 * Eje compartido horizontal de Material (shared axis X): la pantalla nueva entra deslizando un poco y apareciendo,
 * la anterior se va en sentido contrario. [forward] = ir más adentro; false = volver.
 */
fun sharedAxisX(forward: Boolean, enabled: Boolean = true): ContentTransform {
    if (!enabled) return EnterTransition.None togetherWith ExitTransition.None
    val dir = if (forward) 1 else -1
    return (
        slideInHorizontally(tween(Motion.Medium, easing = Motion.Emphasized)) { w -> dir * w / 10 } +
            fadeIn(tween(210, delayMillis = 90, easing = LinearOutSlowInEasing))
        ) togetherWith (
        slideOutHorizontally(tween(Motion.Medium, easing = Motion.Emphasized)) { w -> -dir * w / 10 } +
            fadeOut(tween(90, easing = FastOutLinearInEasing))
        )
}

/**
 * Fundido a través (fade through) de Material: para cambiar entre secciones hermanas (las pestañas).
 * Sale la anterior, y la nueva entra un pelín más pequeña y crece hasta su tamaño.
 */
fun fadeThrough(enabled: Boolean = true): ContentTransform {
    if (!enabled) return EnterTransition.None togetherWith ExitTransition.None
    return (
        fadeIn(tween(210, delayMillis = 90, easing = LinearOutSlowInEasing)) +
            scaleIn(tween(Motion.Medium, delayMillis = 60, easing = Motion.Emphasized), initialScale = 0.94f)
        ) togetherWith fadeOut(tween(90, easing = FastOutLinearInEasing))
}

/** Escala de entrada/salida para elementos que aparecen sueltos (botones, insignias, paneles) */
fun popIn(initialScale: Float = 0.8f) = fadeIn(tween(Motion.Short, easing = LinearOutSlowInEasing)) +
    scaleIn(tween(Motion.Medium, easing = Motion.EmphasizedDecelerate), initialScale = initialScale)

fun popOut(targetScale: Float = 0.85f) = fadeOut(tween(120, easing = FastOutLinearInEasing)) +
    scaleOut(tween(150, easing = Motion.EmphasizedAccelerate), targetScale = targetScale)

/**
 * Respuesta al tocar: el elemento se hunde un poquito mientras el dedo está encima y rebota al soltar.
 * No consume el toque, así que no estorba al clic, al clic largo ni al scroll. Respeta el ajuste "Animaciones".
 * Ponlo antes del clickable para que escale también la onda del ripple.
 */
fun Modifier.pressFeedback(target: Float = 0.97f): Modifier = composed {
    val enabled = LocalAnimations.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) target else 1f,
        animationSpec = if (enabled) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) else snap(),
        label = "pressFeedback",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                // Se observa en la fase inicial sin consumir nada: Chopper mira, no toca
                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}
