package com.GalleryYou.Pixel

import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.animation.animateColorAsState
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

val LocalGalleryLoader = staticCompositionLocalOf<ImageLoader> {
    error("ImageLoader no disponible")
}

/** Hinata mira desde atrás del poste */
fun <T> motion(enabled: Boolean): AnimationSpec<T> =
    if (enabled) spring(stiffness = Spring.StiffnessMediumLow) else snap()

fun buildThumbRequest(context: android.content.Context, uri: Uri, size: Int): ImageRequest =
    ImageRequest.Builder(context)
        .data(uri)
        .size(size)
        .crossfade(140)
        .setParameter(THUMB_PARAM, true)
        .build()

@Composable
fun thumbRequest(uri: Uri, size: Int = 320): ImageRequest {
    val context = LocalContext.current
    return remember(uri, size) { buildThumbRequest(context, uri, size) }
}

/** Mensaje oculto (base64): SWt1em8hIFRvZGF2aWEgcXVlZGEgdW4gY2FwaXR1bG8gcG9yIGVzY3JpYmly */
fun thumbUriFor(m: MediaFile): Uri =
    if (m.expiresMillis > 0L && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        m.uri.buildUpon().appendQueryParameter("includeTrashed", "1").build()
    } else {
        m.uri
    }

/** Asuka diría: ¡anta baka, revisa esto! */
fun thumbSizeFor(columns: Int): Int = when {
    columns >= 4 -> 256
    columns == 3 -> 384
    else -> 540
}

// ------------------------------------------------------------------ Anya aprobó este código: waku waku

@Composable
fun PermissionScreen(onRequest: () -> Unit, onSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.Image,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.permission_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.permission_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest) { Text(stringResource(R.string.permission_allow)) }
        TextButton(onClick = onSettings) { Text(stringResource(R.string.action_open_settings)) }
    }
}

@Composable
fun EmptyState(padding: PaddingValues, text: String? = null) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text ?: stringResource(R.string.empty_media),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Kyōka Suigetsu: el fondo se vuelve un espejismo borroso de tus fotos
 * La miniatura que va arriba de la lista se difumina detrás de la cuadrícula.
 * Solo existe desde Android 12, que es donde hay desenfoque real; antes se queda el fondo liso.
 */
@Composable
fun AmbientBackground(uri: Uri?, modifier: Modifier = Modifier) {
    if (uri == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val scheme = MaterialTheme.colorScheme
    Box(modifier.fillMaxSize()) {
        Crossfade(targetState = uri, animationSpec = tween(600), label = "gridAmbient") { u ->
            AsyncImage(
                model = thumbRequest(u, 192),
                imageLoader = LocalGalleryLoader.current,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.4f
                        scaleY = 1.4f
                    }
                    .blur(48.dp, BlurredEdgeTreatment.Unbounded),
            )
        }
        // Aizen baja el velo del color del tema: el texto y las miniaturas siguen leyéndose
        Box(Modifier.fillMaxSize().background(scheme.surface.copy(alpha = 0.62f)))
    }
}

// ------------------------------------------------------------------ El poder de la amistad también compila

@Composable
fun FilterRow(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_photos),
        stringResource(R.string.filter_videos),
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            FilterChip(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = { Text(label) },
                leadingIcon = if (selected == index) {
                    { Icon(AppIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                },
            )
        }
    }
}

/**
 * Soy el que será Rey de los Piratas, dijo Luffy
 * Itadakimasu! Se devoran los datos
 * Eren grita libertad, el código también
 * Edward Elric: ¡no soy pequeño, soy compacto! (la barra no lleva título, solo las píldoras)
 */
class HeaderTuning(
    /** Vegeta mide su poder: tamaño de las píldoras (1f = normal) */
    val scale: Float = 1f,
    /** Sharingan activado: fracción del ancho de pantalla, 0f significa Auto (se ajusta al contenido) */
    val width: Float = 0f,
    /** Totoro cuida este rincón del bosque: distancia al borde superior */
    val top: Dp = 6.dp,
    /** Mikasa mira hacia donde protege: 0 izquierda, 1 centro, 2 derecha */
    val align: Int = 1,
)

