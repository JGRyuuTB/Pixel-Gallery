package com.GalleryYou.Pixel.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.GalleryYou.Pixel.AppLanguages
import com.GalleryYou.Pixel.BuildConfig
import com.GalleryYou.Pixel.LanguageDialog
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.UpdateState
import com.GalleryYou.Pixel.Updater
import com.GalleryYou.Pixel.data.AppSettings
import com.GalleryYou.Pixel.data.findActivity
import com.GalleryYou.Pixel.ui.components.PredictiveBackStack
import com.GalleryYou.Pixel.ui.components.SearchEmptyState
import com.GalleryYou.Pixel.ui.components.SearchField
import com.GalleryYou.Pixel.ui.theme.APP_PALETTES
import com.GalleryYou.Pixel.ui.theme.ColorPaletteScreen
import com.GalleryYou.Pixel.ui.theme.DYNAMIC_PALETTE_ID
import com.GalleryYou.Pixel.ui.theme.ThemeMode
import com.GalleryYou.Pixel.ui.theme.currentWindowLayout
import com.GalleryYou.Pixel.ui.theme.isThemeDark
import kotlin.math.roundToInt

private enum class SettingsPage {
    CUSTOMIZATION,
    COLOR_PALETTE,
    DISPLAY_SIZE,
    APP_ICON,
    GALLERY,
    PLAYER,
    UPDATES,
    ABOUT,
    ;

    val parent: SettingsPage?
        get() = when (this) {
            COLOR_PALETTE, DISPLAY_SIZE, APP_ICON -> CUSTOMIZATION
            else -> null
        }

    val root: SettingsPage
        get() = parent ?: this
}

private data class SettingsSearchEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: String,
    val keywords: String,
    val page: SettingsPage,
)

@Composable
fun SettingsScreen(
    settings: AppSettings,
    updater: Updater,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val layout = currentWindowLayout()
    val twoPane = layout.supportsTwoPanes
    var openPage by rememberSaveable { mutableStateOf<SettingsPage?>(null) }
    var activeInfo by remember { mutableStateOf<SettingsInfoData?>(null) }
    var showLanguage by rememberSaveable { mutableStateOf(false) }
    val jumpController = remember { SettingsJumpController() }
    val isDark = isThemeDark(ThemeMode.fromInt(settings.themeMode))

    val activePage = openPage ?: if (twoPane) SettingsPage.CUSTOMIZATION else null

    fun navigateUp() {
        val current = openPage
        if (current != null) {
            openPage = current.parent
        } else {
            onBack()
        }
    }

    CompositionLocalProvider(
        LocalSettingsInfoSink provides { activeInfo = it },
        LocalSettingsJump provides jumpController,
    ) {
        if (twoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Box(modifier = Modifier.weight(0.42f).fillMaxHeight()) {
                    SettingsHub(
                        settings = settings,
                        updater = updater,
                        selectedPage = activePage,
                        onOpenPage = { openPage = it },
                        onOpenLanguage = { showLanguage = true },
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                )
                Box(modifier = Modifier.weight(0.58f).fillMaxHeight()) {
                    val isTopLevelPane = activePage?.parent == null
                    CompositionLocalProvider(LocalSettingsDetailShowsBack provides !isTopLevelPane) {
                        SettingsDetailContent(
                            page = activePage ?: SettingsPage.CUSTOMIZATION,
                            settings = settings,
                            updater = updater,
                            isDark = isDark,
                            onNavigate = { openPage = it },
                            onOpenLanguage = { showLanguage = true },
                            onBack = { navigateUp() },
                        )
                    }
                }
            }
        } else {
            PredictiveBackStack(
                childOpen = openPage != null,
                onBack = { navigateUp() },
                background = {
                    SettingsHub(
                        settings = settings,
                        updater = updater,
                        selectedPage = null,
                        onOpenPage = { openPage = it },
                        onOpenLanguage = { showLanguage = true },
                    )
                },
                foreground = { _ ->
                    val page = openPage
                    if (page != null) {
                        SettingsDetailContent(
                            page = page,
                            settings = settings,
                            updater = updater,
                            isDark = isDark,
                            onNavigate = { openPage = it },
                            onOpenLanguage = { showLanguage = true },
                            onBack = { navigateUp() },
                        )
                    }
                },
            )
        }
    }

    activeInfo?.let { info ->
        SettingsInfoDialog(info = info, onDismiss = { activeInfo = null })
    }

    if (showLanguage) {
        LanguageDialog(
            current = settings.language,
            onSelect = { tag ->
                showLanguage = false
                if (tag != settings.language) {
                    settings.updateLanguage(tag)
                    context.findActivity()?.recreate()
                }
            },
            onDismiss = { showLanguage = false },
        )
    }
}

