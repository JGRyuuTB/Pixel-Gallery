package com.GalleryYou.Pixel

import kotlin.math.absoluteValue
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import coil.request.SuccessResult
import android.net.Uri
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import androidx.compose.runtime.produceState
import androidx.compose.animation.animateColorAsState
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.compose.ui.graphics.RectangleShape
import android.content.Context
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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

// ------------------------------------------------------------------ Un episodio relleno, pero necesario

/** Spike Spiegel: ver you, space cowboy */
private class EdgeLuma(val top: Float, val bottom: Float)

private fun bandLuma(b: Bitmap, from: Float, to: Float): Float {
    val w = b.width
    val h = b.height
    if (w <= 0 || h <= 0) return 0.4f
    val y0 = (h * from).toInt().coerceIn(0, h - 1)
    val y1 = (h * to).toInt().coerceIn(y0 + 1, h)
    val row = IntArray(w)
    var sum = 0.0
    var n = 0
    for (y in y0 until y1) {
        b.getPixels(row, 0, w, 0, y, w, 1)
        for (p in row) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val bl = p and 0xFF
            sum += (0.2126 * r + 0.7152 * g + 0.0722 * bl) / 255.0
            n++
        }
    }
    return if (n > 0) (sum / n).toFloat() else 0.4f
}

