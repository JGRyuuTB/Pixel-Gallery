package com.GalleryYou.Pixel

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.LinearProgressIndicator
import android.content.Intent
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animate
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.spring
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

data class ViewerRequest(val items: List<MediaFile>, val start: Int)

private fun share(context: Context, m: MediaFile) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = m.mime
        putExtra(Intent.EXTRA_STREAM, m.uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

private fun initialAspect(m: MediaFile): Float =
    if (m.width > 0 && m.height > 0) m.width.toFloat() / m.height else 16f / 9f

// ------------------------------------------------------------------ Visor

@Composable
fun ViewerScreen(
    request: ViewerRequest,
    settings: AppSettings,
    onClose: () -> Unit,
    onTrash: (MediaFile) -> Unit,
    onPageChanged: (android.net.Uri) -> Unit = {},
    onDragDismiss: () -> Unit = onClose,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val items = request.items
    val pagerState = rememberPagerState(initialPage = request.start.coerceIn(0, items.lastIndex)) {
        items.size
    }
    var chrome by remember { mutableStateOf(true) }
    var zoomed by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    val current = items[pagerState.currentPage.coerceIn(0, items.lastIndex)]
    LaunchedEffect(current.uri) { onPageChanged(current.uri) }
    val darkIcons = colors.surface.luminance() > 0.5f
    val scope = rememberCoroutineScope()
    var drag by remember { mutableFloatStateOf(0f) }
    var viewHeight by remember { mutableIntStateOf(1) }
    val dismissPx = with(LocalDensity.current) { 140.dp.toPx() }
    val backdrop = rememberBackdropState(settings)
    val ambient = rememberBackdropState(settings)

    ImmersiveEffect(hideBars = !chrome, darkIcons = darkIcons)
    LaunchedEffect(pagerState.currentPage) { zoomed = false }

    CompositionLocalProvider(
        LocalContentColor provides colors.onSurface,
        LocalAmbientBackdrop provides ambient,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .onSizeChanged { viewHeight = it.height }
                .then(
                    if (!zoomed) {
                        // Deslizar hacia abajo para cerrar la foto o el video.
                        Modifier.pointerInput(Unit) {
                            val tracker = VelocityTracker()
                            detectVerticalDragGestures(
                                onDragStart = { tracker.resetTracking() },
                                onVerticalDrag = { change, amount ->
                                    tracker.addPosition(change.uptimeMillis, change.position)
                                    change.consume()
                                    drag = (drag + amount).coerceAtLeast(0f)
                                },
                                onDragEnd = {
                                    val velocity = tracker.calculateVelocity().y
                                    scope.launch {
                                        if (drag > dismissPx || velocity > 1400f) {
                                            animate(drag, viewHeight.toFloat(), animationSpec = tween(200)) { value, _ ->
                                                drag = value
                                            }
                                            onDragDismiss()
                                        } else {
                                            animate(
                                                drag,
                                                0f,
                                                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                                            ) { value, _ -> drag = value }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        animate(drag, 0f) { value, _ -> drag = value }
                                    }
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            // Fondo que se desvanece al arrastrar, para ver la galería detrás.
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = (1f - drag / (size.height * 0.6f)).coerceIn(0f, 1f) }
                    .background(Brush.verticalGradient(listOf(colors.surfaceContainerHigh, colors.surface))),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = (drag / size.height).coerceIn(0f, 1f)
                        translationY = drag
                        scaleX = 1f - 0.28f * p
                        scaleY = 1f - 0.28f * p
                    },
            ) {
            // Todo lo que queda dentro de este Box es lo que se ve a través del cristal de las barras.
            Box(Modifier.fillMaxSize().backdropSource(backdrop)) {
                if (backdrop.enabled) {
                    // Fondo ambiental: la portada del archivo actual, muy borrosa, para que el cristal tenga color.
                    Box(Modifier.fillMaxSize().backdropSource(ambient)) {
                        Crossfade(targetState = current.uri, label = "ambient") { uri ->
                            AsyncImage(
                                model = thumbRequest(uri, 256),
                                imageLoader = LocalGalleryLoader.current,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = 1.35f
                                        scaleY = 1.35f
                                    }
                                    .blur(44.dp, BlurredEdgeTreatment.Unbounded),
                            )
                        }
                        Box(Modifier.fillMaxSize().background(colors.surface.copy(alpha = 0.50f)))
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !zoomed,
                    modifier = Modifier.fillMaxSize(),
                    key = { items[it].uri.toString() },
                ) { page ->
                    val m = items[page]
                    if (m.isVideo) {
                        VideoPage(
                            m = m,
                            active = pagerState.settledPage == page,
                            chrome = chrome,
                            settings = settings,
                            onTap = { chrome = !chrome },
                        )
                    } else {
                        PhotoPage(
                            m = m,
                            onTap = { chrome = !chrome },
                            onZoomChange = { zoomed = it },
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = chrome,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .backdropBlur(backdrop, colors.surface.copy(alpha = 0.45f))
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClose) {
                        Icon(AppIcons.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                    Column(Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(
                            formatDateSkeleton(current.dateMillis, "dMMMyyyy"),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            formatDateSkeleton(current.dateMillis, "jm"),
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(onClick = { share(context, current) }) {
                        Icon(AppIcons.Share, contentDescription = stringResource(R.string.cd_share))
                    }
                    IconButton(onClick = { onTrash(current) }) {
                        Icon(AppIcons.Trash, contentDescription = stringResource(R.string.move_to_trash))
                    }
                    IconButton(onClick = { showInfo = true }) {
                        Icon(AppIcons.Info, contentDescription = stringResource(R.string.details))
                    }
                }
            }
            }
        }
    }

    if (showInfo) {
        InfoSheet(current) { showInfo = false }
    }
}

@Composable
private fun ImmersiveEffect(hideBars: Boolean, darkIcons: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = context.findActivity()?.window

    LaunchedEffect(hideBars, window) {
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (hideBars) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(window, darkIcons) {
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val prevStatus = controller?.isAppearanceLightStatusBars
        val prevNav = controller?.isAppearanceLightNavigationBars
        // Iconos de las barras que contrastan con el fondo del visor.
        controller?.isAppearanceLightStatusBars = darkIcons
        controller?.isAppearanceLightNavigationBars = darkIcons
        onDispose {
            if (controller != null) {
                controller.show(WindowInsetsCompat.Type.systemBars())
                if (prevStatus != null) controller.isAppearanceLightStatusBars = prevStatus
                if (prevNav != null) controller.isAppearanceLightNavigationBars = prevNav
            }
        }
    }
}

// ------------------------------------------------------------------ Foto con zoom

@Composable
private fun PhotoPage(m: MediaFile, onTap: () -> Unit, onZoomChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var box by remember { mutableStateOf(IntSize.Zero) }
    val zoomed = scale > 1.01f
    LaunchedEffect(zoomed) { onZoomChange(zoomed) }

    fun clampOffset(o: Offset, s: Float): Offset {
        val maxX = box.width * (s - 1f) / 2f
        val maxY = box.height * (s - 1f) / 2f
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }

    val request = remember(m.uri) { ImageRequest.Builder(context).data(m.uri).build() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { box = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1.01f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2.5f
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                // Pellizcar para ampliar; arrastrar solo cuando hay zoom (así no bloquea el deslizado entre fotos).
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val consumed = event.changes.any { it.isConsumed }
                        if (!consumed) {
                            val multi = event.changes.count { it.pressed } > 1
                            if (multi || scale > 1.01f) {
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                if (zoom != 1f || pan != Offset.Zero) {
                                    val newScale = (scale * zoom).coerceIn(1f, 6f)
                                    scale = newScale
                                    offset = if (newScale <= 1.01f) {
                                        Offset.Zero
                                    } else {
                                        clampOffset(offset + pan, newScale)
                                    }
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            }
                        }
                    } while (!consumed && event.changes.any { it.pressed })
                }
            },
    ) {
        AsyncImage(
            model = request,
            imageLoader = LocalGalleryLoader.current,
            contentDescription = m.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}

// ------------------------------------------------------------------ Marco del reproductor

/** Fondo ambiental del visor, usado por el cristal del panel de controles del video. */
val LocalAmbientBackdrop = androidx.compose.runtime.compositionLocalOf<BackdropState?> { null }

/** Video limpio, solo con esquinas redondeadas (sin borde). El borde vive en el panel de controles. */
@Composable
private fun PlayerFrame(
    width: Dp,
    height: Dp,
    scale: Float,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = ((scale - 0.85f) / 0.15f).coerceIn(0f, 1f)
            }
            .size(width, height)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        content()
    }
}

/** Valor 0..1 que va y viene mientras el video se reproduce, para animar el borde del panel. */
@Composable
private fun rememberBorderShift(mode: Int, animate: Boolean): State<Float>? {
    if (mode != 2 || !animate) return null
    return rememberInfiniteTransition(label = "panelBorder").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Reverse),
        label = "panelShift",
    )
}

/** Borde (del color del tema, fijo o animado) alrededor del panel de controles. */
private fun Modifier.panelBorder(
    mode: Int,
    shift: State<Float>?,
    primary: Color,
    tertiary: Color,
    corner: Dp,
): Modifier =
    if (mode == 0) {
        this
    } else {
        drawWithContent {
            drawContent()
            // El valor animado se lee al dibujar, sin recomponer la pantalla en cada fotograma.
            val s = shift?.value ?: 0f
            val brush = Brush.linearGradient(
                listOf(lerp(primary, tertiary, s), lerp(tertiary, primary, s)),
            )
            val stroke = 3.dp.toPx()
            drawRoundRect(
                brush = brush,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                // Se extiende por debajo del borde de la pantalla: solo se ven arriba y a los lados.
                size = Size(size.width - stroke, size.height + 80.dp.toPx()),
                cornerRadius = CornerRadius(corner.toPx() - stroke / 2f),
                style = Stroke(width = stroke),
            )
        }
    }

/** Reserva espacio para la barra superior y el panel de controles y ajusta el marco al video. */
@Composable
private fun VideoStage(
    aspect: Float,
    chrome: Boolean,
    settings: AppSettings,
    content: @Composable () -> Unit,
) {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPad by animateDpAsState(
        targetValue = if (chrome) statusTop + 72.dp else 24.dp,
        animationSpec = motion(settings.animations),
        label = "stageTop",
    )
    val bottomPad by animateDpAsState(
        targetValue = if (chrome) navBottom + 184.dp else 24.dp,
        animationSpec = motion(settings.animations),
        label = "stageBottom",
    )
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val appear by animateFloatAsState(
        targetValue = if (shown || !settings.animations) 1f else 0.9f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "stageAppear",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = topPad, bottom = bottomPad),
        contentAlignment = Alignment.Center,
    ) {
        if (maxWidth.value > 0f && maxHeight.value > 0f && aspect > 0f) {
            val boxAspect = maxWidth.value / maxHeight.value
            val w: Dp
            val h: Dp
            if (aspect >= boxAspect) {
                w = maxWidth
                h = maxWidth / aspect
            } else {
                h = maxHeight
                w = maxHeight * aspect
            }
            PlayerFrame(
                width = w,
                height = h,
                scale = appear,
                content = content,
            )
        }
    }
}

// ------------------------------------------------------------------ Video

@Composable
private fun VideoPage(
    m: MediaFile,
    active: Boolean,
    chrome: Boolean,
    settings: AppSettings,
    onTap: () -> Unit,
) {
    if (active) {
        VideoPlayerContent(m, chrome, settings, onTap)
    } else {
        // Mientras se desliza entre páginas solo se muestra la portada; el reproductor vive en la página activa.
        Box(Modifier.fillMaxSize()) {
            VideoStage(
                aspect = initialAspect(m),
                chrome = chrome,
                settings = settings,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = thumbRequest(m.uri, 1080),
                        imageLoader = LocalGalleryLoader.current,
                        contentDescription = m.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier.size(64.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(AppIcons.Play, null, Modifier.size(40.dp), tint = Color.White)
                    }
                }
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTap,
                    ),
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerContent(m: MediaFile, chrome: Boolean, settings: AppSettings, onTap: () -> Unit) {
    val context = LocalContext.current
    val exo = remember(m.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(m.uri))
            prepare()
            playWhenReady = settings.autoplay
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(m.durationMs) }
    var aspect by remember { mutableFloatStateOf(initialAspect(m)) }
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    var speed by remember { mutableFloatStateOf(1f) }
    var muted by remember { mutableStateOf(false) }
    var speedMenu by remember { mutableStateOf(false) }
    var flashDir by remember { mutableIntStateOf(0) }
    var lastDir by remember { mutableIntStateOf(1) }
    var flashTick by remember { mutableIntStateOf(0) }
    var loop by remember { mutableStateOf(false) }
    var holdBoost by remember { mutableStateOf(false) }
    // Indicador de brillo/volumen: 0 = ninguno, 1 = brillo, 2 = volumen.
    var hudKind by remember { mutableIntStateOf(0) }
    var hudValue by remember { mutableFloatStateOf(0f) }
    var hudTick by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val activity = remember { context.findActivity() }
    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    val originalBrightness = remember { activity?.window?.attributes?.screenBrightness ?: -1f }
    var brightness by remember {
        mutableFloatStateOf(
            if (originalBrightness >= 0f) {
                originalBrightness
            } else {
                try {
                    Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
                } catch (e: Exception) {
                    0.5f
                }
            },
        )
    }

    fun adjustSide(side: Int, delta: Float) {
        if (side < 0) {
            brightness = (brightness + delta).coerceIn(0.02f, 1f)
            activity?.window?.let { w ->
                val lp = w.attributes
                lp.screenBrightness = brightness
                w.attributes = lp
            }
            hudKind = 1
            hudValue = brightness
        } else {
            val cur = audio.getStreamVolume(AudioManager.STREAM_MUSIC) / maxVolume.toFloat()
            val target = (cur + delta).coerceIn(0f, 1f)
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, (target * maxVolume).roundToInt(), 0)
            hudKind = 2
            hudValue = target
        }
        hudTick++
    }

    // El brillo vuelve al del sistema al salir del video.
    DisposableEffect(activity) {
        onDispose {
            activity?.window?.let { w ->
                val lp = w.attributes
                lp.screenBrightness = originalBrightness
                w.attributes = lp
            }
        }
    }

    LaunchedEffect(hudTick) {
        if (hudKind != 0) {
            delay(900)
            hudKind = 0
        }
    }

    DisposableEffect(exo) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                ended = state == Player.STATE_ENDED
                if (state == Player.STATE_READY && exo.duration > 0) duration = exo.duration
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    aspect = videoSize.width * videoSize.pixelWidthHeightRatio / videoSize.height
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                failed = true
            }
        }
        exo.addListener(listener)
        onDispose {
            exo.removeListener(listener)
            exo.release()
        }
    }

    // Pausa el video si la app pasa a segundo plano.
    val owner = remember { context.findActivity() as? LifecycleOwner }
    DisposableEffect(owner, exo) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) exo.pause()
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    LaunchedEffect(exo) {
        while (true) {
            if (dragFraction == null) position = exo.currentPosition.coerceAtLeast(0L)
            delay(200)
        }
    }

    LaunchedEffect(flashTick) {
        if (flashDir != 0) {
            delay(550)
            flashDir = 0
        }
    }

    fun togglePlay() {
        if (exo.isPlaying) {
            exo.pause()
        } else {
            if (ended || exo.playbackState == Player.STATE_ENDED) exo.seekTo(0)
            exo.play()
        }
    }

    fun seekBy(deltaMs: Long) {
        val target = exo.currentPosition + deltaMs
        val max = if (duration > 0) duration else Long.MAX_VALUE
        exo.seekTo(target.coerceIn(0L, max))
    }

    Box(Modifier.fillMaxSize()) {
        VideoStage(
            aspect = aspect,
            chrome = chrome,
            settings = settings,
        ) {
            AndroidView(
                factory = { ctx ->
                    // PlayerView con TextureView (ver res/layout/view_player.xml) para poder recortar las esquinas.
                    (LayoutInflater.from(ctx).inflate(R.layout.view_player, null, false) as PlayerView).apply {
                        setPlayer(exo)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Toque simple: mostrar/ocultar controles. Doble toque: ±10 s en los lados, pausa en el centro.
        // Mantener pulsado: velocidad 2x mientras no se suelta. Deslizar en vertical por el borde
        // izquierdo ajusta el brillo y por el derecho el volumen (el centro sigue cerrando el visor).
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = {
                            holdBoost = true
                            exo.setPlaybackSpeed(2f)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onPress = {
                            tryAwaitRelease()
                            if (holdBoost) {
                                holdBoost = false
                                exo.setPlaybackSpeed(speed)
                            }
                        },
                        onDoubleTap = { pos ->
                            val w = size.width
                            when {
                                pos.x < w / 3f -> {
                                    seekBy(-10_000)
                                    lastDir = -1
                                    flashDir = -1
                                    flashTick++
                                }

                                pos.x > w * 2f / 3f -> {
                                    seekBy(10_000)
                                    lastDir = 1
                                    flashDir = 1
                                    flashTick++
                                }

                                else -> togglePlay()
                            }
                        },
                    )
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val side = when {
                            down.position.x < size.width * 0.22f -> -1
                            down.position.x > size.width * 0.78f -> 1
                            else -> 0
                        }
                        if (side == 0) return@awaitEachGesture
                        var totalY = 0f
                        var totalX = 0f
                        var accepted = false
                        val slop = viewConfiguration.touchSlop
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            val move = change.positionChange()
                            if (!accepted) {
                                totalY += move.y
                                totalX += move.x
                                if (abs(totalY) > slop && abs(totalY) > abs(totalX)) accepted = true
                            } else {
                                change.consume()
                                // Un deslizamiento por toda la altura cambia el 100 %.
                                adjustSide(side, -move.y / size.height * 1.2f)
                            }
                        }
                    }
                },
        )

        // Indicador de brillo / volumen.
        AnimatedVisibility(
            visible = hudKind != 0,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 96.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut(),
        ) {
            Row(
                Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (hudKind == 1) AppIcons.Brightness else if (hudValue <= 0f) AppIcons.VolumeOff else AppIcons.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White,
                )
                Spacer(Modifier.width(10.dp))
                LinearProgressIndicator(
                    progress = { hudValue },
                    modifier = Modifier.width(110.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
                Spacer(Modifier.width(10.dp))
                Text("${(hudValue * 100).roundToInt()}%", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }

        // Indicador de velocidad 2x mientras se mantiene pulsado.
        AnimatedVisibility(
            visible = holdBoost,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 96.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut(),
        ) {
            Row(
                Modifier
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(AppIcons.Speed, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("2x", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }

        AnimatedVisibility(
            visible = flashDir != 0,
            modifier = Modifier
                .align(if (lastDir < 0) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 40.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.6f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
        ) {
            Box(
                Modifier.size(72.dp).background(Color.Black.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (lastDir < 0) AppIcons.Replay10 else AppIcons.Forward10,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White,
                )
            }
        }

        if (failed) {
            Text(
                stringResource(R.string.video_cannot_play),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        AnimatedVisibility(
            visible = chrome,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
        ) {
            val shownPosition = dragFraction?.let { (it * duration).toLong() } ?: position

            val scheme = MaterialTheme.colorScheme
            val shift = rememberBorderShift(settings.playerBorder, settings.animations && isPlaying)
            val ambientGlass = LocalAmbientBackdrop.current
            val glassOn = ambientGlass != null && ambientGlass.enabled
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = if (glassOn) Color.Transparent else scheme.surfaceContainer,
                tonalElevation = if (glassOn) 0.dp else 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassSurface(
                        state = ambientGlass,
                        tint = scheme.surfaceContainer.copy(alpha = 0.50f),
                        fallback = scheme.surfaceContainer,
                        corner = 32.dp,
                        border = false,
                        extendBottom = true,
                    )
                    .panelBorder(settings.playerBorder, shift, scheme.primary, scheme.tertiary, 32.dp),
            ) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    SeekBar(
                        exo = exo,
                        duration = duration,
                        playing = isPlaying,
                        position = position,
                        dragFraction = dragFraction,
                        onDrag = { dragFraction = it },
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(formatDuration(shownPosition), style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatDuration(duration),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.align(Alignment.CenterStart)) {
                            TextButton(onClick = { speedMenu = true }) {
                                Icon(AppIcons.Speed, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(speedLabel(speed))
                            }
                            DropdownMenu(expanded = speedMenu, onDismissRequest = { speedMenu = false }) {
                                listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f).forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(speedLabel(s)) },
                                        onClick = {
                                            speed = s
                                            exo.setPlaybackSpeed(s)
                                            speedMenu = false
                                        },
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { seekBy(-10_000) }) {
                                Icon(AppIcons.Replay10, contentDescription = stringResource(R.string.cd_rewind_10))
                            }
                            PlayPauseButton(
                                isPlaying = isPlaying,
                                ended = ended,
                                animations = settings.animations,
                                onClick = { togglePlay() },
                            )
                            IconButton(onClick = { seekBy(10_000) }) {
                                Icon(AppIcons.Forward10, contentDescription = stringResource(R.string.cd_forward_10))
                            }
                        }

                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    loop = !loop
                                    exo.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                                },
                            ) {
                                Icon(
                                    AppIcons.Loop,
                                    contentDescription = stringResource(R.string.cd_loop),
                                    tint = if (loop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(
                                onClick = {
                                    muted = !muted
                                    exo.volume = if (muted) 0f else 1f
                                },
                            ) {
                                Icon(
                                    if (muted) AppIcons.VolumeOff else AppIcons.VolumeUp,
                                    contentDescription = stringResource(if (muted) R.string.cd_unmute else R.string.cd_mute),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Barra de progreso: mientras se reproduce se actualiza en cada fotograma de la pantalla
 * (no cada 200 ms), y solo esta pieza se redibuja, así que se ve fluida sin recargar el resto.
 */
@Composable
private fun SeekBar(
    exo: ExoPlayer,
    duration: Long,
    playing: Boolean,
    position: Long,
    dragFraction: Float?,
    onDrag: (Float?) -> Unit,
) {
    var progress by remember { mutableFloatStateOf(0f) }

    fun current(): Float =
        if (duration > 0L) (exo.currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f

    // En pausa (o tras saltar con los botones) se sincroniza con la posición conocida.
    LaunchedEffect(position, duration) {
        if (!playing) progress = current()
    }
    // Reproduciendo: una lectura por fotograma.
    LaunchedEffect(exo, playing, duration) {
        if (playing) {
            while (true) {
                withFrameNanos { }
                progress = current()
            }
        } else {
            progress = current()
        }
    }

    Slider(
        value = dragFraction ?: progress,
        onValueChange = { onDrag(it) },
        onValueChangeFinished = {
            dragFraction?.let { f -> exo.seekTo((f * duration).toLong()) }
            onDrag(null)
        },
        valueRange = 0f..1f,
    )
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, ended: Boolean, animations: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "playScale",
    )
    val state = when {
        ended -> 2
        isPlaying -> 1
        else -> 0
    }
    FilledIconButton(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        // El icono cambia con una pequeña animación de escala y desvanecido.
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                if (animations) {
                    (fadeIn(tween(160)) + scaleIn(initialScale = 0.6f)) togetherWith
                        (fadeOut(tween(100)) + scaleOut(targetScale = 0.6f))
                } else {
                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                }
            },
            label = "playIcon",
        ) { s ->
            Icon(
                when (s) {
                    2 -> AppIcons.Replay
                    1 -> AppIcons.Pause
                    else -> AppIcons.Play
                },
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

private fun speedLabel(s: Float): String =
    if (s % 1f == 0f) "${s.toInt()}x" else "${s}x"

// ------------------------------------------------------------------ Detalles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoSheet(m: MediaFile, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 16.dp)) {
            Text(
                stringResource(R.string.details),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            InfoRow(
                icon = { Icon(AppIcons.Calendar, contentDescription = null) },
                title = formatDateSkeleton(m.dateMillis, "EEEEdMMMMyyyy"),
                subtitle = formatDateSkeleton(m.dateMillis, "jm"),
            )
            InfoRow(
                icon = {
                    Icon(
                        if (m.isVideo) AppIcons.Video else AppIcons.Image,
                        contentDescription = null,
                    )
                },
                title = m.name,
                subtitle = "${formatSize(m.size)} · ${m.mime}",
            )
            if (m.width > 0 && m.height > 0) {
                val mp = m.width.toLong() * m.height / 1_000_000.0
                InfoRow(
                    icon = { Icon(AppIcons.Frame, contentDescription = null) },
                    title = "${m.width} × ${m.height}",
                    subtitle = "%.1f MP".format(mp),
                )
            }
            if (m.isVideo) {
                InfoRow(
                    icon = { Icon(AppIcons.Timer, contentDescription = null) },
                    title = formatDuration(m.durationMs),
                    subtitle = stringResource(R.string.info_duration),
                )
            }
            if (m.bucket.isNotBlank()) {
                InfoRow(
                    icon = { Icon(AppIcons.Collections, contentDescription = null) },
                    title = m.bucket,
                    subtitle = stringResource(R.string.info_album),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: @Composable () -> Unit, title: String, subtitle: String) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
