package com.GalleryYou.Pixel.data

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.ui.theme.DYNAMIC_PALETTE_ID
import com.GalleryYou.Pixel.ui.theme.PaletteStyle
import com.GalleryYou.Pixel.ui.theme.ThemeMode
import com.GalleryYou.Pixel.ui.theme.UI_SCALE_DEFAULT
import com.GalleryYou.Pixel.ui.theme.UI_SCALE_MAX
import com.GalleryYou.Pixel.ui.theme.UI_SCALE_MIN
import com.GalleryYou.Pixel.util.HapticsLevel

enum class NavTabLabels(val storageId: String) {
    SELECTED("selected"),
    ALWAYS("always"),
    NEVER("never");

    companion object {
        fun fromStorageId(value: String?): NavTabLabels =
            entries.firstOrNull { it.storageId == value } ?: SELECTED
    }
}

const val SEEK_STYLE_COUNT = 7

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("galeria_settings", Context.MODE_PRIVATE)

    // Grid & Gallery
    var photoColumns by mutableIntStateOf(prefs.getInt("photoColumns", 3).coerceIn(2, 5))
        private set
    var albumColumns by mutableIntStateOf(prefs.getInt("albumColumns", 2).coerceIn(2, 5))
        private set
    var gap by mutableIntStateOf(prefs.getInt("gap", 2))
        private set
    var roundedCorners by mutableStateOf(prefs.getBoolean("roundedCorners", true))
        private set

    // Koda Expressive Theme & Colors
    var themeMode by mutableIntStateOf(prefs.getInt("themeMode", ThemeMode.SYSTEM.storageId))
        private set
    var colorPalette by mutableStateOf(
        prefs.getString("colorPalette", null)
            ?: if (prefs.getBoolean("dynamicColor", true)) DYNAMIC_PALETTE_ID else "electric"
    )
        private set
    var paletteStyle by mutableStateOf(
        PaletteStyle.fromStorageId(prefs.getString("paletteStyle", PaletteStyle.TONAL_SPOT.storageId))
    )
        private set
    var amoledDark by mutableStateOf(prefs.getBoolean("amoledDark", false))
        private set
    var uiScale by mutableFloatStateOf(
        prefs.getFloat("uiScale", UI_SCALE_DEFAULT).coerceIn(UI_SCALE_MIN, UI_SCALE_MAX)
    )
        private set

    // Expressive Motion, Ambient, & Haptics
    var animations by mutableStateOf(prefs.getBoolean("animations", true))
        private set
    var startupAnim by mutableStateOf(prefs.getBoolean("startupAnim", false))
        private set
    var ambientBg by mutableStateOf(prefs.getBoolean("ambientBg", false))
        private set
    var hapticsLevel by mutableStateOf(prefs.getString("hapticsLevel", HapticsLevel.DEFAULT) ?: HapticsLevel.DEFAULT)
        private set

    // Koda Expressive Navigation & Chrome
    var nonExpressiveNavigationBar by mutableStateOf(prefs.getBoolean("nonExpressiveNavBar", false))
        private set
    var barHidesOnScroll by mutableStateOf(prefs.getBoolean("barHidesOnScroll", true))
        private set
    var tabLabels by mutableStateOf(
        NavTabLabels.fromStorageId(prefs.getString("tabLabels", NavTabLabels.SELECTED.storageId))
    )
        private set
    var showGreeting by mutableStateOf(prefs.getBoolean("showGreeting", true))
        private set

    // Glass backdrop (used in Viewer chrome / optional glass panels)
    var blur by mutableIntStateOf(prefs.getInt("blur", 7).coerceIn(0, 7))
        private set
    var liquidGlass by mutableStateOf(prefs.getBoolean("liquidGlass", true))
        private set

    // Video Player
    var autoplay by mutableStateOf(prefs.getBoolean("autoplay", true))
        private set
    var playerBorder by mutableIntStateOf(prefs.getInt("playerBorder", 0).coerceIn(0, 2))
        private set
    var seekStyle by mutableIntStateOf(prefs.getInt("seekStyle", 1).coerceIn(0, SEEK_STYLE_COUNT - 1))
        private set
    var playerFloating by mutableStateOf(prefs.getBoolean("playerFloating", true))
        private set
    var playerWidth by mutableIntStateOf(prefs.getInt("playerWidth", 100).coerceIn(75, 100))
        private set
    var playerThickness by mutableIntStateOf(prefs.getInt("playerThickness", 100).coerceIn(85, 150))
        private set
    var playerSize by mutableIntStateOf(prefs.getInt("playerSize", 100).coerceIn(80, 130))
        private set
    var playerBottom by mutableIntStateOf(prefs.getInt("playerBottom", 10).coerceIn(0, 60))
        private set
    var playerAlign by mutableIntStateOf(prefs.getInt("playerAlign", 1).coerceIn(0, 2))
        private set

    // System, App Icon, Language, Updates, Onboarding
    var appIcon by mutableIntStateOf(
        prefs.getInt("appIcon", if (prefs.getBoolean("materialYouIcon", false)) 1 else 0)
            .coerceIn(0, AppIconOptions.lastIndex)
    )
        private set
    var language by mutableStateOf(prefs.getString("language", "") ?: "")
        private set
    var autoUpdate by mutableStateOf(prefs.getBoolean("autoUpdate", true))
        private set
    val lastUpdateCheck: Long get() = prefs.getLong("lastUpdateCheck", 0L)
    var onboardingDone by mutableStateOf(prefs.getBoolean("onboardingDone", false))
        private set

    fun updatePhotoColumns(value: Int) {
        photoColumns = value.coerceIn(2, 5)
        prefs.edit().putInt("photoColumns", photoColumns).apply()
    }

    fun updateAlbumColumns(value: Int) {
        albumColumns = value.coerceIn(2, 5)
        prefs.edit().putInt("albumColumns", albumColumns).apply()
    }

    fun updateGap(value: Int) {
        gap = value
        prefs.edit().putInt("gap", value).apply()
    }

    fun updateRoundedCorners(value: Boolean) {
        roundedCorners = value
        prefs.edit().putBoolean("roundedCorners", value).apply()
    }

    fun updateThemeMode(value: Int) {
        themeMode = value.coerceIn(0, 2)
        prefs.edit().putInt("themeMode", themeMode).apply()
    }

    fun updateThemeMode(mode: ThemeMode) = updateThemeMode(mode.storageId)

    fun updateColorPalette(value: String) {
        colorPalette = value
        prefs.edit().putString("colorPalette", value).apply()
    }

    fun updatePaletteStyle(value: PaletteStyle) {
        paletteStyle = value
        prefs.edit().putString("paletteStyle", value.storageId).apply()
    }

    fun updateAmoledDark(value: Boolean) {
        amoledDark = value
        prefs.edit().putBoolean("amoledDark", value).apply()
    }

    fun updateUiScale(value: Float) {
        uiScale = value.coerceIn(UI_SCALE_MIN, UI_SCALE_MAX)
        prefs.edit().putFloat("uiScale", uiScale).apply()
    }

    fun updateAnimations(value: Boolean) {
        animations = value
        prefs.edit().putBoolean("animations", value).apply()
    }

    fun updateStartupAnim(value: Boolean) {
        startupAnim = value
        prefs.edit().putBoolean("startupAnim", value).apply()
    }

    fun updateAmbientBg(value: Boolean) {
        ambientBg = value
        prefs.edit().putBoolean("ambientBg", value).apply()
    }

    fun updateHapticsLevel(value: String) {
        hapticsLevel = value
        prefs.edit().putString("hapticsLevel", value).apply()
    }

    fun updateNonExpressiveNavigationBar(value: Boolean) {
        nonExpressiveNavigationBar = value
        prefs.edit().putBoolean("nonExpressiveNavBar", value).apply()
    }

    fun updateBarHidesOnScroll(value: Boolean) {
        barHidesOnScroll = value
        prefs.edit().putBoolean("barHidesOnScroll", value).apply()
    }

    fun updateTabLabels(value: NavTabLabels) {
        tabLabels = value
        prefs.edit().putString("tabLabels", value.storageId).apply()
    }

    fun updateShowGreeting(value: Boolean) {
        showGreeting = value
        prefs.edit().putBoolean("showGreeting", value).apply()
    }

    fun updateBlur(value: Int) {
        blur = value.coerceIn(0, 7)
        prefs.edit().putInt("blur", blur).apply()
    }

    fun updateLiquidGlass(value: Boolean) {
        liquidGlass = value
        prefs.edit().putBoolean("liquidGlass", value).apply()
    }

    fun updateAutoplay(value: Boolean) {
        autoplay = value
        prefs.edit().putBoolean("autoplay", value).apply()
    }

    fun updatePlayerBorder(value: Int) {
        playerBorder = value.coerceIn(0, 2)
        prefs.edit().putInt("playerBorder", playerBorder).apply()
    }

    fun updateSeekStyle(value: Int) {
        seekStyle = value.coerceIn(0, SEEK_STYLE_COUNT - 1)
        prefs.edit().putInt("seekStyle", seekStyle).apply()
    }

    fun updatePlayerFloating(value: Boolean) {
        playerFloating = value
        prefs.edit().putBoolean("playerFloating", value).apply()
    }

    fun updatePlayerWidth(v: Int) {
        playerWidth = v.coerceIn(75, 100)
        prefs.edit().putInt("playerWidth", playerWidth).apply()
    }

    fun updatePlayerThickness(v: Int) {
        playerThickness = v.coerceIn(85, 150)
        prefs.edit().putInt("playerThickness", playerThickness).apply()
    }

    fun updatePlayerSize(v: Int) {
        playerSize = v.coerceIn(80, 130)
        prefs.edit().putInt("playerSize", playerSize).apply()
    }

    fun updatePlayerBottom(v: Int) {
        playerBottom = v.coerceIn(0, 60)
        prefs.edit().putInt("playerBottom", playerBottom).apply()
    }

    fun updatePlayerAlign(v: Int) {
        playerAlign = v.coerceIn(0, 2)
        prefs.edit().putInt("playerAlign", playerAlign).apply()
    }

    fun resetPlayerTuning() {
        updatePlayerWidth(100)
        updatePlayerThickness(100)
        updatePlayerSize(100)
        updatePlayerBottom(10)
        updatePlayerAlign(1)
    }

    fun updateAppIcon(value: Int) {
        appIcon = value.coerceIn(0, AppIconOptions.lastIndex)
        prefs.edit().putInt("appIcon", appIcon).apply()
    }

    fun updateLanguage(tag: String) {
        language = tag
        prefs.edit().putString("language", tag).apply()
    }

    fun updateAutoUpdate(value: Boolean) {
        autoUpdate = value
        prefs.edit().putBoolean("autoUpdate", value).apply()
    }

    fun markUpdateChecked() {
        prefs.edit().putLong("lastUpdateCheck", System.currentTimeMillis()).apply()
    }

    fun completeOnboarding() {
        onboardingDone = true
        prefs.edit().putBoolean("onboardingDone", true).apply()
    }
}