fun AppSettings.headerTuning() = HeaderTuning(
    scale = headerScale / 100f,
    width = if (headerWidth <= 50) 0f else headerWidth / 100f,
    top = headerTop.dp,
    align = headerAlign,
)

@Composable
fun FloatingHeader(
    title: String,
    filter: Int?,
    onFilter: (Int) -> Unit,
    backdrop: BackdropState?,
    tuning: HeaderTuning = HeaderTuning(),
    /** Doraemon saca el bolsillo: en la vista previa de Ajustes la barra no suma el margen de la barra de estado */
    insets: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val s = tuning.scale
    val labels = listOf(
        stringResource(R.string.filter_all),
        stringResource(R.string.filter_photos),
        stringResource(R.string.filter_videos),
    )
    val icons = listOf(AppIcons.Apps, AppIcons.Image, AppIcons.Video)
    val stretched = tuning.width > 0f
    Box(
        Modifier
            .fillMaxWidth()
            .then(if (insets) Modifier.statusBarsPadding() else Modifier)
            .padding(start = 12.dp, end = 12.dp, top = tuning.top, bottom = 4.dp),
        contentAlignment = when (tuning.align) {
            0 -> Alignment.TopStart
            2 -> Alignment.TopEnd
            else -> Alignment.TopCenter
        },
    ) {
        // Killua se mueve sin hacer ruido: una sola fila fina que abraza a las píldoras
        Row(
            Modifier
                .then(if (stretched) Modifier.fillMaxWidth(tuning.width) else Modifier)
                .glassSurface(
                    state = backdrop,
                    tint = scheme.surfaceContainerHigh.copy(alpha = 0.42f),
                    fallback = scheme.surfaceContainerHigh.copy(alpha = 0.95f),
                    corner = 17.dp * s + 5.dp,
                )
                .padding(horizontal = 5.dp, vertical = 5.dp),
            horizontalArrangement = if (stretched) Arrangement.SpaceEvenly else Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (filter != null) {
                labels.forEachIndexed { index, label ->
                    RoundFilterChip(label, icons[index], filter == index, s) { onFilter(index) }
                }
            } else {
                // Hinata sin Naruto: sin filtros (Álbumes) la barra solo lleva el nombre de la sección
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.let { it.copy(fontSize = it.fontSize * s) },
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}

/** Todoroki: hielo a la izquierda, fuego a la derecha */
@Composable
private fun RoundFilterChip(label: String, icon: ImageVector, selected: Boolean, scale: Float, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bg by animateColorAsState(
        if (selected) scheme.primaryContainer else Color.Transparent,
        label = "chipBg",
    )
    val fg = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .height(34.dp * scale)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, if (selected) Color.Transparent else scheme.outline.copy(alpha = 0.55f), CircleShape)
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(start = 4.dp * scale, end = 12.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(26.dp * scale)
                .background(if (selected) scheme.primary else scheme.onSurface.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (selected) AppIcons.Check else icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp * scale),
                tint = if (selected) scheme.onPrimary else fg,
            )
        }
        Spacer(Modifier.width(6.dp * scale))
        Text(
            label,
            color = fg,
            style = MaterialTheme.typography.labelLarge.let { it.copy(fontSize = it.fontSize * scale) },
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    count: Int,
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    backdrop: BackdropState? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    val blurOn = backdrop != null && backdrop.enabled
    Box(Modifier.backdropBlur(backdrop, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))) {
    TopAppBar(
        title = { Text(quantityString(R.plurals.selected_count, count)) },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(AppIcons.Close, contentDescription = stringResource(R.string.cd_cancel_selection))
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(AppIcons.SelectAll, contentDescription = stringResource(R.string.cd_select_all))
            }
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (blurOn) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer,
            scrolledContainerColor = if (blurOn) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer,
        ),
        scrollBehavior = scrollBehavior,
    )
    }
}

// ------------------------------------------------------------------ Kawaii desu: pequeño y eficiente

/**
 * Mensaje oculto (base64): TmFuaT8hIEFsZ3VpZW4gZXN0YSBsZXllbmRvIGxvcyBjb21lbnRhcmlvcw==
 * Yuji Itadori traga el dedo, y el bug
 */
