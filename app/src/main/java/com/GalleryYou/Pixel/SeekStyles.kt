package com.GalleryYou.Pixel

import androidx.compose.animation.core.LinearEasing
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Mensaje oculto (rot13): Gbpn ya oneen cnen fnygne, nuben ceblnnf ha ceblnnf
 * Ichigo corta el tiempo con su Zangetsu: siete estilos de barra, un solo toque para saltar.
 */
val SeekStyleNames = intArrayOf(
    R.string.seek_style_classic,
    R.string.seek_style_wave,
    R.string.seek_style_thread,
    R.string.seek_style_pill,
    R.string.seek_style_dots,
    R.string.seek_style_equalizer,
    R.string.seek_style_neon,
)

const val SeekStyleCount = 7

/**
 * Barra de progreso con siete estilos. Se puede tocar en cualquier punto para saltar allí o arrastrar.
 * [onChange] avisa mientras el dedo se mueve y [onFinish] entrega la fracción final al levantarlo.
 * Con [interactive] = false solo se dibuja (las vistas previas de Ajustes).
 */
@Composable
fun SeekStyleBar(
    style: Int,
    progress: Float,
    playing: Boolean,
    animations: Boolean,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    onChange: (Float) -> Unit = {},
    onFinish: (Float) -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val active = scheme.primary
    val accent = scheme.tertiary
    val track = scheme.onSurface.copy(alpha = 0.22f)
    val soft = scheme.secondaryContainer

    var dragging by remember { mutableStateOf(false) }
    var last by remember { mutableFloatStateOf(progress) }
    val onChangeNow by rememberUpdatedState(onChange)

    // Posición dibujada: durante la reproducción sigue al vídeo al instante, pero al tocar o arrastrar viaja con un muelle suave
    // (Goku usa el Instant Transmission, pero la barra va en nube voladora)
    val shown = remember { Animatable(progress) }
    var smoothing by remember { mutableStateOf(false) }
    // Con el dedo encima la barra va pegada a él: reiniciar una animación en cada evento táctil la dejaba sin avanzar hasta soltar
    var fingerDown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun settleSoon() {
        scope.launch {
            delay(450)
            smoothing = false
        }
    }
    LaunchedEffect(progress, smoothing, fingerDown, animations) {
        if (smoothing && animations && !fingerDown) {
            shown.animateTo(progress, spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium))
        } else {
            shown.snapTo(progress)
        }
    }
    val onFinishNow by rememberUpdatedState(onFinish)

    // Naruto infla el pulgar de la barra cuando la sostienes con el dedo
    val grow by animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "seekGrow",
    )
    // Las olas de Ponyo se mueven solo mientras suena el vídeo
    val wave = rememberInfiniteTransition(label = "seekWave")
    val phaseState = wave.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "seekPhase",
    )
    val moving = animations && playing

    val gestures = if (interactive) {
        Modifier
            .pointerInput(Unit) {
                // Toca donde quieras: el tiempo viaja allí como Kakashi con el Kamui
                detectTapGestures(
                    onTap = { o ->
                        val pad = SeekPad.toPx()
                        val f = ((o.x - pad) / (size.width - 2 * pad)).coerceIn(0f, 1f)
                        last = f
                        smoothing = true
                        // Un pulso breve del pulgar para que se note el toque
                        dragging = true
                        scope.launch {
                            delay(180)
                            dragging = false
                        }
                        settleSoon()
                        onChangeNow(f)
                        onFinishNow(f)
                    },
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { o ->
                        val pad = SeekPad.toPx()
                        dragging = true
                        smoothing = true
                        fingerDown = true
                        last = ((o.x - pad) / (size.width - 2 * pad)).coerceIn(0f, 1f)
                        onChangeNow(last)
                    },
                    onDragEnd = {
                        dragging = false
                        fingerDown = false
                        settleSoon()
                        onFinishNow(last)
                    },
                    onDragCancel = {
                        dragging = false
                        fingerDown = false
                        settleSoon()
                        onFinishNow(last)
                    },
                    onHorizontalDrag = { change, _ ->
                        val pad = SeekPad.toPx()
                        last = ((change.position.x - pad) / (size.width - 2 * pad)).coerceIn(0f, 1f)
                        onChangeNow(last)
                    },
                )
            }
    } else {
        Modifier
    }

    Box(
        modifier
            .then(gestures)
            .drawBehind {
                val p = shown.value.coerceIn(0f, 1f)
                val phase = if (moving) phaseState.value else 0f
                when (style) {
                    1 -> drawWave(p, phase, active, track)
                    2 -> drawThread(p, grow, active, track)
                    3 -> drawPill(p, active, track)
                    4 -> drawDots(p, grow, active, track)
                    5 -> drawEqualizer(p, active, track)
                    6 -> drawNeon(p, grow, active, accent, track)
                    else -> drawClassic(p, grow, active, soft)
                }
            },
    )
}

