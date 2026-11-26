package com.GalleryYou.Pixel.ui.home

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.data.Album
import com.GalleryYou.Pixel.data.AppSettings
import com.GalleryYou.Pixel.data.LocalGalleryLoader
import com.GalleryYou.Pixel.data.MediaFile
import com.GalleryYou.Pixel.data.thumbRequest
import com.GalleryYou.Pixel.pressFeedback
import com.GalleryYou.Pixel.staggerIn
import com.GalleryYou.Pixel.ui.components.AmbientBackground
import com.GalleryYou.Pixel.ui.components.EmptyState
import com.GalleryYou.Pixel.ui.components.SearchEmptyState
import com.GalleryYou.Pixel.ui.components.SearchField
import com.GalleryYou.Pixel.util.rememberGalleryHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AlbumsGrid(
    albums: List<Album>,
    padding: PaddingValues,
    settings: AppSettings,
    gridState: LazyGridState = rememberLazyGridState(),
    onOpen: (Album) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredAlbums = remember(albums, searchQuery) {
        if (searchQuery.isBlank()) albums
        else albums.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    if (albums.isEmpty()) {
        EmptyState(padding, icon = Icons.Rounded.Folder)
        return
    }
    val columns = settings.albumColumns
    val spacing = if (columns <= 3) 14.dp else 10.dp
    var ambientUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    if (settings.ambientBg) {
        LaunchedEffect(gridState, filteredAlbums) {
            snapshotFlow { gridState.layoutInfo.visibleItemsInfo.map { it.index } }
                .distinctUntilChanged()
                .collectLatest { idxs ->
                    if (ambientUris.isNotEmpty()) delay(300)
                    val picks = listOfNotNull(
                        idxs.firstOrNull(),
                        idxs.getOrNull(idxs.size / 2),
                        idxs.lastOrNull(),
                    ).distinct()
                    ambientUris = picks.mapNotNull { filteredAlbums.getOrNull(it)?.cover?.uri }
                }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (settings.ambientBg) AmbientBackground(ambientUris)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
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
            item(span = { GridItemSpan(maxLineSpan) }, key = "albums_header") {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.nav_albums),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    SearchField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = stringResource(R.string.home_search_albums),
                    )
                }
            }

            if (filteredAlbums.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "albums_empty") {
                    SearchEmptyState(
                        title = stringResource(R.string.home_search_empty_title, searchQuery),
                        hint = stringResource(R.string.home_search_empty_hint),
                    )
                }
            } else {
                itemsIndexed(filteredAlbums, key = { _, a -> a.id }) { index, album ->
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
}

@Composable
fun AlbumCard(
    album: Album,
    columns: Int,
    anim: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val haptics = rememberGalleryHaptics()
    val context = LocalContext.current
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
        else -> 14.dp
    }
    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(corner))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {
                    haptics.subtle()
                    onClick()
                },
            ),
        shape = RoundedCornerShape(corner),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
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
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = album.name,
                    style = if (columns >= 4) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (columns >= 4) {
                        "${album.count}"
                    } else {
                        context.resources.getQuantityString(R.plurals.album_items, album.count, album.count)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumDetailScreen(
    album: Album,
    settings: AppSettings,
    selected: Set<Uri>,
    pickMode: Boolean = false,
    onBack: () -> Unit,
    onClick: (MediaFile, Int) -> Unit,
    onLongClick: (MediaFile) -> Unit,
    onClearSelection: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onShare: () -> Unit,
    onMove: () -> Unit,
    onTrash: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val allSelected = album.items.isNotEmpty() && album.items.all { it.uri in selected }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = album.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = stringResource(R.string.home_collection_summary, album.count),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            shapes = IconButtonDefaults.shapes(),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(44.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                )
            },
        ) { padding ->
            MediaGrid(
                items = album.items,
                padding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + if (selected.isNotEmpty()) 96.dp else 16.dp,
                ),
                settings = settings,
                selected = selected,
                onClick = onClick,
                onLongClick = onLongClick,
            )
        }

        if (!pickMode) {
            MediaSelectionDeck(
                visible = selected.isNotEmpty(),
                selectedCount = selected.size,
                totalCount = album.items.size,
                allSelected = allSelected,
                bottomPadding = 20.dp,
                snackbar = snackbar,
                onClear = onClearSelection,
                onToggleSelectAll = onToggleSelectAll,
                onShare = onShare,
                onMove = onMove,
                onTrash = onTrash,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveSheet(
    albums: List<Album>,
    currentBuckets: Set<Long>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var newAlbum by remember { mutableStateOf(false) }
    val targets = remember(albums, currentBuckets) {
        albums.filter { it.relativePath.isNotBlank() && it.id !in currentBuckets }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            stringResource(R.string.move_to_album),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        LazyColumn(modifier = Modifier.navigationBarsPadding()) {
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.new_album), fontWeight = FontWeight.SemiBold) },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.pressFeedback().clickable { newAlbum = true },
                )
            }
            items(targets, key = { it.id }) { album ->
                ListItem(
                    headlineContent = { Text(album.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supportingContent = {
                        Text(context.resources.getQuantityString(R.plurals.album_items, album.count, album.count))
                    },
                    leadingContent = {
                        AsyncImage(
                            model = thumbRequest(album.cover.uri, 160),
                            imageLoader = LocalGalleryLoader.current,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.pressFeedback().clickable { onPick(album.relativePath) },
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
        shape = RoundedCornerShape(28.dp),
        title = { Text(stringResource(R.string.new_album), fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.field_name)) },
                shape = RoundedCornerShape(16.dp),
            )
        },
        confirmButton = {
            TextButton(enabled = clean.isNotEmpty(), onClick = { onConfirm(clean) }) {
                Text(stringResource(R.string.action_move), fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