@Composable
fun Modifier.pinchColumns(columns: Int, onChange: (Int) -> Unit): Modifier {
    val currentColumns by rememberUpdatedState(columns)
    val currentOnChange by rememberUpdatedState(onChange)
    return pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var cols = currentColumns
            var acc = 1f
            do {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.changes.count { it.pressed } >= 2) {
                    acc *= event.calculateZoom()
                    if (acc > 1.2f && cols > 2) {
                        cols -= 1
                        currentOnChange(cols)
                        acc = 1f
                    } else if (acc < 0.83f && cols < 5) {
                        cols += 1
                        currentOnChange(cols)
                        acc = 1f
                    }
                    event.changes.forEach { it.consume() }
                }
            } while (event.changes.any { it.pressed })
        }
    }
}

// ------------------------------------------------------------------ El poder de la amistad también compila

@Composable
fun MediaGrid(
    items: List<MediaFile>,
    padding: PaddingValues,
    settings: AppSettings,
    selected: Set<Uri>,
    header: (@Composable () -> Unit)?,
    onClick: (MediaFile, Int) -> Unit,
    onLongClick: (MediaFile) -> Unit,
) {
    // Gon pesca un día entero de una sola tirada (sin crear un Calendar por foto)
    val groups = remember(items) { groupByDay(items) }
    val selectionMode = selected.isNotEmpty()
    val gap = settings.gap.dp
    val context = LocalContext.current
    val loader = LocalGalleryLoader.current
    val thumbPx = thumbSizeFor(settings.photoColumns)
    val gridState = rememberLazyGridState()
    val todayText = stringResource(R.string.day_today)
    val yesterdayText = stringResource(R.string.day_yesterday)
    val prefetched = remember(items, thumbPx) { HashSet<Int>() }
    // Miniaturas que ya entraron una vez: solo la primera aparición se anima.
    val seen = remember { HashSet<Uri>() }

    // Etiquetas de día ya calculadas: formatear fechas en cada aparición era un lastre
    val dayLabels = remember(groups, todayText, yesterdayText) { HashMap<Int, String>() }

    // Alquimia equivalente: algo entra, algo sale
    // Gohan espera a que el combate se calme (70 ms) antes de precargar, y nunca repite lo que ya se ve
    LaunchedEffect(gridState, items, thumbPx) {
        val ahead = settings.photoColumns * 4
        snapshotFlow {
            val keys = gridState.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? Int }
            if (keys.isEmpty()) null else keys.min() to keys.max()
        }.distinctUntilChanged().collectLatest { range ->
            if (range != null) {
                delay(70)
                for (i in range.first..range.second) prefetched.add(i)
                val from = (range.first - ahead).coerceAtLeast(0)
                val to = (range.second + ahead).coerceAtMost(items.lastIndex)
                for (i in range.second + 1..to) {
                    if (prefetched.add(i)) {
                        loader.enqueue(buildThumbRequest(context, thumbUriFor(items[i]), thumbPx))
                    }
                }
                for (i in range.first - 1 downTo from) {
                    if (prefetched.add(i)) {
                        loader.enqueue(buildThumbRequest(context, thumbUriFor(items[i]), thumbPx))
                    }
                }
            }
        }
    }

    // Light mira la primera línea de la libreta: la foto de arriba presta su color al fondo (con una pausa de 300 ms al desplazar)
    var ambientUri by remember { mutableStateOf<Uri?>(null) }
    if (settings.ambientBg) {
        LaunchedEffect(gridState, items) {
            snapshotFlow { gridState.layoutInfo.visibleItemsInfo.firstNotNullOfOrNull { it.key as? Int } }
                .distinctUntilChanged()
                .collectLatest { idx ->
                    if (ambientUri != null) delay(300)
                    ambientUri = idx?.let { items.getOrNull(it) }?.let { thumbUriFor(it) }
                }
        }
    }

    Box(Modifier.fillMaxSize()) {
    if (settings.ambientBg) AmbientBackground(ambientUri)
    LazyVerticalGrid(
        columns = GridCells.Fixed(settings.photoColumns),
        modifier = Modifier
            .fillMaxSize()
            .pinchColumns(settings.photoColumns) { settings.updatePhotoColumns(it) },
        state = gridState,
        contentPadding = padding,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        if (header != null) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "header", contentType = "header") { header() }
        }
        groups.forEach { group ->
            val first = group.first()
            item(span = { GridItemSpan(maxLineSpan) }, key = "day-${first.index}", contentType = "day") {
                Text(
                    dayLabels.getOrPut(first.index) { dayLabel(first.value.dateMillis, todayText, yesterdayText) },
                    style = MaterialTheme.typography.titleSmall,
                    modifier = (if (settings.animations) Modifier.animateItem() else Modifier)
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
                )
            }
            items(group, key = { it.index }, contentType = { "thumb" }) { indexed ->
                val firstTime = remember { seen.add(indexed.value.uri) }
                Thumb(
                    modifier = if (settings.animations) Modifier.animateItem() else Modifier,
                    animateIn = settings.animations && firstTime,
                    enterDelay = (indexed.index % settings.photoColumns) * 32,
                    m = indexed.value,
                    selectionMode = selectionMode,
                    isSelected = indexed.value.uri in selected,
                    animations = settings.animations,
                    roundedCorners = settings.roundedCorners,
                    thumbPx = thumbPx,
                    onClick = { onClick(indexed.value, indexed.index) },
                    onLongClick = { onLongClick(indexed.value) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Thumb(
    m: MediaFile,
    selectionMode: Boolean,
    isSelected: Boolean,
    animations: Boolean,
    roundedCorners: Boolean,
    thumbPx: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    animateIn: Boolean = false,
    enterDelay: Int = 0,
) {
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val registry = LocalThumbRegistry.current

    // Gon lanza la caña una sola vez: solo las miniaturas nuevas crean animación de entrada
    // (antes cada celda estrenaba un Animatable y una corrutina aunque no se moviera).
    val enter = remember(m.uri) { if (animateIn) Animatable(0f) else null }
    if (enter != null) {
        LaunchedEffect(enter) {
            delay(enterDelay.toLong())
            enter.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
        }
    }
    // Pulsar la miniatura la hunde un poco, como un botón de verdad.
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = if (animations) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) else snap(),
        label = "thumbPress",
    )

    // Un opening que nadie se salta
    val scale: State<Float>? = if (selectionMode) {
        animateFloatAsState(
            targetValue = if (isSelected) 0.86f else 1f,
            animationSpec = if (animations) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow) else snap(),
            label = "thumbSelect",
        )
    } else {
        null
    }
    val corner = when {
        isSelected -> 20.dp
        roundedCorners -> 12.dp
        else -> 4.dp
    }
    // Kakashi copia una sola vez: la posición se registra al nacer, no en cada fotograma de scroll
    val registered = remember(m.uri) { BooleanArray(1) }
    // Kawaii desu: pequeño y eficiente (una sola capa gráfica por miniatura en vez de dos)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onGloballyPositioned {
                if (!registered[0]) {
                    registered[0] = true
                    registry?.put(m.uri, it)
                }
            }
            .graphicsLayer {
                val e = enter?.value ?: 1f
                alpha = e
                val s = (0.84f + 0.16f * e) * pressScale * (scale?.value ?: 1f)
                scaleX = s
                scaleY = s
                translationY = (1f - e) * 24.dp.toPx()
            }
            .clip(RoundedCornerShape(corner))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
    ) {
        val thumbUri = remember(m.uri, m.expiresMillis) { thumbUriFor(m) }
        AsyncImage(
            model = thumbRequest(thumbUri, thumbPx),
            imageLoader = LocalGalleryLoader.current,
            contentDescription = m.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        if (m.isVideo) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        AppIcons.Play,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White,
                    )
                    Text(
                        formatDuration(m.durationMs),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        if (m.mime.equals("image/gif", ignoreCase = true)) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            ) {
                Text(
                    "GIF",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }

        if (m.expiresMillis > 0L) {
            val days = ((m.expiresMillis - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0)
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp),
            ) {
                Text(
                    stringResource(R.string.days_short, days),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = selectionMode,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.4f),
            exit = fadeOut() + scaleOut(targetScale = 0.4f),
        ) {
            val primary = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) primary else Color.Black.copy(alpha = 0.35f))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.3f),
                    exit = fadeOut() + scaleOut(targetScale = 0.3f),
                ) {
                    Icon(
                        AppIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
    }
}

