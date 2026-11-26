package com.GalleryYou.Pixel.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.ui.components.DismissibleSnackbarHost
import com.GalleryYou.Pixel.util.rememberGalleryHaptics

/**
 * Floating selection deck replacing the Home navigation toolbar whenever items are selected.
 * Hosts a [DismissibleSnackbarHost] above the bar so confirmation messages stay visible after clearing selection.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MediaSelectionDeck(
    visible: Boolean,
    selectedCount: Int,
    totalCount: Int,
    allSelected: Boolean,
    trashMode: Boolean = false,
    bottomPadding: Dp,
    snackbar: SnackbarHostState,
    onClear: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onShare: (() -> Unit)? = null,
    onMove: (() -> Unit)? = null,
    onTrash: (() -> Unit)? = null,
    onRestore: (() -> Unit)? = null,
    onDeleteForever: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberGalleryHaptics()

    BackHandler(enabled = visible) { onClear() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = bottomPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DismissibleSnackbarHost(hostState = snackbar)
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + scaleIn(
                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialScale = 0.85f,
                ),
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
            ) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    modifier = Modifier.widthIn(max = 560.dp),
                    content = {
                        IconButton(
                            onClick = {
                                haptics.subtle()
                                onClear()
                            },
                            shapes = IconButtonDefaults.shapes(),
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.sel_close))
                        }

                        SelectedCount(selectedCount)

                        if (totalCount > 1) {
                            IconButton(
                                onClick = {
                                    haptics.tick()
                                    onToggleSelectAll()
                                },
                                shapes = IconButtonDefaults.shapes(),
                            ) {
                                Icon(
                                    imageVector = if (allSelected) Icons.Rounded.Deselect else Icons.Rounded.SelectAll,
                                    contentDescription = stringResource(
                                        if (allSelected) R.string.sel_select_none else R.string.sel_select_all,
                                    ),
                                )
                            }
                        }

                        if (!trashMode) {
                            if (onShare != null) {
                                IconButton(
                                    onClick = {
                                        haptics.confirm()
                                        onShare()
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.sel_share))
                                }
                            }
                            if (onMove != null) {
                                IconButton(
                                    onClick = {
                                        haptics.confirm()
                                        onMove()
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.DriveFileMove,
                                        contentDescription = stringResource(R.string.sel_move),
                                    )
                                }
                            }
                            if (onTrash != null) {
                                IconButton(
                                    onClick = {
                                        haptics.reject()
                                        onTrash()
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = stringResource(R.string.sel_trash),
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        } else {
                            if (onRestore != null) {
                                IconButton(
                                    onClick = {
                                        haptics.confirm()
                                        onRestore()
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        Icons.Rounded.RestoreFromTrash,
                                        contentDescription = stringResource(R.string.sel_restore),
                                    )
                                }
                            }
                            if (onDeleteForever != null) {
                                IconButton(
                                    onClick = {
                                        haptics.reject()
                                        onDeleteForever()
                                    },
                                    shapes = IconButtonDefaults.shapes(),
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteForever,
                                        contentDescription = stringResource(R.string.sel_delete_forever),
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectedCount(count: Int) {
    val label = LocalContext.current.resources.getQuantityString(R.plurals.sel_count, count, count)
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(36.dp)
            .semantics { contentDescription = label },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    val up = targetState > initialState
                    (slideInVertically(spring(stiffness = Spring.StiffnessMedium)) { if (up) it else -it } + fadeIn()) togetherWith
                        (slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { if (up) -it else it } + fadeOut())
                },
                label = "SelectedCount",
            ) { shown ->
                Text(
                    text = shown.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
