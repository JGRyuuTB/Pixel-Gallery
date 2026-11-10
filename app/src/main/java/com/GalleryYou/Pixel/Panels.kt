package com.GalleryYou.Pixel

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.core.graphics.drawable.toBitmap
import androidx.core.content.res.ResourcesCompat
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.animateColorAsState
import androidx.annotation.DrawableRes
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.ui.Alignment
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// ------------------------------------------------------------------ Tanjiro huele un bug a kilómetros

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settings: AppSettings, updater: Updater, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val canDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val gapValues = listOf(0, 2, 6)
    var showAbout by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showAbout) { showAbout = false }
    var showLanguage by rememberSaveable { mutableStateOf(false) }
    val updateStatus = when (val st = updater.state) {
        is UpdateState.Checking -> stringResource(R.string.update_status_checking)
        is UpdateState.Available -> stringResource(R.string.update_status_available, st.info.version)
        is UpdateState.UpToDate -> stringResource(R.string.update_status_up_to_date, updater.currentVersion)
        is UpdateState.Downloading -> stringResource(R.string.update_status_downloading)
        is UpdateState.Ready -> stringResource(R.string.update_status_ready)
        is UpdateState.Failed -> st.message
        is UpdateState.Idle -> stringResource(R.string.version_label, updater.currentVersion)
    }
    val languageName = AppLanguages.firstOrNull { it.tag == settings.language }?.nativeName
        ?: stringResource(R.string.language_system)

    AnimatedContent(
        targetState = showAbout,
        transitionSpec = {
            if (!settings.animations) {
                EnterTransition.None togetherWith ExitTransition.None
            } else if (targetState) {
                (slideInHorizontally(tween(300)) { w -> w / 3 } + fadeIn(tween(240))) togetherWith
                    (slideOutHorizontally(tween(300)) { w -> -w / 6 } + fadeOut(tween(180)))
            } else {
                (slideInHorizontally(tween(300)) { w -> -w / 6 } + fadeIn(tween(240))) togetherWith
                    (slideOutHorizontally(tween(300)) { w -> w / 3 } + fadeOut(tween(180)))
            }
        },
        label = "about",
    ) { about ->
    if (about) {
        AboutScreen(onBack = { showAbout = false })
    } else {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                // Makima lo tiene todo bajo control
                .padding(bottom = LocalNavInset.current),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CompositionLocalProvider(LocalGroupCounter provides remember { GroupCounter() }) {
            SettingsGroup(stringResource(R.string.settings_language)) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_language)) },
                    supportingContent = { Text(languageName) },
                    leadingContent = { Icon(AppIcons.Globe, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { showLanguage = true },
                )
            }

            SettingsGroup(stringResource(R.string.settings_appearance)) {
                ChoiceRow(
                    title = stringResource(R.string.settings_theme),
                    options = listOf(
                        stringResource(R.string.theme_system),
                        stringResource(R.string.theme_light),
                        stringResource(R.string.theme_dark),
                    ),
                    selected = settings.themeMode,
                    onSelect = { settings.updateThemeMode(it) },
                )
                SwitchRow(
                    title = stringResource(R.string.settings_dynamic_colors),
                    subtitle = if (canDynamic) {
                        stringResource(R.string.settings_dynamic_colors_on)
                    } else {
                        stringResource(R.string.requires_android12)
                    },
                    checked = settings.dynamicColor && canDynamic,
                    enabled = canDynamic,
                    onChange = { settings.updateDynamicColor(it) },
                )
                val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ScrollChoiceRow(
                    title = stringResource(R.string.settings_blur),
                    subtitle = if (canBlur) {
                        stringResource(R.string.settings_blur_desc)
                    } else {
                        stringResource(R.string.requires_android12)
                    },
                    options = listOf(stringResource(R.string.option_none)) +
                        BlurStyles.map { stringResource(it.nameRes) },
                    selected = if (canBlur) settings.blur else 0,
                    onSelect = { if (canBlur) settings.updateBlur(it) },
                )
                SwitchRow(
                    title = stringResource(R.string.settings_liquid_glass),
                    subtitle = stringResource(R.string.settings_liquid_glass_desc),
                    checked = settings.liquidGlass,
                    onChange = { settings.updateLiquidGlass(it) },
                )
                SwitchRow(
                    title = stringResource(R.string.settings_animations),
                    subtitle = stringResource(R.string.settings_animations_desc),
                    checked = settings.animations,
                    onChange = { settings.updateAnimations(it) },
                )
            }

            SettingsGroup(stringResource(R.string.nav_style_title)) {
                val canBlurNav = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ScrollChoiceRow(
                    title = stringResource(R.string.nav_style_title),
                    options = NavStyleNames.map { stringResource(it) },
                    selected = settings.navStyle,
                    onSelect = {
                        settings.updateNavStyle(it)
                        // Yare yare daze, qué complicado
                        if (it == 2 && canBlurNav && settings.blur == 0) settings.updateBlur(BLUR_DARK)
                    },
                )
                SliderRow(
                    title = stringResource(R.string.nav_tune_height),
                    valueText = "${settings.navHeight}%",
                    value = settings.navHeight,
                    range = 70..130,
                    onChange = { settings.updateNavHeight(it) },
                )
                SliderRow(
                    title = stringResource(R.string.nav_tune_width),
                    subtitle = stringResource(R.string.nav_tune_width_note),
                    valueText = "${settings.navWidth}%",
                    value = settings.navWidth,
                    range = 60..100,
                    onChange = { settings.updateNavWidth(it) },
                )
                SliderRow(
                    title = stringResource(R.string.nav_tune_bottom),
                    valueText = "${settings.navBottom} dp",
                    value = settings.navBottom,
                    range = 0..40,
                    onChange = { settings.updateNavBottom(it) },
                )
                SliderRow(
                    title = stringResource(R.string.nav_tune_icon),
                    valueText = "${settings.navIcon}%",
                    value = settings.navIcon,
                    range = 80..130,
                    onChange = { settings.updateNavIcon(it) },
                )
                SliderRow(
                    title = stringResource(R.string.nav_tune_round),
                    valueText = "${settings.navRound}%",
                    value = settings.navRound,
                    range = 0..100,
                    onChange = { settings.updateNavRound(it) },
                )
                TextButton(
                    onClick = { settings.resetNavTuning() },
                    modifier = Modifier.align(Alignment.End).padding(end = 8.dp),
                ) { Text(stringResource(R.string.nav_tune_reset)) }
            }

            SettingsGroup(stringResource(R.string.settings_photos_albums)) {
                ChoiceRow(
                    title = stringResource(R.string.settings_columns_photos),
                    subtitle = stringResource(R.string.settings_columns_photos_desc),
                    options = listOf("2", "3", "4", "5"),
                    selected = settings.photoColumns - 2,
                    onSelect = { settings.updatePhotoColumns(it + 2) },
                )
                ChoiceRow(
                    title = stringResource(R.string.settings_columns_albums),
                    options = listOf("2", "3", "4", "5"),
                    selected = settings.albumColumns - 2,
                    onSelect = { settings.updateAlbumColumns(it + 2) },
                )
                ChoiceRow(
                    title = stringResource(R.string.settings_gap),
                    options = listOf(
                        stringResource(R.string.option_none),
                        stringResource(R.string.gap_small),
                        stringResource(R.string.gap_large),
                    ),
                    selected = gapValues.indexOf(settings.gap).coerceAtLeast(0),
                    onSelect = { settings.updateGap(gapValues[it]) },
                )
                SwitchRow(
                    title = stringResource(R.string.settings_rounded),
                    subtitle = stringResource(R.string.settings_rounded_desc),
                    checked = settings.roundedCorners,
                    onChange = { settings.updateRoundedCorners(it) },
                )
            }

            SettingsGroup(stringResource(R.string.settings_player)) {
                ChoiceRow(
                    title = stringResource(R.string.settings_player_border),
                    options = listOf(
                        stringResource(R.string.option_none),
                        stringResource(R.string.border_theme),
                        stringResource(R.string.border_animated),
                    ),
                    selected = settings.playerBorder,
                    onSelect = { settings.updatePlayerBorder(it) },
                )
                ChoiceRow(
                    title = stringResource(R.string.settings_player_panel),
                    subtitle = stringResource(R.string.settings_player_panel_desc),
                    options = listOf(
                        stringResource(R.string.player_panel_default),
                        stringResource(R.string.player_panel_floating),
                    ),
                    selected = if (settings.playerFloating) 1 else 0,
                    onSelect = { settings.updatePlayerFloating(it == 1) },
                )
                SwitchRow(
                    title = stringResource(R.string.settings_autoplay),
                    subtitle = stringResource(R.string.settings_autoplay_desc),
                    checked = settings.autoplay,
                    onChange = { settings.updateAutoplay(it) },
                )
            }

            SettingsGroup(stringResource(R.string.settings_app_icon)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(stringResource(R.string.settings_icon), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.settings_icon_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconPicker(selected = settings.appIcon, animations = settings.animations) {
                    settings.updateAppIcon(it)
                    applyAppIcon(context, it)
                }
            }

            SettingsGroup(stringResource(R.string.settings_updates)) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_check_updates)) },
                    supportingContent = { Text(updateStatus) },
                    leadingContent = { Icon(AppIcons.Download, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        val st = updater.state
                        if (st is UpdateState.Available || st is UpdateState.Ready || st is UpdateState.Downloading) {
                            updater.dialogVisible = true
                        } else {
                            scope.launch { updater.check(manual = true) }
                        }
                    },
                )
                SwitchRow(
                    title = stringResource(R.string.settings_auto_update),
                    subtitle = stringResource(R.string.settings_auto_update_desc),
                    checked = settings.autoUpdate,
                    onChange = { settings.updateAutoUpdate(it) },
                )
            }

            SettingsGroup(stringResource(R.string.settings_about)) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.app_name)) },
                    supportingContent = { Text(stringResource(R.string.version_stable, appVersion(context))) },
                    leadingContent = { Icon(AppIcons.Info, contentDescription = null) },
                    trailingContent = { Icon(AppIcons.ArrowForward, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { showAbout = true },
                )
            }

            Spacer(Modifier.height(16.dp))
            }
        }
    }
    }
    }

    if (showLanguage) {
        LanguageDialog(
            current = settings.language,
            onSelect = { tag ->
                showLanguage = false
                if (tag != settings.language) {
                    settings.updateLanguage(tag)
                    // Muda muda muda: nada de código muerto
                    context.findActivity()?.recreate()
                }
            },
            onDismiss = { showLanguage = false },
        )
    }
}