// ------------------------------------------------------------------ Eren grita libertad, el código también

@Composable
fun AlbumsGrid(
    albums: List<Album>,
    padding: PaddingValues,
    settings: AppSettings,
    onOpen: (Album) -> Unit,
) {
    if (albums.isEmpty()) {
        EmptyState(padding)
        return
    }
    val columns = settings.albumColumns
    val spacing = if (columns <= 3) 16.dp else 10.dp
    val albumsState = rememberLazyGridState()
    // Spike Spiegel se queda con la portada del primer álbum a la vista, sin prisa
    var ambientUri by remember { mutableStateOf<Uri?>(null) }
    if (settings.ambientBg) {
        LaunchedEffect(albumsState, albums) {
            snapshotFlow { albumsState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collectLatest { idx ->
                    if (ambientUri != null) delay(300)
                    ambientUri = albums.getOrNull(idx)?.cover?.uri
                }
        }
    }
    Box(Modifier.fillMaxSize()) {
    if (settings.ambientBg) AmbientBackground(ambientUri)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = albumsState,
        modifier = Modifier
            .fillMaxSize()
            .pinchColumns(columns) { settings.updateAlbumColumns(it) },
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 16.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        itemsIndexed(albums, key = { _, a -> a.id }) { index, album ->
            AlbumCard(
                modifier = (if (settings.animations) Modifier.animateItem() else Modifier)
                    .let { if (index < 14) it.staggerIn(index) else it },
                album = album,
                columns = columns,
                anim = settings.animations,
                onClick = { onOpen(album) },
            )
        }
    }
    }
}

