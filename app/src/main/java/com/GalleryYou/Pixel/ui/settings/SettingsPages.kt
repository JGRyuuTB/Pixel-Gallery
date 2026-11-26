package com.GalleryYou.Pixel.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoAlbum
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RoundedCorner
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material.icons.rounded.SwipeDown
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.ViewColumn
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material.icons.rounded.WidthNormal
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.GalleryYou.Pixel.AppLanguages
import com.GalleryYou.Pixel.BlurStyles
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.SeekStylePicker
import com.GalleryYou.Pixel.UpdateState
import com.GalleryYou.Pixel.Updater
import com.GalleryYou.Pixel.data.AppIconOptions
import com.GalleryYou.Pixel.data.AppSettings
import com.GalleryYou.Pixel.data.NavTabLabels
import com.GalleryYou.Pixel.ui.theme.APP_PALETTES
import com.GalleryYou.Pixel.ui.theme.DYNAMIC_PALETTE_ID
import com.GalleryYou.Pixel.ui.theme.ThemeMode
import com.GalleryYou.Pixel.util.HapticsLevel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
internal fun CustomizationSettingsPage(
    settings: AppSettings,
    onOpenPalette: () -> Unit,
    onOpenDisplaySize: () -> Unit,
    onOpenAppIcon: () -> Unit,
    onOpenLanguage: () -> Unit,
    onBack: () -> Unit,
) {
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val paletteName = if (settings.colorPalette == DYNAMIC_PALETTE_ID) {
        stringResource(R.string.cp_dynamic_cd)
    } else {
        APP_PALETTES.firstOrNull { it.id == settings.colorPalette }?.name ?: settings.colorPalette
    }
    val iconName = stringResource(AppIconOptions[settings.appIcon].nameRes)
    val langName = AppLanguages.firstOrNull { it.tag == settings.language }?.nativeName
        ?: stringResource(R.string.language_system)
    val blurLabel = if (settings.blur == 0) {
        stringResource(R.string.option_none)
    } else {
        stringResource(BlurStyles[settings.blur - 1].nameRes)
    }

    SettingsDetailScaffold(
        title = stringResource(R.string.settings_customization),
        onBack = onBack,
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_theme)) {
                SettingsCard {
                    SettingsChoiceRow(
                        icon = Icons.Rounded.Brightness4,
                        title = stringResource(R.string.settings_theme_mode),
                        subtitle = stringResource(R.string.settings_theme_mode_desc),
                        labels = listOf(
                            stringResource(R.string.theme_system),
                            stringResource(R.string.theme_light),
                            stringResource(R.string.theme_dark),
                        ),
                        selectedIndex = settings.themeMode.coerceIn(0, 2),
                        onSelect = { settings.updateThemeMode(ThemeMode.fromInt(it)) },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.DarkMode,
                        title = stringResource(R.string.settings_amoled_dark),
                        subtitle = stringResource(R.string.settings_amoled_dark_desc),
                        enabled = settings.amoledDark,
                        onToggle = { settings.updateAmoledDark(it) },
                        explanation = stringResource(R.string.si_toggle_amoled),
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Palette,
                        title = stringResource(R.string.sp_color_palette),
                        subtitle = "$paletteName · ${stringResource(settings.paletteStyle.labelRes)}",
                        onClick = onOpenPalette,
                        showChevron = true,
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.FormatSize,
                        title = stringResource(R.string.sp_display_size),
                        subtitle = stringResource(R.string.sp_display_size_percent, (settings.uiScale * 100f).roundToInt()),
                        onClick = onOpenDisplaySize,
                        showChevron = true,
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Apps,
                        title = stringResource(R.string.settings_app_icon),
                        subtitle = iconName,
                        onClick = onOpenAppIcon,
                        showChevron = true,
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_navigation_bar)) {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Dock,
                        title = stringResource(R.string.settings_non_expressive_nav),
                        subtitle = stringResource(R.string.settings_non_expressive_nav_desc),
                        enabled = settings.nonExpressiveNavigationBar,
                        onToggle = { settings.updateNonExpressiveNavigationBar(it) },
                    )
                    SettingsDivider()
                    SettingsChoiceRow(
                        icon = Icons.Rounded.Label,
                        title = stringResource(R.string.settings_tab_labels),
                        subtitle = stringResource(R.string.settings_tab_labels_desc),
                        labels = listOf(
                            stringResource(R.string.tab_labels_selected),
                            stringResource(R.string.tab_labels_always),
                            stringResource(R.string.tab_labels_never),
                        ),
                        selectedIndex = NavTabLabels.entries.indexOf(settings.tabLabels).coerceIn(0, 2),
                        onSelect = { settings.updateTabLabels(NavTabLabels.entries[it]) },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.SwipeDown,
                        title = stringResource(R.string.settings_bar_hides_on_scroll),
                        subtitle = stringResource(R.string.settings_bar_hides_on_scroll_desc),
                        enabled = settings.barHidesOnScroll,
                        onToggle = { settings.updateBarHidesOnScroll(it) },
                        available = !settings.nonExpressiveNavigationBar,
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.WavingHand,
                        title = stringResource(R.string.settings_show_greeting),
                        subtitle = stringResource(R.string.settings_show_greeting_desc),
                        enabled = settings.showGreeting,
                        onToggle = { settings.updateShowGreeting(it) },
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_appearance)) {
                SettingsCard {
                    SettingsSliderRow(
                        icon = Icons.Rounded.BlurOn,
                        title = stringResource(R.string.settings_blur),
                        subtitle = if (canBlur) stringResource(R.string.settings_blur_desc) else stringResource(R.string.requires_android12),
                        valueText = blurLabel,
                        value = settings.blur,
                        range = 0..BlurStyles.size,
                        onChange = { settings.updateBlur(it) },
                        enabled = canBlur,
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = stringResource(R.string.settings_liquid_glass),
                        subtitle = stringResource(R.string.settings_liquid_glass_desc),
                        enabled = settings.liquidGlass,
                        onToggle = { settings.updateLiquidGlass(it) },
                        available = canBlur && settings.blur > 0,
                        explanation = stringResource(R.string.si_toggle_blur),
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Wallpaper,
                        title = stringResource(R.string.settings_ambient_bg),
                        subtitle = stringResource(R.string.settings_ambient_bg_desc),
                        enabled = settings.ambientBg,
                        onToggle = { settings.updateAmbientBg(it) },
                        explanation = stringResource(R.string.si_toggle_ambient),
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.Animation,
                        title = stringResource(R.string.settings_animations),
                        subtitle = stringResource(R.string.settings_animations_desc),
                        enabled = settings.animations,
                        onToggle = { settings.updateAnimations(it) },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = stringResource(R.string.settings_startup_anim),
                        subtitle = stringResource(R.string.settings_startup_anim_desc),
                        enabled = settings.startupAnim,
                        onToggle = { settings.updateStartupAnim(it) },
                        available = settings.animations,
                        explanation = stringResource(R.string.si_toggle_startup),
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_haptics)) {
                SettingsCard {
                    val hapticsLevels = listOf(
                        HapticsLevel.OFF,
                        HapticsLevel.SUBTLE,
                        HapticsLevel.BALANCED,
                        HapticsLevel.EXPRESSIVE,
                    )
                    val currentHaptics = HapticsLevel.fromPref(settings.hapticsLevel)
                    SettingsChoiceRow(
                        icon = Icons.Rounded.Vibration,
                        title = stringResource(R.string.settings_haptics),
                        subtitle = stringResource(R.string.settings_haptics_desc),
                        labels = listOf(
                            stringResource(R.string.haptic_level_off),
                            stringResource(R.string.haptic_level_subtle),
                            stringResource(R.string.haptic_level_balanced),
                            stringResource(R.string.haptic_level_expressive),
                        ),
                        selectedIndex = hapticsLevels.indexOf(currentHaptics).coerceIn(0, 3),
                        onSelect = { settings.updateHapticsLevel(HapticsLevel.toPref(hapticsLevels[it])) },
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Rounded.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = langName,
                        onClick = onOpenLanguage,
                        showChevron = true,
                    )
                }
            }
        }
    }
}