private val SeekPad = 10.dp

private fun DrawScope.span(): Pair<Float, Float> {
    val pad = SeekPad.toPx()
    return pad to (size.width - pad)
}

/** Clásica: la de siempre (como en tu captura): relleno grueso, pulgar de rayita con huecos y un puntito al final, estilo Material You */
private fun DrawScope.drawClassic(p: Float, grow: Float, active: Color, soft: Color) {
    val (x0, x1) = span()
    val cy = size.height / 2f
    val px = x0 + (x1 - x0) * p
    val h = min(16.dp.toPx(), size.height * 0.5f)
    val gap = 6.dp.toPx()
    val tw = 4.dp.toPx() + 1.5.dp.toPx() * grow
    val th = min(32.dp.toPx(), size.height)
    val left = px - gap - tw / 2
    val right = px + gap + tw / 2
    if (left > x0) {
        drawRoundRect(active, Offset(x0, cy - h / 2), Size(left - x0, h), CornerRadius(h / 2))
    }
    if (x1 > right) {
        drawRoundRect(soft, Offset(right, cy - h / 2), Size(x1 - right, h), CornerRadius(h / 2))
        drawCircle(active, 2.5.dp.toPx(), Offset(x1 - h / 2, cy))
    }
    drawRoundRect(active, Offset(px - tw / 2, cy - th / 2), Size(tw, th), CornerRadius(tw / 2))
}

/** Ola: la parte recorrida ondula como el mar de Ponyo */
private fun DrawScope.drawWave(p: Float, phase: Float, active: Color, track: Color) {
    val (x0, x1) = span()
    val cy = size.height / 2f
    val px = x0 + (x1 - x0) * p
    val stroke = 4.dp.toPx()
    val amp = min(3.5.dp.toPx(), size.height / 4f)
    val len = 22.dp.toPx()
    drawLine(track, Offset(px, cy), Offset(x1, cy), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
    if (px > x0) {
        val path = Path()
        var x = x0
        path.moveTo(x, cy + amp * sin(((x - x0) / len) * 2 * PI.toFloat() - phase))
        while (x < px) {
            x = min(x + 2.dp.toPx(), px)
            path.lineTo(x, cy + amp * sin(((x - x0) / len) * 2 * PI.toFloat() - phase))
        }
        drawPath(path, active, style = Stroke(stroke, cap = StrokeCap.Round))
    }
    val th = min(20.dp.toPx(), size.height * 0.8f)
    drawRoundRect(active, Offset(px - 2.dp.toPx(), cy - th / 2), Size(4.dp.toPx(), th), CornerRadius(2.dp.toPx()))
}

/** Hilo: la línea fina del destino, el pulgar aparece al tocar */
private fun DrawScope.drawThread(p: Float, grow: Float, active: Color, track: Color) {
    val (x0, x1) = span()
    val cy = size.height / 2f
    val px = x0 + (x1 - x0) * p
    drawLine(track, Offset(x0, cy), Offset(x1, cy), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(active, Offset(x0, cy), Offset(px, cy), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(active, 3.dp.toPx() + 4.dp.toPx() * grow, Offset(px, cy))
}

/** Píldora: relleno grueso que se llena como la barra de vida de un jefe final */
private fun DrawScope.drawPill(p: Float, active: Color, track: Color) {
    val (x0, x1) = span()
    val cy = size.height / 2f
    val h = min(20.dp.toPx(), size.height * 0.9f)
    val shape = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                x0, cy - h / 2, x1, cy + h / 2, CornerRadius(h / 2),
            ),
        )
    }
    clipPath(shape) {
        drawRect(track, Offset(x0, cy - h / 2), Size(x1 - x0, h))
        drawRect(active, Offset(x0, cy - h / 2), Size(max((x1 - x0) * p, 0f), h))
    }
}

