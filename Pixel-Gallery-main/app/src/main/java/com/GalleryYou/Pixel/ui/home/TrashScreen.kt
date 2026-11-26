package com.GalleryYou.Pixel.ui.home

import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.data.AppSettings
import com.GalleryYou.Pixel.data.MediaFile
import com.GalleryYou.Pixel.ui.components.EmptyState
import com.GalleryYou.Pixel.util.rememberGalleryHaptics

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrashScreen(
    items: List<MediaFile>,
    settings: AppSettings,
    bottomPadding: Dp,
    selectionBottomPadding: Dp,
    gridState: LazyGridState = rememberLazyGridState(),
    onRestore: (List<MediaFile>) -> Unit,
    onDeleteForever: (List<MediaFile>) -> Unit,
    onSelectionActiveChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val haptics = rememberGalleryHaptics()
    var selected by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var confirm by remember { mutableStateOf<List<MediaFile>?>(null) }
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val picked = remember(items, selected) { items.filter { it.uri in selected } }
    val snackbar = remember { SnackbarHostState() }

    onSelectionActiveChange(selected.isNotEmpty())
    BackHandler(enabled = selected.isNotEmpty()) { selected = emptySet() }

    fun toggle(m: MediaFile) {
        selected = if (m.uri in selected) selected - m.uri else selected + m.uri
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.nav_trash),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.home_collection_summary, items.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (items.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            haptics.reject()
                            confirm = items
                        },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            Icons.Rounded.DeleteSweep,
                            contentDescription = stringResource(R.string.cd_empty_trash),
                        )
                    }
                }
            }

            val gridPadding = PaddingValues(bottom = bottomPadding + 16.dp)
            when {
                !supported -> EmptyState(gridPadding, stringResource(R.string.trash_needs_android11), Icons.Rounded.DeleteOutline)
                items.isEmpty() -> EmptyState(gridPadding, stringResource(R.string.trash_empty), Icons.Rounded.DeleteOutline)
                else -> MediaGrid(
                    items = items,
                    padding = gridPadding,
                    settings = settings,
                    selected = selected,
                    gridState = gridState,
                    header = {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.trash_notice),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    onClick = { m, _ -> toggle(m) },
                    onLongClick = { m -> toggle(m) },
                )
            }
        }

        MediaSelectionDeck(
            visible = selected.isNotEmpty(),
            selectedCount = selected.size,
            totalCount = items.size,
            allSelected = items.isNotEmpty() && items.all { it.uri in selected },
            trashMode = true,
            bottomPadding = selectionBottomPadding,
            snackbar = snackbar,
            onClear = { selected = emptySet() },
            onToggleSelectAll = {
                selected = if (items.all { it.uri in selected }) emptySet() else items.map { it.uri }.toSet()
            },
            onRestore = {
                onRestore(picked)
                selected = emptySet()
            },
            onDeleteForever = { confirm = picked },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    val toDelete = confirm
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { confirm = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(R.string.delete_forever), fontWeight = FontWeight.Bold) },
            text = {
                Text(context.resources.getQuantityString(R.plurals.delete_confirm, toDelete.size, toDelete.size))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteForever(toDelete)
                        confirm = null
                        selected = emptySet()
                    },
                ) {
                    Text(
                        stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = null }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}