/** Cuenta los grupos de Ajustes para escalonar su entrada (uno nuevo por pantalla). */
private class GroupCounter { var n = 0 }

private val LocalGroupCounter = staticCompositionLocalOf { GroupCounter() }

/** Cuadrícula de iconos de la app con vista previa real del icono adaptativo. */
@Composable
private fun IconPicker(selected: Int, animations: Boolean, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
        AppIconOptions.chunked(4).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                rowItems.forEachIndexed { col, option ->
                    val index = rowIndex * 4 + col
                    IconTile(option, index == selected, animations) { onSelect(index) }
                }
                repeat(4 - rowItems.size) { Spacer(Modifier.width(72.dp)) }
            }
        }
    }
}

@Composable
private fun IconTile(option: AppIconOption, selected: Boolean, animations: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bitmap = rememberIconBitmap(option.previewRes, 192)
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = if (animations) spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium) else snap(),
        label = "iconTileScale",
    )
    val ring by animateColorAsState(if (selected) scheme.primary else Color.Transparent, label = "iconTileRing")
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(2.5.dp, ring, CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(scheme.primaryContainer, CircleShape))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(option.nameRes),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/** Dibuja el icono adaptativo tal como lo enmascara el launcher (con los colores del tema si es Material You). */
@Composable
private fun rememberIconBitmap(@DrawableRes res: Int, sizePx: Int): ImageBitmap? {
    val context = LocalContext.current
    return remember(res, sizePx) {
        try {
            ResourcesCompat.getDrawable(context.resources, res, context.theme)
                ?.toBitmap(sizePx, sizePx)
                ?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    val counter = LocalGroupCounter.current
    val order = remember { counter.n++ }
    Column(Modifier.staggerIn(order, stepMs = 55)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        )
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(enabled = enabled) { onChange(!checked) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceRow(
    title: String,
    options: List<String>,
    selected: Int,
    subtitle: String? = null,
    onSelect: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = index == selected,
                    onClick = { onSelect(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    icon = {},
                    label = { Text(label) },
                )
            }
        }
    }
}

/** Tsundere: parece fría, pero funciona */
@Composable
private fun SliderRow(
    title: String,
    valueText: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    subtitle: String? = null,
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(valueText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
        )
    }
}

/** Aquí dentro vive un Pokémon salvaje */
@Composable
private fun ScrollChoiceRow(
    title: String,
    options: List<String>,
    selected: Int,
    subtitle: String? = null,
    onSelect: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEachIndexed { index, label ->
                FilterChip(
                    selected = index == selected,
                    onClick = { onSelect(index) },
                    label = { Text(label) },
                )
            }
        }
    }
}