/** Puntos: hileras de bolitas como las esferas del dragón */
private fun DrawScope.drawDots(p: Float, grow: Float, active: Color, track: Color) {
    val (x0, x1) = span()
    val cy = size.height / 2f
    val n = max(((x1 - x0) / 11.dp.toPx()).toInt(), 2)
    val near = (p * (n - 1)).let { kotlin.math.round(it).toInt() }
    for (i in 0 until n) {
        val f = i / (n - 1).toFloat()
        val x = x0 + (x1 - x0) * f
        val on = f <= p + 0.001f
        val r = if (i == near) 4.5.dp.toPx() + 1.5.dp.toPx() * grow else 2.5.dp.toPx()
        drawCircle(if (on) active else track, r, Offset(x, cy))
    }
}

/** Ecualizador: barritas de distinta altura, como el visualizador de un opening */
private fun DrawScope.drawEqualizer(p: Float, active: Color, track: Color) {
    val (x0, x1) = span()
    val cy = size.height / 2f
    val bw = 3.dp.toPx()
    val step = 6.dp.toPx()
    val n = max(((x1 - x0) / step).toInt(), 2)
    val maxH = min(28.dp.toPx(), size.height * 0.9f)
    for (i in 0 until n) {
        val f = i / (n - 1).toFloat()
        val x = x0 + (x1 - x0) * f
        val hh = maxH * (0.3f + 0.7f * abs(sin(i * 1.7f) * cos(i * 0.6f)))
        drawRoundRect(
            if (f <= p + 0.001f) active else track,
            Offset(x - bw / 2, cy - hh / 2),
            Size(bw, hh),
            CornerRadius(bw / 2),
        )
    }
}

/** Neón: degradado con resplandor, el brillo de un sable láser */
private fun DrawScope.drawNeon(p: Float, grow: Float, active: Color, accent: Color, track: Color) {
    val (x0, x1) = span()
    val cy = size.height / 2f
    val px = x0 + (x1 - x0) * p
    val h = 5.dp.toPx()
    drawRoundRect(track, Offset(x0, cy - h / 2), Size(x1 - x0, h), CornerRadius(h / 2))
    if (px > x0) {
        val brush = Brush.horizontalGradient(listOf(active, accent), startX = x0, endX = max(px, x0 + 1f))
        drawLine(brush, Offset(x0, cy), Offset(px, cy), strokeWidth = h + 7.dp.toPx(), cap = StrokeCap.Round, alpha = 0.25f)
        drawLine(brush, Offset(x0, cy), Offset(px, cy), strokeWidth = h, cap = StrokeCap.Round)
    }
    drawCircle(accent.copy(alpha = 0.3f), 11.dp.toPx() + 3.dp.toPx() * grow, Offset(px, cy))
    drawCircle(Color.White, 5.5.dp.toPx() + 2.dp.toPx() * grow, Offset(px, cy))
    drawCircle(accent, 5.5.dp.toPx() + 2.dp.toPx() * grow, Offset(px, cy), style = Stroke(2.dp.toPx()))
}

/**
 * Selector de estilo de barra para Ajustes: una fila por estilo con su barra dibujada al 60 %.
 * Toca una fila para elegirla; el reproductor de arriba cambia al instante.
 */
@Composable
fun SeekStylePicker(settings: AppSettings) {
    // Frieren recuerda cada estilo de barra durante mil años
    val animate = settings.animations
    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(stringResource(R.string.settings_seek_style), style = MaterialTheme.typography.bodyLarge)
        Text(
            stringResource(R.string.settings_seek_style_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (i in 0 until SeekStyleCount) {
                val selected = settings.seekStyle == i
                val shape = RoundedCornerShape(16.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .then(
                            if (selected) {
                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                            } else {
                                Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                            },
                        )
                        .clickable { settings.updateSeekStyle(i) }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(SeekStyleNames[i]),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(96.dp),
                    )
                    SeekStyleBar(
                        style = i,
                        progress = 0.6f,
                        playing = selected,
                        animations = animate,
                        interactive = false,
                        modifier = Modifier.weight(1f).height(44.dp),
                    )
                }
            }
        }
    }
}