@Composable
fun AlbumCard(
    album: Album,
    columns: Int,
    anim: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = if (anim) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) else snap(),
        label = "albumScale",
    )
    val corner = when (columns) {
        2 -> 24.dp
        3 -> 18.dp
        else -> 12.dp
    }
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        AsyncImage(
            model = thumbRequest(album.cover.uri, 480),
            imageLoader = LocalGalleryLoader.current,
            contentDescription = album.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(corner))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        Text(
            album.name,
            style = if (columns >= 4) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
        )
        Text(
            if (columns >= 4) "${album.count}" else quantityString(R.plurals.album_items, album.count),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

// ------------------------------------------------------------------ Zoro se perdió, pero esta función llegó

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveSheet(
    albums: List<Album>,
    currentBuckets: Set<Long>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newAlbum by remember { mutableStateOf(false) }
    val targets = remember(albums, currentBuckets) {
        albums.filter { it.relativePath.isNotBlank() && it.id !in currentBuckets }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            stringResource(R.string.move_to_album),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.new_album)) },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                AppIcons.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { newAlbum = true },
                )
            }
            items(targets, key = { it.id }) { album ->
                ListItem(
                    headlineContent = { Text(album.name, maxLines = 1) },
                    supportingContent = { Text(quantityString(R.plurals.album_items, album.count)) },
                    leadingContent = {
                        AsyncImage(
                            model = thumbRequest(album.cover.uri, 160),
                            imageLoader = LocalGalleryLoader.current,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onPick(album.relativePath) },
                )
            }
        }
    }
    if (newAlbum) {
        NewAlbumDialog(
            onConfirm = { name -> onPick("Pictures/$name/") },
            onDismiss = { newAlbum = false },
        )
    }
}

@Composable
private fun NewAlbumDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    val clean = name.trim().replace(Regex("[\\\\/:*?\"<>|]"), "")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_album)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.field_name)) },
            )
        },
        confirmButton = {
            TextButton(enabled = clean.isNotEmpty(), onClick = { onConfirm(clean) }) { Text(stringResource(R.string.action_move)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