@Composable
internal fun GalleryLayoutSettingsPage(
    settings: AppSettings,
    onBack: () -> Unit,
) {
    val gapValues = listOf(0, 2, 6)
    val gapIndex = gapValues.indexOfFirst { it == settings.gap }.let { if (it < 0) 1 else it }

    SettingsDetailScaffold(
        title = stringResource(R.string.settings_photos_albums),
        onBack = onBack,
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_section_gallery)) {
                SettingsCard {
                    SettingsChoiceRow(
                        icon = Icons.Rounded.GridView,
                        title = stringResource(R.string.settings_columns_photos),
                        subtitle = stringResource(R.string.settings_columns_photos_desc),
                        labels = listOf("2", "3", "4", "5"),
                        selectedIndex = (settings.photoColumns - 2).coerceIn(0, 3),
                        onSelect = { settings.updatePhotoColumns(it + 2) },
                    )
                    SettingsDivider()
                    SettingsChoiceRow(
                        icon = Icons.Rounded.PhotoAlbum,
                        title = stringResource(R.string.settings_columns_albums),
                        subtitle = stringResource(R.string.onboard_columns),
                        labels = listOf("2", "3", "4", "5"),
                        selectedIndex = (settings.albumColumns - 2).coerceIn(0, 3),
                        onSelect = { settings.updateAlbumColumns(it + 2) },
                    )
                    SettingsDivider()
                    SettingsChoiceRow(
                        icon = Icons.Rounded.SpaceBar,
                        title = stringResource(R.string.settings_gap),
                        subtitle = "",
                        labels = listOf(
                            stringResource(R.string.settings_gap_none),
                            stringResource(R.string.gap_small),
                            stringResource(R.string.gap_large),
                        ),
                        selectedIndex = gapIndex,
                        onSelect = { settings.updateGap(gapValues[it]) },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.RoundedCorner,
                        title = stringResource(R.string.settings_rounded),
                        subtitle = stringResource(R.string.settings_rounded_desc),
                        enabled = settings.roundedCorners,
                        onToggle = { settings.updateRoundedCorners(it) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun PlayerSettingsPage(
    settings: AppSettings,
    onBack: () -> Unit,
) {
    SettingsDetailScaffold(
        title = stringResource(R.string.settings_player),
        onBack = onBack,
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_section_playback)) {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Rounded.PlayCircle,
                        title = stringResource(R.string.settings_autoplay),
                        subtitle = stringResource(R.string.settings_autoplay_desc),
                        enabled = settings.autoplay,
                        onToggle = { settings.updateAutoplay(it) },
                    )
                    SettingsDivider()
                    SettingsChoiceRow(
                        icon = Icons.Rounded.Dock,
                        title = stringResource(R.string.settings_player_panel),
                        subtitle = stringResource(R.string.settings_player_panel_desc),
                        labels = listOf(
                            stringResource(R.string.player_panel_default),
                            stringResource(R.string.player_panel_floating),
                        ),
                        selectedIndex = if (settings.playerFloating) 1 else 0,
                        onSelect = { settings.updatePlayerFloating(it == 1) },
                    )
                    SettingsDivider()
                    SettingsChoiceRow(
                        icon = Icons.Rounded.BorderStyle,
                        title = stringResource(R.string.settings_player_border),
                        subtitle = "",
                        labels = listOf(
                            stringResource(R.string.option_none),
                            stringResource(R.string.border_theme),
                            stringResource(R.string.border_animated),
                        ),
                        selectedIndex = settings.playerBorder.coerceIn(0, 2),
                        onSelect = { settings.updatePlayerBorder(it) },
                    )
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_seek_style)) {
                SettingsCard {
                    SeekStylePicker(settings = settings)
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.settings_player_panel)) {
                SettingsCard {
                    SettingsSliderRow(
                        icon = Icons.Rounded.WidthNormal,
                        title = stringResource(R.string.player_tune_width),
                        subtitle = stringResource(R.string.player_tune_floating_note),
                        valueText = "${settings.playerWidth}%",
                        value = settings.playerWidth,
                        range = 75..100,
                        onChange = { settings.updatePlayerWidth(it) },
                        enabled = settings.playerFloating,
                    )
                    SettingsDivider()
                    SettingsSliderRow(
                        icon = Icons.Rounded.Height,
                        title = stringResource(R.string.player_tune_thickness),
                        valueText = "${settings.playerThickness}%",
                        value = settings.playerThickness,
                        range = 85..150,
                        onChange = { settings.updatePlayerThickness(it) },
                    )
                    SettingsDivider()
                    SettingsSliderRow(
                        icon = Icons.Rounded.TouchApp,
                        title = stringResource(R.string.player_tune_size),
                        valueText = "${settings.playerSize}%",
                        value = settings.playerSize,
                        range = 80..130,
                        onChange = { settings.updatePlayerSize(it) },
                    )
                    SettingsDivider()
                    SettingsSliderRow(
                        icon = Icons.Rounded.ViewColumn,
                        title = stringResource(R.string.player_tune_bottom),
                        valueText = "${settings.playerBottom} dp",
                        value = settings.playerBottom,
                        range = 0..60,
                        onChange = { settings.updatePlayerBottom(it) },
                        enabled = settings.playerFloating,
                    )
                    SettingsDivider()
                    SettingsChoiceRow(
                        icon = Icons.Rounded.Tune,
                        title = stringResource(R.string.tune_align),
                        subtitle = "",
                        labels = listOf(
                            stringResource(R.string.align_left),
                            stringResource(R.string.align_center),
                            stringResource(R.string.align_right),
                        ),
                        selectedIndex = settings.playerAlign.coerceIn(0, 2),
                        onSelect = { settings.updatePlayerAlign(it) },
                        enabled = settings.playerFloating,
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
internal fun UpdatesSettingsPage(
    settings: AppSettings,
    updater: Updater,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val statusText = when (val st = updater.state) {
        is UpdateState.Checking -> stringResource(R.string.update_status_checking)
        is UpdateState.Available -> stringResource(R.string.update_status_available, st.info.version)
        is UpdateState.UpToDate -> stringResource(R.string.update_status_up_to_date, updater.currentVersion)
        is UpdateState.Downloading -> stringResource(R.string.update_status_downloading)
        is UpdateState.Ready -> stringResource(R.string.update_status_ready)
        is UpdateState.Failed -> st.message
        is UpdateState.Idle -> stringResource(R.string.version_label, updater.currentVersion)
    }

    SettingsDetailScaffold(
        title = stringResource(R.string.settings_updates),
        onBack = onBack,
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_updates)) {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Rounded.SystemUpdate,
                        title = stringResource(R.string.settings_check_updates),
                        subtitle = statusText,
                        onClick = { scope.launch { updater.check(manual = true) } },
                        showChevron = true,
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = stringResource(R.string.settings_auto_update),
                        subtitle = stringResource(R.string.settings_auto_update_desc),
                        enabled = settings.autoUpdate,
                        onToggle = { settings.updateAutoUpdate(it) },
                    )
                }
            }
        }
    }
}