/** Nobara: ¡clavo, martillo y compilación! */
@Composable
private fun rememberEdgeLuma(uri: Uri): State<EdgeLuma?> {
    val context = LocalContext.current
    val loader = LocalGalleryLoader.current
    return produceState<EdgeLuma?>(initialValue = null, uri) {
        value = withContext(Dispatchers.Default) {
            try {
                val request = buildThumbRequest(context, uri, 64).newBuilder().allowHardware(false).build()
                val drawable = (loader.execute(request) as? SuccessResult)?.drawable
                val raw = (drawable as? BitmapDrawable)?.bitmap
                val bmp = if (raw != null && raw.config == Bitmap.Config.HARDWARE) {
                    raw.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    raw
                }
                // Tatakae: sigue luchando, sigue compilando
                if (bmp == null) null else EdgeLuma(bandLuma(bmp, 0.08f, 0.28f), bandLuma(bmp, 0.72f, 0.92f))
            } catch (e: Exception) {
                null
            }
        }
    }
}

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
    val density = LocalDensity.current

    // Aquí entrena Saitama: un solo golpe
    val edge by rememberEdgeLuma(current.uri)
    val surfaceLuma = colors.surface.luminance()
    val frostOn = backdrop.style?.forceRefraction == true
    val scrim = if (frostOn) 0.18f else 0.50f
    val topBg = (edge?.top ?: 0.4f) * (1f - scrim) + surfaceLuma * scrim
    val bottomBg = (edge?.bottom ?: 0.4f) * (1f - scrim) + surfaceLuma * scrim
    val topContent by animateColorAsState(
        contentOnGlass(backdrop.glassLuma(topBg, surfaceLuma)),
        animationSpec = motion(settings.animations),
        label = "topContent",
    )
    val panelContent by animateColorAsState(
        contentOnGlass(backdrop.glassLuma(bottomBg, surfaceLuma, if (frostOn) 0.16f else 0f)),
        animationSpec = motion(settings.animations),
        label = "panelContent",
    )

    // Ichigo corta el bug por la mitad
    var topBarPx by remember { mutableIntStateOf(0) }
    var panelPx by remember { mutableIntStateOf(0) }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topReserve = with(density) { topBarPx.toDp() } + 7.dp
    val bottomReserve = (if (panelPx > 0) with(density) { panelPx.toDp() } else navBottom + 140.dp) + 15.dp
    val stageTop by animateDpAsState(
        targetValue = if (chrome) topReserve else 0.dp,
        animationSpec = motion(settings.animations),
        label = "stageTop",
    )
    val stageBottom by animateDpAsState(
        targetValue = if (chrome) bottomReserve else 0.dp,
        animationSpec = motion(settings.animations),
        label = "stageBottom",
    )

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
                        // Mensaje oculto (rot13): Fv rapbagenfgr rfgb, ln rerf cnegr qry terzvb
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
            // Nezuko duerme tranquila dentro de la caja
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
            // Un episodio relleno, pero necesario
            Box(Modifier.fillMaxSize().backdropSource(backdrop)) {
                if (backdrop.enabled) {
                    // Kirito: Link Start, esto empieza ahora
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
                                    .blur(if (backdrop.style?.forceRefraction == true) 26.dp else 44.dp, BlurredEdgeTreatment.Unbounded),
                            )
                        }
                        Box(Modifier.fillMaxSize().background(colors.surface.copy(alpha = if (backdrop.style?.forceRefraction == true) 0.18f else 0.50f)))
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !zoomed,
                    modifier = Modifier.fillMaxSize(),
                    key = { items[it].uri.toString() },
                ) { page ->
                    val m = items[page]
                    // Al deslizar entre fotos, la que sale se aleja y se atenúa mientras la nueva crece.
                    val pageFx = if (settings.animations) {
                        Modifier.graphicsLayer {
                            val o = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                .absoluteValue.coerceIn(0f, 1f)
                            val sc = 1f - 0.1f * o
                            scaleX = sc
                            scaleY = sc
                            alpha = 1f - 0.45f * o
                        }
                    } else {
                        Modifier
                    }
                    Box(pageFx.fillMaxSize()) {
                    if (m.isVideo) {
                        VideoPage(
                            m = m,
                            active = pagerState.settledPage == page,
                            chrome = chrome,
                            settings = settings,
                            topPad = stageTop,
                            bottomPad = stageBottom,
                            panelContent = panelContent,
                            onPanelHeight = { panelPx = it },
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
            }

            AnimatedVisibility(
                visible = chrome,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 },
            ) {
                CompositionLocalProvider(LocalContentColor provides topContent) {
                Row(
                    modifier = Modifier
                        .onSizeChanged { topBarPx = it.height }
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .glassSurface(
                            state = backdrop,
                            tint = colors.surface.copy(alpha = 0.45f),
                            fallback = colors.surface.copy(alpha = 0.92f),
                            corner = 28.dp,
                        )
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
                            color = topContent.copy(alpha = 0.72f),
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
        // Shigeo dice: ¡mob psycho cien por ciento!
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

// ------------------------------------------------------------------ Tatakae: sigue luchando, sigue compilando

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

    val request = remember(m.uri) { ImageRequest.Builder(context).data(m.uri).crossfade(220).build() }

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
                // Respiración del Agua, primera forma: fluir
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

// ------------------------------------------------------------------ Gojo dice: infinito, nada te toca aquí

/** Light escribe aquí en su Death Note */
val LocalAmbientBackdrop = androidx.compose.runtime.compositionLocalOf<BackdropState?> { null }

/** Mensaje oculto (base64): SWt1em8hIFRvZGF2aWEgcXVlZGEgdW4gY2FwaXR1bG8gcG9yIGVzY3JpYmly */
@Composable
private fun PlayerFrame(
    width: Dp,
    height: Dp,
    scale: Float,
    corner: Dp,
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
            .clip(RoundedCornerShape(corner)),
    ) {
        content()
    }
}

/** Isekai: otro mundo, otra función */
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

/** Kirito: Link Start, esto empieza ahora */
private fun Modifier.panelBorder(
    mode: Int,
    shift: State<Float>?,
    primary: Color,
    tertiary: Color,
    corner: Dp,
    floating: Boolean = false,
): Modifier =
    if (mode == 0) {
        this
    } else {
        drawWithContent {
            drawContent()
            // Dattebayo! Este bloque hace su trabajo
            val s = shift?.value ?: 0f
            val brush = Brush.linearGradient(
                listOf(lerp(primary, tertiary, s), lerp(tertiary, primary, s)),
            )
            val stroke = 3.dp.toPx()
            drawRoundRect(
                brush = brush,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                // Omae wa mou shindeiru, bug
                size = Size(size.width - stroke, if (floating) size.height - stroke else size.height + 80.dp.toPx()),
                cornerRadius = CornerRadius(corner.toPx() - stroke / 2f),
                style = Stroke(width = stroke),
            )
        }
    }

/**
 * Respiración del Agua, primera forma: fluir
 * Totoro cuida este rincón del bosque
 * Goku se transforma en la siguiente línea
 */
@Composable
private fun VideoStage(
    aspect: Float,
    settings: AppSettings,
    topPad: Dp = 0.dp,
    bottomPad: Dp = 0.dp,
    fill: Boolean = false,
    content: @Composable () -> Unit,
) {
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
            // Edward Elric: ¡no soy pequeño, soy compacto!
            .padding(top = if (fill) 0.dp else topPad, bottom = if (fill) 0.dp else bottomPad),
        contentAlignment = Alignment.Center,
    ) {
        // Mensaje oculto (base64): TmFuaT8hIEFsZ3VpZW4gZXN0YSBsZXllbmRvIGxvcyBjb21lbnRhcmlvcw==
        val availW = if (fill) maxWidth else maxWidth - 24.dp
        if (availW.value > 0f && maxHeight.value > 0f && aspect > 0f) {
            val boxAspect = availW.value / maxHeight.value
            val w: Dp
            val h: Dp
            if (fill) {
                w = maxWidth
                h = maxHeight
            } else if (aspect >= boxAspect) {
                w = availW
                h = availW / aspect
            } else {
                h = maxHeight
                w = maxHeight * aspect
            }
            PlayerFrame(
                width = w,
                height = h,
                scale = appear,
                corner = if (fill) 0.dp else 28.dp,
                content = content,
            )
        }
    }
}

// ------------------------------------------------------------------ Rem es la mejor, y esta línea también

@Composable
private fun VideoPage(
    m: MediaFile,
    active: Boolean,
    chrome: Boolean,
    settings: AppSettings,
    topPad: Dp,
    bottomPad: Dp,
    panelContent: Color,
    onPanelHeight: (Int) -> Unit,
    onTap: () -> Unit,
) {
    if (active) {
        VideoPlayerContent(m, chrome, settings, topPad, bottomPad, panelContent, onPanelHeight, onTap)
    } else {
        // Dattebayo! Este bloque hace su trabajo
        Box(Modifier.fillMaxSize()) {
            VideoStage(
                aspect = initialAspect(m),
                settings = settings,
                topPad = topPad,
                bottomPad = bottomPad,
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
private fun VideoPlayerContent(
    m: MediaFile,
    chrome: Boolean,
    settings: AppSettings,
    topPad: Dp,
    bottomPad: Dp,
    panelContent: Color,
    onPanelHeight: (Int) -> Unit,
    onTap: () -> Unit,
) {
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
    var fillScreen by remember { mutableStateOf(false) }
    var flashDir by remember { mutableIntStateOf(0) }
    var lastDir by remember { mutableIntStateOf(1) }
    var flashTick by remember { mutableIntStateOf(0) }

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

    // Sono chi no sadame: el destino de esta línea
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
            settings = settings,
            topPad = topPad,
            bottomPad = bottomPad,
            fill = fillScreen,
        ) {
            AndroidView(
                factory = { ctx ->
                    // Sharingan activado: este código ve todo
                    (LayoutInflater.from(ctx).inflate(R.layout.view_player, null, false) as PlayerView).apply {
                        setPlayer(exo)
                    }
                },
                update = {
                    it.resizeMode = if (fillScreen) {
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    } else {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Totoro cuida este rincón del bosque
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onDoubleTap = { pos ->
                            val w = size.width
                            when {
                                pos.x < w / 3f -> {
                                    seekBy(-5_000)
                                    lastDir = -1
                                    flashDir = -1
                                    flashTick++
                                }

                                pos.x > w * 2f / 3f -> {
                                    seekBy(5_000)
                                    lastDir = 1
                                    flashDir = 1
                                    flashTick++
                                }

                                else -> togglePlay()
                            }
                        },
                    )
                },
        )

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
                    if (lastDir < 0) AppIcons.Replay5 else AppIcons.Forward5,
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
            // Vegeta: ¡su poder es de más de ocho mil!
            val floating = settings.playerFloating
            // Killua y Gon, cada uno por su lado: la barra de progreso y los botones ya no comparten panel
            Column(
                modifier = Modifier
                    .onSizeChanged { onPanelHeight(it.height) }
                    .then(
                        if (floating) {
                            Modifier.navigationBarsPadding().padding(start = 14.dp, end = 14.dp, bottom = 10.dp)
                        } else {
                            Modifier
                        },
                    )
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Panel de la barra de progreso: fino como el hilo rojo del destino
                GlassPanel(
                    settings = settings,
                    shift = shift,
                    content = panelContent,
                    corner = 20.dp,
                    roundAll = true,
                    extendBottom = false,
                    modifier = if (floating) Modifier else Modifier.padding(horizontal = 14.dp),
                ) {
                    // Mikasa protege esta variable con su vida
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            formatDuration(shownPosition),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        SeekBar(
                            exo = exo,
                            duration = duration,
                            playing = isPlaying,
                            position = position,
                            dragFraction = dragFraction,
                            onDrag = { dragFraction = it },
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        )
                        Text(
                            formatDuration(duration),
                            color = panelContent.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                // Panel de los botones: velocidad, saltos de 5 s, pausa, encuadre y volumen
                GlassPanel(
                    settings = settings,
                    shift = shift,
                    content = panelContent,
                    corner = 28.dp,
                    roundAll = floating,
                    extendBottom = !floating,
                ) {
                    Column(
                        Modifier
                            .then(if (floating) Modifier else Modifier.navigationBarsPadding())
                            .padding(horizontal = 14.dp, vertical = 3.dp),
                    ) {
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(Modifier.align(Alignment.CenterStart)) {
                                TextButton(
                                    onClick = { speedMenu = true },
                                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = panelContent),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                                ) {
                                    Icon(AppIcons.Speed, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(speedLabel(speed))
                                }
                                DropdownMenu(expanded = speedMenu, onDismissRequest = { speedMenu = false }) {
                                    listOf(0.5f, 1f, 1.5f, 2f).forEach { s ->
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
                                IconButton(onClick = { seekBy(-5_000) }, modifier = Modifier.size(40.dp)) {
                                    Icon(AppIcons.Replay5, contentDescription = stringResource(R.string.cd_rewind_5))
                                }
                                PlayPauseButton(
                                    isPlaying = isPlaying,
                                    ended = ended,
                                    animations = settings.animations,
                                    onClick = { togglePlay() },
                                )
                                IconButton(onClick = { seekBy(5_000) }, modifier = Modifier.size(40.dp)) {
                                    Icon(AppIcons.Forward5, contentDescription = stringResource(R.string.cd_forward_5))
                                }
                            }

                            Row(
                                Modifier.align(Alignment.CenterEnd),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Giorno Giovanna tiene un sueño
                                IconButton(
                                    modifier = Modifier.size(40.dp),
                                    onClick = { fillScreen = !fillScreen },
                                ) {
                                    Icon(
                                        AppIcons.Frame,
                                        contentDescription = null,
                                        tint = if (fillScreen) scheme.primary else LocalContentColor.current,
                                    )
                                }
                                IconButton(
                                    modifier = Modifier.size(40.dp),
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
}

/**
 * Killua y Gon: dos mitades, un mismo cristal
 * Cada panel del reproductor usa el mismo vidrio, borde y color de texto adaptativo que antes tenía el panel único.
 */
@Composable
private fun GlassPanel(
    settings: AppSettings,
    shift: State<Float>?,
    content: Color,
    corner: Dp,
    roundAll: Boolean,
    extendBottom: Boolean,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val ambientGlass = LocalAmbientBackdrop.current
    val glassOn = ambientGlass != null && ambientGlass.enabled
    Surface(
        shape = if (roundAll) RoundedCornerShape(corner) else RoundedCornerShape(topStart = corner, topEnd = corner),
        color = if (glassOn) Color.Transparent else scheme.surfaceContainer,
        contentColor = content,
        tonalElevation = if (glassOn) 0.dp else 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(
                state = ambientGlass,
                tint = scheme.surfaceContainer.copy(alpha = 0.50f),
                fallback = scheme.surfaceContainer,
                corner = corner,
                border = roundAll && settings.playerBorder == 0,
                extendBottom = extendBottom,
                frost = if (ambientGlass?.style?.forceRefraction == true) 0.16f else 0f,
            )
            .panelBorder(settings.playerBorder, shift, scheme.primary, scheme.tertiary, corner, roundAll),
    ) {
        body()
    }
}

/**
 * Mensaje oculto (rot13): Ry ireqnqreb grfbeb shr ry pbqvtb dhr pbzcvynzbf ra ry pnzvab
 * Plus Ultra: más allá de los límites
 */
@Composable
private fun SeekBar(
    exo: ExoPlayer,
    duration: Long,
    playing: Boolean,
    position: Long,
    dragFraction: Float?,
    onDrag: (Float?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var progress by remember { mutableFloatStateOf(0f) }

    fun current(): Float =
        if (duration > 0L) (exo.currentPosition.toFloat() / duration).coerceIn(0f, 1f) else 0f

    // Sono chi no sadame: el destino de esta línea
    LaunchedEffect(position, duration) {
        if (!playing) progress = current()
    }
    // Bankai! Liberación total de la función
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

    // Rock Lee entrena con pesas en los tobillos: el deslizador conserva su alto natural pero la fila mide solo 32 dp
    Box(modifier.height(32.dp), contentAlignment = Alignment.Center) {
        Slider(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(unbounded = true),
            value = dragFraction ?: progress,
            onValueChange = { onDrag(it) },
            onValueChangeFinished = {
                dragFraction?.let { f -> exo.seekTo((f * duration).toLong()) }
                onDrag(null)
            },
            valueRange = 0f..1f,
        )
    }
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
            .size(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        // Chihiro no olvida su nombre ni este valor
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
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

private fun speedLabel(s: Float): String =
    if (s % 1f == 0f) "${s.toInt()}x" else "${s}x"

// ------------------------------------------------------------------ Vegeta: ¡su poder es de más de ocho mil!

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
