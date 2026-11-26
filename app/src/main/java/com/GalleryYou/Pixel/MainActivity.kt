package com.GalleryYou.Pixel

import com.GalleryYou.Pixel.ui.home.MoveSheet
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberFloatingToolbarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.GalleryYou.Pixel.data.AppSettings
import com.GalleryYou.Pixel.data.EXT_PICK
import com.GalleryYou.Pixel.data.EXT_VIEW
import com.GalleryYou.Pixel.data.LocalGalleryLoader
import com.GalleryYou.Pixel.data.MediaFile
import com.GalleryYou.Pixel.data.MediaPrefetch
import com.GalleryYou.Pixel.data.MediaRepo
import com.GalleryYou.Pixel.data.MediaThumbFetcher
import com.GalleryYou.Pixel.data.NavTabLabels
import com.GalleryYou.Pixel.data.applyAppIcon
import com.GalleryYou.Pixel.data.externalMedia
import com.GalleryYou.Pixel.data.findActivity
import com.GalleryYou.Pixel.data.finishPick
import com.GalleryYou.Pixel.data.indexOfExternal
import com.GalleryYou.Pixel.data.mimeMatches
import com.GalleryYou.Pixel.data.parseExternal
import com.GalleryYou.Pixel.ui.components.ExpressivePullToRefresh
import com.GalleryYou.Pixel.ui.components.PermissionScreen
import com.GalleryYou.Pixel.ui.components.PredictiveBackStack
import com.GalleryYou.Pixel.ui.components.navBarScrub
import com.GalleryYou.Pixel.ui.components.navBarScrubItem
import com.GalleryYou.Pixel.ui.components.rememberNavBarScrubState
import com.GalleryYou.Pixel.ui.home.AlbumDetailScreen
import com.GalleryYou.Pixel.ui.home.AlbumsGrid
import com.GalleryYou.Pixel.ui.home.HOME_FLOATING_RAIL_RESERVE
import com.GalleryYou.Pixel.ui.home.HOME_STANDARD_RAIL_RESERVE
import com.GalleryYou.Pixel.ui.home.HomeFloatingRail
import com.GalleryYou.Pixel.ui.home.HomeNavTab
import com.GalleryYou.Pixel.ui.home.HomeStandardRail
import com.GalleryYou.Pixel.ui.home.MediaGrid
import com.GalleryYou.Pixel.ui.home.MediaSelectionDeck
import com.GalleryYou.Pixel.ui.home.NavTabIcon
import com.GalleryYou.Pixel.ui.home.PhotosHomeHeader
import com.GalleryYou.Pixel.ui.home.TrashScreen
import com.GalleryYou.Pixel.ui.onboarding.OnboardingScreen
import com.GalleryYou.Pixel.ui.settings.SettingsScreen
import com.GalleryYou.Pixel.ui.theme.PaletteStyle
import com.GalleryYou.Pixel.ui.theme.PixelGalleryTheme
import com.GalleryYou.Pixel.ui.theme.ThemeMode
import com.GalleryYou.Pixel.ui.theme.currentWindowLayout
import com.GalleryYou.Pixel.ui.theme.isThemeDark
import com.GalleryYou.Pixel.ui.theme.safeHorizontalInsets
import com.GalleryYou.Pixel.util.LocalHapticsLevel
import com.GalleryYou.Pixel.util.rememberGalleryHaptics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val EXPRESSIVE_NAV_BAR_RESERVE = 88.dp
private val NON_EXPRESSIVE_NAV_BAR_RESERVE = 80.dp

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withAppLocale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.also {
                it.layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }

        val settings = AppSettings(applicationContext)
        applyAppIcon(applicationContext, settings.appIcon)
        MediaPrefetch.start(applicationContext)

        setContent {
            val darkTheme = isThemeDark(ThemeMode.fromInt(settings.themeMode))
            PixelGalleryTheme(
                darkTheme = darkTheme,
                colorPalette = settings.colorPalette,
                amoledDark = settings.amoledDark,
                uiScale = settings.uiScale,
                paletteStyle = settings.paletteStyle,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    StartupGate(settings) {
                        GaleriaApp(settings)
                    }
                }
            }
        }
    }
}