@Composable
private fun SettingsDetailContent(
    page: SettingsPage,
    settings: AppSettings,
    updater: Updater,
    isDark: Boolean,
    onNavigate: (SettingsPage) -> Unit,
    onOpenLanguage: () -> Unit,
    onBack: () -> Unit,
) {
    when (page) {
        SettingsPage.CUSTOMIZATION -> CustomizationSettingsPage(
            settings = settings,
            onOpenPalette = { onNavigate(SettingsPage.COLOR_PALETTE) },
            onOpenDisplaySize = { onNavigate(SettingsPage.DISPLAY_SIZE) },
            onOpenAppIcon = { onNavigate(SettingsPage.APP_ICON) },
            onOpenLanguage = onOpenLanguage,
            onBack = onBack,
        )
        SettingsPage.COLOR_PALETTE -> ColorPaletteScreen(
            currentPalette = settings.colorPalette,
            onPaletteSelected = { settings.updateColorPalette(it) },
            isDarkMode = isDark,
            onBack = onBack,
            paletteStyle = settings.paletteStyle,
            onPaletteStyleSelected = { settings.updatePaletteStyle(it) },
            amoledDark = settings.amoledDark,
        )
        SettingsPage.DISPLAY_SIZE -> DisplaySizeSettingsPage(
            uiScale = settings.uiScale,
            onUiScaleChange = { settings.updateUiScale(it) },
            onBack = onBack,
        )
        SettingsPage.APP_ICON -> AppIconSettingsPage(
            currentIcon = settings.appIcon,
            onApplyIcon = { settings.updateAppIcon(it) },
            onBack = onBack,
        )
        SettingsPage.GALLERY -> GalleryLayoutSettingsPage(
            settings = settings,
            onBack = onBack,
        )
        SettingsPage.PLAYER -> PlayerSettingsPage(
            settings = settings,
            onBack = onBack,
        )
        SettingsPage.UPDATES -> UpdatesSettingsPage(
            settings = settings,
            updater = updater,
            onBack = onBack,
        )
        SettingsPage.ABOUT -> AboutScreen(onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsHub(
    settings: AppSettings,
    updater: Updater,
    selectedPage: SettingsPage?,
    onOpenPage: (SettingsPage) -> Unit,
    onOpenLanguage: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val jump = LocalSettingsJump.current
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val themeLabel = when (ThemeMode.fromInt(settings.themeMode)) {
        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
        ThemeMode.DARK -> stringResource(R.string.theme_dark)
    }
    val paletteName = if (settings.colorPalette == DYNAMIC_PALETTE_ID) {
        stringResource(R.string.cp_dynamic_cd)
    } else {
        APP_PALETTES.firstOrNull { it.id == settings.colorPalette }?.name ?: settings.colorPalette
    }
    val langName = AppLanguages.firstOrNull { it.tag == settings.language }?.nativeName
        ?: stringResource(R.string.language_system)
    val updateSummary = when (val st = updater.state) {
        is UpdateState.Available -> stringResource(R.string.update_status_available, st.info.version)
        is UpdateState.UpToDate -> stringResource(R.string.update_status_up_to_date, updater.currentVersion)
        is UpdateState.Checking -> stringResource(R.string.update_status_checking)
        else -> stringResource(R.string.version_label, updater.currentVersion)
    }

    val searchEntries = buildSettingsSearchEntries(settings)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = SettingsBottomClearance),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.nav_settings),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Spacer(Modifier.height(14.dp))
                SearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = stringResource(R.string.settings_search_placeholder),
                )
                if (searchQuery.isBlank()) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_hold_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                val matches = searchEntries.filter {
                    it.title.lowercase().contains(q) ||
                        it.subtitle.lowercase().contains(q) ||
                        it.keywords.lowercase().contains(q) ||
                        it.category.lowercase().contains(q)
                }
                if (matches.isEmpty()) {
                    item {
                        SearchEmptyState(
                            title = stringResource(R.string.settings_search_empty_title, searchQuery),
                            hint = stringResource(R.string.settings_search_empty_hint),
                        )
                    }
                } else {
                    item {
                        SettingsCard {
                            matches.forEachIndexed { index, entry ->
                                if (index > 0) SettingsDivider()
                                SettingsRow(
                                    icon = entry.icon,
                                    title = entry.title,
                                    subtitle = "${entry.category} · ${entry.subtitle}",
                                    onClick = {
                                        focusManager.clearFocus()
                                        jump?.request(entry.title)
                                        onOpenPage(entry.page)
                                    },
                                    showChevron = true,
                                )
                            }
                        }
                    }
                }
            } else {
                item {
                    SettingsSection(title = stringResource(R.string.settings_section_look_and_feel)) {
                        SettingsCard {
                            SettingsHubRow(
                                icon = Icons.Rounded.Palette,
                                title = stringResource(R.string.settings_customization),
                                value = buildString {
                                    append(themeLabel)
                                    append(", ")
                                    append(paletteName)
                                    if (settings.uiScale != 1f) {
                                        append(" · ${(settings.uiScale * 100f).roundToInt()}%")
                                    }
                                },
                                onClick = { onOpenPage(SettingsPage.CUSTOMIZATION) },
                                selected = selectedPage?.root == SettingsPage.CUSTOMIZATION,
                                tint = MaterialTheme.colorScheme.tertiary,
                                iconShape = MaterialShapes.Cookie9Sided.toShape(),
                                explanation = stringResource(R.string.si_hub_customization),
                            )
                        }
                    }
                }

                item {
                    SettingsSection(title = stringResource(R.string.settings_section_gallery)) {
                        SettingsCard {
                            SettingsHubRow(
                                icon = Icons.Rounded.PhotoLibrary,
                                title = stringResource(R.string.settings_photos_albums),
                                value = "${settings.photoColumns} × ${settings.albumColumns}",
                                onClick = { onOpenPage(SettingsPage.GALLERY) },
                                selected = selectedPage == SettingsPage.GALLERY,
                                tint = MaterialTheme.colorScheme.secondary,
                                iconShape = MaterialShapes.SoftBurst.toShape(),
                                explanation = stringResource(R.string.si_hub_gallery),
                            )
                            SettingsDivider()
                            SettingsHubRow(
                                icon = Icons.Rounded.PlayCircle,
                                title = stringResource(R.string.settings_player),
                                value = if (settings.playerFloating) {
                                    stringResource(R.string.player_panel_floating)
                                } else {
                                    stringResource(R.string.player_panel_default)
                                },
                                onClick = { onOpenPage(SettingsPage.PLAYER) },
                                selected = selectedPage == SettingsPage.PLAYER,
                                tint = MaterialTheme.colorScheme.secondary,
                                iconShape = MaterialShapes.Gem.toShape(),
                                explanation = stringResource(R.string.si_hub_player),
                            )
                        }
                    }
                }

                item {
                    SettingsSection(title = stringResource(R.string.settings_section_system)) {
                        SettingsCard {
                            SettingsHubRow(
                                icon = Icons.Rounded.Language,
                                title = stringResource(R.string.settings_language),
                                value = langName,
                                onClick = onOpenLanguage,
                                iconShape = MaterialShapes.Pentagon.toShape(),
                                explanation = stringResource(R.string.si_hub_language),
                            )
                            SettingsDivider()
                            SettingsHubRow(
                                icon = Icons.Rounded.SystemUpdate,
                                title = stringResource(R.string.settings_updates),
                                value = updateSummary,
                                onClick = { onOpenPage(SettingsPage.UPDATES) },
                                selected = selectedPage == SettingsPage.UPDATES,
                                iconShape = MaterialShapes.Clover8Leaf.toShape(),
                                explanation = stringResource(R.string.si_hub_updates),
                            )
                        }
                    }
                }

                item {
                    SettingsSection(title = stringResource(R.string.settings_section_about)) {
                        SettingsCard {
                            SettingsRow(
                                icon = Icons.Rounded.Info,
                                title = stringResource(R.string.app_name),
                                subtitle = stringResource(
                                    R.string.settings_version_subtitle,
                                    BuildConfig.VERSION_NAME,
                                    BuildConfig.VERSION_CODE,
                                ),
                                onClick = { onOpenPage(SettingsPage.ABOUT) },
                                showChevron = true,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun buildSettingsSearchEntries(settings: AppSettings): List<SettingsSearchEntry> {
    val look = stringResource(R.string.settings_customization)
    val gallery = stringResource(R.string.settings_photos_albums)
    val player = stringResource(R.string.settings_player)
    val updates = stringResource(R.string.settings_updates)
    return listOf(
        SettingsSearchEntry(
            title = stringResource(R.string.settings_theme_mode),
            subtitle = stringResource(R.string.settings_theme_mode_desc),
            icon = Icons.Rounded.Palette,
            category = look,
            keywords = "dark light system theme",
            page = SettingsPage.CUSTOMIZATION,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_amoled_dark),
            subtitle = stringResource(R.string.settings_amoled_dark_desc),
            icon = Icons.Rounded.Palette,
            category = look,
            keywords = "amoled black oled",
            page = SettingsPage.CUSTOMIZATION,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.sp_color_palette),
            subtitle = stringResource(R.string.settings_color_palette_desc),
            icon = Icons.Rounded.Palette,
            category = look,
            keywords = "colors palette dynamic material you",
            page = SettingsPage.COLOR_PALETTE,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.sp_display_size),
            subtitle = stringResource(R.string.settings_display_size_desc),
            icon = Icons.Rounded.Palette,
            category = look,
            keywords = "scale dpi zoom size",
            page = SettingsPage.DISPLAY_SIZE,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_app_icon),
            subtitle = stringResource(R.string.settings_app_icon_desc),
            icon = Icons.Rounded.Palette,
            category = look,
            keywords = "launcher icon anime pixel",
            page = SettingsPage.APP_ICON,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_blur),
            subtitle = stringResource(R.string.settings_blur_desc),
            icon = Icons.Rounded.Palette,
            category = look,
            keywords = "blur glass liquid acrylic",
            page = SettingsPage.CUSTOMIZATION,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_haptics),
            subtitle = stringResource(R.string.settings_haptics_desc),
            icon = Icons.Rounded.Palette,
            category = look,
            keywords = "vibration feedback haptics",
            page = SettingsPage.CUSTOMIZATION,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_columns_photos),
            subtitle = stringResource(R.string.settings_columns_photos_desc),
            icon = Icons.Rounded.PhotoLibrary,
            category = gallery,
            keywords = "grid columns photos pinch",
            page = SettingsPage.GALLERY,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_columns_albums),
            subtitle = stringResource(R.string.onboard_columns),
            icon = Icons.Rounded.PhotoLibrary,
            category = gallery,
            keywords = "albums columns",
            page = SettingsPage.GALLERY,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_seek_style),
            subtitle = stringResource(R.string.settings_seek_style_desc),
            icon = Icons.Rounded.PlayCircle,
            category = player,
            keywords = "progress seek bar wave neon equalizer",
            page = SettingsPage.PLAYER,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_player_panel),
            subtitle = stringResource(R.string.settings_player_panel_desc),
            icon = Icons.Rounded.PlayCircle,
            category = player,
            keywords = "floating player controls border",
            page = SettingsPage.PLAYER,
        ),
        SettingsSearchEntry(
            title = stringResource(R.string.settings_check_updates),
            subtitle = stringResource(R.string.settings_auto_update_desc),
            icon = Icons.Rounded.SystemUpdate,
            category = updates,
            keywords = "ota github release update",
            page = SettingsPage.UPDATES,
        ),
    )
}