fun applyAppIcon(context: Context, index: Int) {
    val pm = context.packageManager
    val chosen = index.coerceIn(0, AppIconOptions.lastIndex)
    fun component(i: Int) = ComponentName(context.packageName, "com.GalleryYou.Pixel.${AppIconOptions[i].alias}")
    fun set(i: Int, state: Int) {
        val c = component(i)
        if (pm.getComponentEnabledSetting(c) != state) {
            pm.setComponentEnabledSetting(c, state, PackageManager.DONT_KILL_APP)
        }
    }
    set(chosen, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    AppIconOptions.indices.filter { it != chosen }.forEach { set(it, PackageManager.COMPONENT_ENABLED_STATE_DISABLED) }
}

class AppIconOption(val alias: String, @StringRes val nameRes: Int, @DrawableRes val previewRes: Int)

val AppIconOptions = listOf(
    AppIconOption("AliasDefault", R.string.icon_default, R.mipmap.ic_launcher_default),
    AppIconOption("AliasMaterialYou", R.string.icon_material_you, R.mipmap.ic_launcher_you),
    AppIconOption("AliasOutline", R.string.icon_outline, R.mipmap.ic_launcher_outline),
    AppIconOption("AliasPastel", R.string.icon_pastel, R.mipmap.ic_launcher_pastel),
    AppIconOption("AliasGlass", R.string.icon_glass, R.mipmap.ic_launcher_glass),
    AppIconOption("AliasMono", R.string.icon_mono, R.mipmap.ic_launcher_mono),
    AppIconOption("AliasSunset", R.string.icon_sunset, R.mipmap.ic_launcher_sunset),
    AppIconOption("AliasSakura", R.string.icon_sakura, R.mipmap.ic_launcher_sakura),
    AppIconOption("AliasNeon", R.string.icon_neon, R.mipmap.ic_launcher_neon),
    AppIconOption("AliasFlat", R.string.icon_flat, R.mipmap.ic_launcher_flat),
    AppIconOption("AliasPixel", R.string.icon_pixel, R.mipmap.ic_launcher_pixel),
    AppIconOption("AliasAnime", R.string.icon_anime, R.mipmap.ic_launcher_anime),
)

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