@Composable
fun GaleriaApp(settings: AppSettings) {
    val context = LocalContext.current
    val updater = remember { Updater(context.applicationContext) }
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(MediaThumbFetcher.Factory(context))
                add(VideoFrameDecoder.Factory())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(false)
            .allowRgb565(true)
            .respectCacheHeaders(false)
            .memoryCache {
                MemoryCache.Builder(context).maxSizePercent(0.30).build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("thumbs"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    var granted by remember {
        mutableStateOf(
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            },
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> granted = result.values.all { it } }

    LaunchedEffect(settings.onboardingDone, granted) {
        if (settings.onboardingDone && granted && settings.autoUpdate) {
            val now = System.currentTimeMillis()
            if (now - settings.lastUpdateCheck > 12L * 60L * 60L * 1000L) {
                settings.markUpdateChecked()
                updater.check(manual = false)
            }
        }
    }

    CompositionLocalProvider(
        LocalGalleryLoader provides imageLoader,
        LocalAnimations provides settings.animations,
        LocalHapticsLevel provides settings.hapticsLevel,
    ) {
        val openAppSettings = {
            context.startActivity(
                Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                ),
            )
        }
        when {
            !settings.onboardingDone -> OnboardingScreen(
                settings = settings,
                granted = granted,
                onRequestPermission = { launcher.launch(permissions) },
                onOpenAppSettings = openAppSettings,
                onFinish = { settings.completeOnboarding() },
            )
            !granted -> PermissionScreen(
                onRequest = { launcher.launch(permissions) },
                onSettings = openAppSettings,
            )
            else -> GalleryRoot(settings, updater)
        }
        UpdateDialog(updater)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GalleryRoot(settings: AppSettings, updater: Updater) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberGalleryHaptics()

    var media by remember { mutableStateOf<List<MediaFile>?>(null) }
    var trash by remember { mutableStateOf<List<MediaFile>>(emptyList()) }
    var reload by remember { mutableIntStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(reload) {
        val loaded = (if (reload == 0) MediaPrefetch.take() else null) ?: withContext(Dispatchers.IO) {
            MediaRepo.load(context) to MediaRepo.loadTrash(context)
        }
        if (loaded.first != media) media = loaded.first
        if (loaded.second != trash) trash = loaded.second
        if (refreshing) {
            delay(350)
            refreshing = false
        }
    }

    val owner = remember { context.findActivity() as? LifecycleOwner }
    DisposableEffect(owner) {
        var first = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (first) first = false else reload++
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var filterMode by rememberSaveable { mutableIntStateOf(0) }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var openAlbumId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selected by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var trashSelectionActive by remember { mutableStateOf(false) }
    var moveItems by remember { mutableStateOf<List<MediaFile>?>(null) }
    var viewer by remember { mutableStateOf<ViewerRequest?>(null) }
    var editing by remember { mutableStateOf<MediaFile?>(null) }
    var lastViewer by remember { mutableStateOf<ViewerRequest?>(null) }

    val activity = remember { context.findActivity() }
    val external = remember { parseExternal(activity?.intent) }
    val pickMode = external?.kind == EXT_PICK
    val pickMultiple = external?.multiple == true
    var picked by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var externalHandled by remember { mutableStateOf(false) }
    var externalViewing by remember { mutableStateOf(false) }

    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val intentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) pending?.invoke()
        pending = null
    }

    val snackbar = remember { SnackbarHostState() }
    fun announce(message: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    fun launchRequest(sender: IntentSender, onGranted: () -> Unit) {
        pending = onGranted
        intentLauncher.launch(IntentSenderRequest.Builder(sender).build())
    }

    fun shareItems(items: List<MediaFile>) {
        if (items.isEmpty()) return
        val intent = if (items.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = items[0].mime
                putExtra(Intent.EXTRA_STREAM, items[0].uri)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(items.map { it.uri }))
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, null))
    }

    fun trashItems(items: List<MediaFile>, onDone: () -> Unit = {}) {
        if (items.isEmpty()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Toast.makeText(context, context.getString(R.string.trash_needs_android11), Toast.LENGTH_SHORT).show()
            return
        }
        val uris = items.map { it.uri }
        val sender = runCatching {
            MediaStore.createTrashRequest(context.contentResolver, uris, true).intentSender
        }.getOrNull() ?: return
        launchRequest(sender) {
            announce(context.resources.getQuantityString(R.plurals.toast_trashed, items.size, items.size))
            selected = emptySet()
            onDone()
            reload++
        }
    }

    fun restoreItems(items: List<MediaFile>) {
        if (items.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createTrashRequest(context.contentResolver, uris, false).intentSender) {
            announce(context.getString(R.string.toast_restored))
            reload++
        }
    }

    fun deleteForever(items: List<MediaFile>) {
        if (items.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender) {
            announce(context.getString(R.string.toast_deleted_forever))
            reload++
        }
    }

    fun moveItemsTo(items: List<MediaFile>, relativePath: String) {
        if (items.isEmpty()) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Toast.makeText(context, context.getString(R.string.move_needs_android11), Toast.LENGTH_SHORT).show()
            return
        }
        val uris = items.map { it.uri }
        launchRequest(MediaStore.createWriteRequest(context.contentResolver, uris).intentSender) {
            scope.launch {
                val moved = withContext(Dispatchers.IO) { MediaRepo.move(context, items, relativePath) }
                announce(
                    if (moved == items.size) {
                        context.getString(R.string.toast_moved)
                    } else {
                        context.getString(R.string.toast_moved_partial, moved, items.size)
                    },
                )
                selected = emptySet()
                reload++
            }
        }
    }

    val thumbRegistry = remember { ThumbRegistry() }
    val viewerProgress = remember { Animatable(0f) }
    ViewerMotion.progress = { viewerProgress.value }
    var viewerVisible by remember { mutableStateOf(false) }
    var viewerOrigin by remember { mutableStateOf<Rect?>(null) }
    var viewerCurrent by remember { mutableStateOf<Uri?>(null) }
    var dragDismissed by remember { mutableStateOf(false) }

    fun open(items: List<MediaFile>, index: Int) {
        val request = ViewerRequest(items, index)
        lastViewer = request
        val first = items.getOrNull(index)?.uri
        viewerCurrent = first
        viewerOrigin = thumbRegistry.boundsOf(first)
        viewer = request
    }

    val viewerOpen = viewer != null

    LaunchedEffect(media, externalHandled) {
        val request = external
        val list = media
        if (request?.kind == EXT_VIEW && !externalHandled && list != null) {
            val uri = request.uri ?: return@LaunchedEffect
            externalHandled = true
            externalViewing = true
            val index = indexOfExternal(list, uri)
            if (index >= 0) open(list, index)
            else open(listOf(withContext(Dispatchers.IO) { externalMedia(context, uri) }), 0)
        }
    }
    LaunchedEffect(viewerOpen, viewerVisible) {
        if (externalViewing && !viewerOpen && !viewerVisible) activity?.finish()
    }
    LaunchedEffect(viewerOpen) {
        if (viewerOpen) {
            viewerVisible = true
            if (settings.animations) {
                viewerProgress.snapTo(0f)
                viewerProgress.animateTo(1f, tween(Motion.ContainerOpen, easing = Motion.Container))
            } else {
                viewerProgress.snapTo(1f)
            }
        } else if (viewerVisible) {
            if (dragDismissed || !settings.animations) {
                viewerProgress.snapTo(0f)
            } else {
                viewerOrigin = thumbRegistry.boundsOf(viewerCurrent)
                viewerProgress.animateTo(0f, tween(Motion.ContainerClose, easing = Motion.ContainerExit))
            }
            viewerVisible = false
            dragDismissed = false
        }
    }

    fun toggle(m: MediaFile) {
        selected = if (m.uri in selected) selected - m.uri else selected + m.uri
    }

    val all = media.orEmpty()
    val pool = remember(all, external) {
        if (external?.kind == EXT_PICK) all.filter { mimeMatches(it, external.mimes) } else all
    }
    val filtered = remember(pool, filterMode, searchActive, searchQuery) {
        val byType = when (filterMode) {
            1 -> pool.filter { !it.isVideo }
            2 -> pool.filter { it.isVideo }
            else -> pool
        }
        if (searchActive && searchQuery.isNotBlank()) {
            val q = searchQuery.trim()
            byType.filter {
                it.name.contains(q, ignoreCase = true) || it.bucket.contains(q, ignoreCase = true)
            }
        } else {
            byType
        }
    }

    fun pickClick(m: MediaFile) {
        if (pickMultiple) picked = if (m.uri in picked) picked - m.uri else picked + m.uri
        else finishPick(activity, listOf(m))
    }

    val unnamedAlbum = stringResource(R.string.album_unnamed)
    val albums = remember(pool, unnamedAlbum) { MediaRepo.albums(pool, unnamedAlbum) }
    val openAlbum = albums.firstOrNull { it.id == openAlbumId }
    val selectedItems = remember(all, selected) { all.filter { it.uri in selected } }

    BackHandler(enabled = selected.isNotEmpty() && tab == 0 && openAlbum == null) { selected = emptySet() }
    BackHandler(enabled = tab >= 1 && openAlbum == null && viewer == null) { tab = 0 }
    BackHandler(enabled = viewer != null) { viewer = null }
    BackHandler(enabled = pickMode && picked.isNotEmpty() && viewer == null) { picked = emptySet() }

    val layout = currentWindowLayout()
    val useRail = layout.usesNavigationRail
    val safeStartInset = safeHorizontalInsets().asPaddingValues().calculateStartPadding(LocalLayoutDirection.current)
    val railReserve = when {
        !useRail -> 0.dp
        settings.nonExpressiveNavigationBar -> HOME_STANDARD_RAIL_RESERVE
        else -> HOME_FLOATING_RAIL_RESERVE
    }
    val navBottomReserve = when {
        useRail -> 16.dp
        settings.nonExpressiveNavigationBar -> NON_EXPRESSIVE_NAV_BAR_RESERVE
        else -> EXPRESSIVE_NAV_BAR_RESERVE
    }

    val photosGridState = rememberLazyGridState()
    val albumsGridState = rememberLazyGridState()
    val trashGridState = rememberLazyGridState()
    val floatingToolbarState = rememberFloatingToolbarState()
    val floatingToolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
        state = floatingToolbarState,
    )

    val navTabs = listOf(
        HomeNavTab(0, stringResource(R.string.nav_photos), Icons.Rounded.PhotoLibrary, Icons.Outlined.PhotoLibrary),
        HomeNavTab(1, stringResource(R.string.nav_albums), Icons.Rounded.Folder, Icons.Outlined.Folder),
        HomeNavTab(2, stringResource(R.string.nav_trash), Icons.Rounded.Delete, Icons.Outlined.DeleteOutline, trash.size),
        HomeNavTab(3, stringResource(R.string.nav_settings), Icons.Rounded.Settings, Icons.Outlined.Settings),
    )

    val selectNavTab: (Int) -> Unit = { target ->
        if (target == tab) {
            scope.launch {
                when (target) {
                    0 -> photosGridState.animateScrollToItem(0)
                    1 -> albumsGridState.animateScrollToItem(0)
                    2 -> trashGridState.animateScrollToItem(0)
                }
            }
        } else {
            selected = emptySet()
            tab = target
        }
    }

    val navScrub = rememberNavBarScrubState(
        tabIds = navTabs.map { it.id },
        onHoverChange = { haptics.tick() },
        onCommit = { target ->
            if (target != tab) {
                haptics.subtle()
                selected = emptySet()
                tab = target
            }
        },
    )

    val selectionActive = (selected.isNotEmpty() && tab == 0) || (trashSelectionActive && tab == 2)

    CompositionLocalProvider(LocalThumbRegistry provides thumbRegistry) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (media == null) {
                Box(Modifier.fillMaxSize())
            } else {
                PredictiveBackStack(
                    childOpen = openAlbum != null,
                    onBack = {
                        openAlbumId = null
                        selected = emptySet()
                    },
                    background = {
                        val toolbarNestedScroll = if (
                            !settings.nonExpressiveNavigationBar &&
                            !useRail &&
                            settings.barHidesOnScroll &&
                            tab <= 2
                        ) {
                            Modifier.nestedScroll(floatingToolbarScrollBehavior)
                        } else {
                            Modifier
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = railReserve)
                                .then(toolbarNestedScroll),
                        ) {
                            AnimatedContent(
                                targetState = tab,
                                transitionSpec = { fadeThrough(settings.animations) },
                                label = "homeTabs",
                                modifier = Modifier.fillMaxSize(),
                            ) { current ->
                                when (current) {
                                    0 -> ExpressivePullToRefresh(
                                        isRefreshing = refreshing,
                                        onRefresh = {
                                            refreshing = true
                                            reload++
                                        },
                                        indicatorTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                                    ) {
                                        val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                                        MediaGrid(
                                            items = filtered,
                                            padding = PaddingValues(
                                                top = topInset,
                                                bottom = navBottomReserve + 24.dp,
                                            ),
                                            settings = settings,
                                            selected = if (pickMode) picked else selected,
                                            gridState = photosGridState,
                                            emptySearchQuery = if (searchActive) searchQuery else null,
                                            header = {
                                                PhotosHomeHeader(
                                                    showGreeting = settings.showGreeting,
                                                    itemCount = filtered.size,
                                                    filterMode = filterMode,
                                                    onFilterChange = { filterMode = it },
                                                    searchActive = searchActive,
                                                    searchQuery = searchQuery,
                                                    onSearchToggle = {
                                                        searchActive = !searchActive
                                                        if (!searchActive) searchQuery = ""
                                                    },
                                                    onSearchQueryChange = { searchQuery = it },
                                                )
                                            },
                                            onClick = { m, index ->
                                                if (pickMode) pickClick(m)
                                                else if (selected.isNotEmpty()) toggle(m)
                                                else open(filtered, index)
                                            },
                                            onLongClick = { m -> if (pickMode) pickClick(m) else toggle(m) },
                                        )
                                    }

                                    1 -> ExpressivePullToRefresh(
                                        isRefreshing = refreshing,
                                        onRefresh = {
                                            refreshing = true
                                            reload++
                                        },
                                        indicatorTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                                    ) {
                                        val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                                        AlbumsGrid(
                                            albums = albums,
                                            padding = PaddingValues(
                                                top = topInset,
                                                bottom = navBottomReserve + 24.dp,
                                            ),
                                            settings = settings,
                                            gridState = albumsGridState,
                                            onOpen = { openAlbumId = it.id },
                                        )
                                    }

                                    2 -> TrashScreen(
                                        items = trash,
                                        settings = settings,
                                        bottomPadding = navBottomReserve,
                                        selectionBottomPadding = when {
                                            useRail -> 24.dp
                                            settings.nonExpressiveNavigationBar -> 92.dp
                                            else -> 20.dp
                                        },
                                        gridState = trashGridState,
                                        onRestore = { restoreItems(it) },
                                        onDeleteForever = { deleteForever(it) },
                                        onSelectionActiveChange = { trashSelectionActive = it },
                                    )

                                    else -> SettingsScreen(
                                        settings = settings,
                                        updater = updater,
                                        onBack = { tab = 0 },
                                    )
                                }
                            }
                        }

                        // Navigation Rail or Bottom Bar / HorizontalFloatingToolbar
                        if (useRail) {
                            if (settings.nonExpressiveNavigationBar) {
                                HomeStandardRail(
                                    tabs = navTabs,
                                    selectedTab = tab,
                                    scrub = navScrub,
                                    onSelect = selectNavTab,
                                    modifier = Modifier.align(Alignment.CenterStart),
                                )
                            } else {
                                HomeFloatingRail(
                                    tabs = navTabs,
                                    selectedTab = tab,
                                    scrub = navScrub,
                                    onSelect = selectNavTab,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = safeStartInset + 12.dp),
                                )
                            }
                        } else if (settings.nonExpressiveNavigationBar) {
                            NavigationBar(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .navBarScrub(navScrub),
                            ) {
                                navTabs.forEach { item ->
                                    val isSel = (navScrub.hoveredTab ?: tab) == item.id
                                    NavigationBarItem(
                                        modifier = Modifier
                                            .weight(1f)
                                            .navBarScrubItem(navScrub, item.id),
                                        selected = isSel,
                                        onClick = { selectNavTab(item.id) },
                                        icon = { NavTabIcon(item, isSel) },
                                        label = if (settings.tabLabels == NavTabLabels.NEVER) null else {
                                            { Text(item.label, maxLines = 1) }
                                        },
                                        alwaysShowLabel = settings.tabLabels != NavTabLabels.SELECTED,
                                    )
                                }
                            }
                        } else {
                            AnimatedVisibility(
                                visible = !selectionActive,
                                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                                exit = fadeOut() + scaleOut(targetScale = 0.9f),
                                modifier = Modifier.align(Alignment.BottomCenter),
                            ) {
                                HorizontalFloatingToolbar(
                                    expanded = true,
                                    scrollBehavior = if (settings.barHidesOnScroll) floatingToolbarScrollBehavior else null,
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .padding(bottom = 20.dp)
                                        .navBarScrub(navScrub),
                                    content = {
                                        navTabs.forEach { item ->
                                            val isSel = (navScrub.hoveredTab ?: tab) == item.id
                                            val animatedPadding by animateDpAsState(
                                                targetValue = if (isSel) 20.dp else 12.dp,
                                                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                                label = "navPadding",
                                            )
                                            val containerColor by animateColorAsState(
                                                targetValue = if (isSel) {
                                                    MaterialTheme.colorScheme.primaryContainer
                                                } else {
                                                    Color.Transparent
                                                },
                                                animationSpec = tween(200),
                                                label = "navContainer",
                                            )
                                            val contentColor by animateColorAsState(
                                                targetValue = if (isSel) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                animationSpec = tween(200),
                                                label = "navContent",
                                            )
                                            Surface(
                                                selected = isSel,
                                                onClick = { selectNavTab(item.id) },
                                                shape = CircleShape,
                                                color = containerColor,
                                                contentColor = contentColor,
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .navBarScrubItem(navScrub, item.id),
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .padding(horizontal = animatedPadding)
                                                        .animateContentSize(
                                                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                                        ),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    NavTabIcon(item, isSel)
                                                    AnimatedVisibility(
                                                        visible = when (settings.tabLabels) {
                                                            NavTabLabels.ALWAYS -> true
                                                            NavTabLabels.NEVER -> false
                                                            else -> isSel
                                                        },
                                                        enter = fadeIn() + expandHorizontally(
                                                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                                        ),
                                                        exit = fadeOut() + shrinkHorizontally(
                                                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                                        ),
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Spacer(Modifier.width(8.dp))
                                                            Text(
                                                                text = item.label,
                                                                style = MaterialTheme.typography.labelLarge,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                )
                            }
                        }

                        if (!pickMode && tab == 0) {
                            MediaSelectionDeck(
                                visible = selected.isNotEmpty(),
                                selectedCount = selected.size,
                                totalCount = filtered.size,
                                allSelected = filtered.isNotEmpty() && filtered.all { it.uri in selected },
                                bottomPadding = when {
                                    useRail -> 24.dp
                                    settings.nonExpressiveNavigationBar -> 92.dp
                                    else -> 20.dp
                                },
                                snackbar = snackbar,
                                onClear = { selected = emptySet() },
                                onToggleSelectAll = {
                                    selected = if (filtered.all { it.uri in selected }) {
                                        emptySet()
                                    } else {
                                        filtered.map { it.uri }.toSet()
                                    }
                                },
                                onShare = { shareItems(selectedItems) },
                                onMove = { moveItems = selectedItems },
                                onTrash = { trashItems(selectedItems) },
                                modifier = Modifier.align(Alignment.BottomCenter),
                            )
                        }
                    },
                    foreground = { _ ->
                        openAlbum?.let { album ->
                            AlbumDetailScreen(
                                album = album,
                                settings = settings,
                                selected = if (pickMode) picked else selected,
                                pickMode = pickMode,
                                onBack = {
                                    openAlbumId = null
                                    selected = emptySet()
                                },
                                onClick = { m, index ->
                                    if (pickMode) pickClick(m)
                                    else if (selected.isNotEmpty()) toggle(m)
                                    else open(album.items, index)
                                },
                                onLongClick = { m -> if (pickMode) pickClick(m) else toggle(m) },
                                onClearSelection = { selected = emptySet() },
                                onToggleSelectAll = {
                                    selected = if (album.items.all { it.uri in selected }) {
                                        emptySet()
                                    } else {
                                        album.items.map { it.uri }.toSet()
                                    }
                                },
                                onShare = { shareItems(selectedItems) },
                                onMove = { moveItems = selectedItems },
                                onTrash = { trashItems(selectedItems) },
                            )
                        }
                    },
                )
            }

            if (pickMode && picked.isNotEmpty() && !viewerVisible) {
                ExtendedFloatingActionButton(
                    onClick = { finishPick(activity, pool.filter { it.uri in picked }) },
                    icon = { Icon(Icons.Rounded.Check, contentDescription = null) },
                    text = { Text(stringResource(R.string.pick_done, picked.size)) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 96.dp),
                )
            }

            val shown = viewer ?: lastViewer
            if (viewerVisible) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = viewerProgress.value.coerceIn(0f, 1f) }
                        .background(Color.Black.copy(alpha = 0.5f)),
                )
                shown?.let { request ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val p = viewerProgress.value
                                val o = viewerOrigin
                                val w = size.width
                                val h = size.height
                                if (o != null && w > 0f && h > 0f) {
                                    val s0 = o.width / w
                                    val s = lerp(s0, 1f, p)
                                    scaleX = s
                                    scaleY = s
                                    translationX = lerp(o.center.x - w / 2f, 0f, p)
                                    translationY = lerp(o.center.y - h / 2f, 0f, p)
                                    alpha = (p * 3f).coerceIn(0f, 1f)
                                    clip = true
                                    shape = RevealShape(p, o.height / o.width, s)
                                } else {
                                    val s = 0.84f + 0.16f * p
                                    scaleX = s
                                    scaleY = s
                                    translationY = (1f - p) * h * 0.05f
                                    alpha = p.coerceIn(0f, 1f)
                                }
                            },
                    ) {
                        ViewerScreen(
                            request = request,
                            settings = settings,
                            onClose = { viewer = null },
                            onTrash = { m -> trashItems(listOf(m)) { viewer = null } },
                            onPageChanged = { viewerCurrent = it },
                            onEdit = { m -> editing = m },
                            onDragDismiss = {
                                dragDismissed = true
                                viewer = null
                            },
                        )
                    }
                }
            }

            editing?.let { target ->
                EditorScreen(
                    media = target,
                    onClose = { editing = null },
                    onSaved = {
                        editing = null
                        reload++
                    },
                )
            }
        }
    }

    val toMove = moveItems
    if (toMove != null) {
        MoveSheet(
            albums = albums,
            currentBuckets = toMove.map { it.bucketId }.toSet(),
            onPick = { path ->
                moveItems = null
                moveItemsTo(toMove, path)
            },
            onDismiss = { moveItems = null },
        )
    }
}