// ------------------------------------------------------------------ Mensaje oculto (rot13): Fv rapbagenfgr rfgb, ln rerf cnegr qry terzvb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    items: List<MediaFile>,
    settings: AppSettings,
    onBack: () -> Unit,
    onRestore: (List<MediaFile>) -> Unit,
    onDeleteForever: (List<MediaFile>) -> Unit,
) {
    var selected by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var confirm by remember { mutableStateOf<List<MediaFile>?>(null) }
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val picked = items.filter { it.uri in selected }

    BackHandler(enabled = selected.isNotEmpty()) { selected = emptySet() }

    fun toggle(m: MediaFile) {
        selected = if (m.uri in selected) selected - m.uri else selected + m.uri
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (selected.isNotEmpty()) {
                SelectionTopBar(
                    count = selected.size,
                    onClose = { selected = emptySet() },
                    onSelectAll = { selected = items.map { it.uri }.toSet() },
                ) {
                    IconButton(onClick = {
                        onRestore(picked)
                        selected = emptySet()
                    }) {
                        Icon(AppIcons.TrashRestore, contentDescription = stringResource(R.string.cd_restore))
                    }
                    IconButton(onClick = { confirm = picked }) {
                        Icon(AppIcons.TrashX, contentDescription = stringResource(R.string.delete_forever))
                    }
                }
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_trash)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(AppIcons.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                        }
                    },
                    actions = {
                        if (items.isNotEmpty()) {
                            IconButton(onClick = { confirm = items }) {
                                Icon(AppIcons.Trash, contentDescription = stringResource(R.string.cd_empty_trash))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        },
    ) { padding ->
        val gridPadding = PaddingValues(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding() + LocalNavInset.current,
        )
        when {
            !supported -> EmptyState(gridPadding, stringResource(R.string.trash_needs_android11))
            items.isEmpty() -> EmptyState(gridPadding, stringResource(R.string.trash_empty))
            else -> MediaGrid(
                items = items,
                padding = gridPadding,
                settings = settings,
                selected = selected,
                header = {
                    Text(
                        stringResource(R.string.trash_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                },
                onClick = { m, _ -> toggle(m) },
                onLongClick = { m -> toggle(m) },
            )
        }
    }

    val toDelete = confirm
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(stringResource(R.string.delete_forever)) },
            text = {
                Text(quantityString(R.plurals.delete_confirm, toDelete.size))
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteForever(toDelete)
                    confirm = null
                    selected = emptySet()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}
