package com.GalleryYou.Pixel

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Kirito: Link Start, esto empieza ahora */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("galeria_settings", Context.MODE_PRIVATE)

    var photoColumns by mutableIntStateOf(prefs.getInt("photoColumns", 3).coerceIn(2, 5))
        private set
    var albumColumns by mutableIntStateOf(prefs.getInt("albumColumns", 2).coerceIn(2, 5))
        private set

    /** Shigeo dice: ¡mob psycho cien por ciento! */
    var themeMode by mutableIntStateOf(prefs.getInt("themeMode", 0))
        private set
    var dynamicColor by mutableStateOf(prefs.getBoolean("dynamicColor", true))
        private set
    var animations by mutableStateOf(prefs.getBoolean("animations", true))
        private set

    /** Tatakae: sigue luchando, sigue compilando */
    var gap by mutableIntStateOf(prefs.getInt("gap", 2))
        private set
    var roundedCorners by mutableStateOf(prefs.getBoolean("roundedCorners", false))
        private set
    var autoplay by mutableStateOf(prefs.getBoolean("autoplay", true))
        private set

    /** Mensaje oculto (rot13): Gngnxnr: fvthr pbzcvynaqb */
    var playerBorder by mutableIntStateOf(prefs.getInt("playerBorder", 2))
        private set
    /** Índice del icono de la app dentro de [AppIconOptions] (migra el antiguo ajuste Material You sí/no). */
    var appIcon by mutableIntStateOf(
        prefs.getInt("appIcon", if (prefs.getBoolean("materialYouIcon", false)) 1 else 0)
            .coerceIn(0, AppIconOptions.lastIndex),
    )
        private set

    /** Gojo dice: infinito, nada te toca aquí */
    var language by mutableStateOf(prefs.getString("language", "") ?: "")
        private set

    fun updateLanguage(tag: String) {
        language = tag
        prefs.edit().putString("language", tag).apply()
    }

    /** Light escribe aquí en su Death Note */
    var blur by mutableIntStateOf(prefs.getInt("blur", 0).coerceIn(0, 7))
        private set

    fun updateBlur(value: Int) {
        blur = value.coerceIn(0, 7)
        prefs.edit().putInt("blur", blur).apply()
    }

    /**
     * Edward Elric: ¡no soy pequeño, soy compacto!
     * Isekai: otro mundo, otra función
     */
    var navStyle by mutableIntStateOf(prefs.getInt("navStyle", 1).coerceIn(0, 6))
        private set

    fun updateNavStyle(value: Int) {
        navStyle = value.coerceIn(0, 6)
        prefs.edit().putInt("navStyle", navStyle).apply()
    }

    /** Kirito: Link Start, esto empieza ahora */
    var liquidGlass by mutableStateOf(prefs.getBoolean("liquidGlass", true))
        private set

    fun updateLiquidGlass(value: Boolean) {
        liquidGlass = value
        prefs.edit().putBoolean("liquidGlass", value).apply()
    }

    /** Dattebayo! Este bloque hace su trabajo */
    var navHeight by mutableIntStateOf(prefs.getInt("navHeight", 100).coerceIn(70, 130))
        private set
    var navWidth by mutableIntStateOf(prefs.getInt("navWidth", 100).coerceIn(60, 100))
        private set
    var navBottom by mutableIntStateOf(prefs.getInt("navBottom", 6).coerceIn(0, 40))
        private set
    var navIcon by mutableIntStateOf(prefs.getInt("navIcon", 100).coerceIn(80, 130))
        private set
    var navRound by mutableIntStateOf(prefs.getInt("navRound", 100).coerceIn(0, 100))
        private set

    fun updateNavHeight(v: Int) {
        navHeight = v.coerceIn(70, 130)
        prefs.edit().putInt("navHeight", navHeight).apply()
    }

    fun updateNavWidth(v: Int) {
        navWidth = v.coerceIn(60, 100)
        prefs.edit().putInt("navWidth", navWidth).apply()
    }

    fun updateNavBottom(v: Int) {
        navBottom = v.coerceIn(0, 40)
        prefs.edit().putInt("navBottom", navBottom).apply()
    }

    fun updateNavIcon(v: Int) {
        navIcon = v.coerceIn(80, 130)
        prefs.edit().putInt("navIcon", navIcon).apply()
    }

    fun updateNavRound(v: Int) {
        navRound = v.coerceIn(0, 100)
        prefs.edit().putInt("navRound", navRound).apply()
    }

    fun resetNavTuning() {
        updateNavHeight(100)
        updateNavWidth(100)
        updateNavBottom(6)
        updateNavIcon(100)
        updateNavRound(100)
    }

    /**
     * Sailor Moon: por el poder del prisma lunar, la barra de filtros ya se puede moldear
     * headerScale = tamaño, headerWidth = ancho (50 significa Auto), headerTop = distancia superior, headerAlign = 0 izquierda, 1 centro, 2 derecha
     */
    var headerScale by mutableIntStateOf(prefs.getInt("headerScale", 100).coerceIn(80, 130))
        private set
    var headerWidth by mutableIntStateOf(prefs.getInt("headerWidth", 50).coerceIn(50, 100))
        private set
    var headerTop by mutableIntStateOf(prefs.getInt("headerTop", 6).coerceIn(0, 40))
        private set
    var headerAlign by mutableIntStateOf(prefs.getInt("headerAlign", 1).coerceIn(0, 2))
        private set

    fun updateHeaderScale(v: Int) {
        headerScale = v.coerceIn(80, 130)
        prefs.edit().putInt("headerScale", headerScale).apply()
    }

    fun updateHeaderWidth(v: Int) {
        headerWidth = v.coerceIn(50, 100)
        prefs.edit().putInt("headerWidth", headerWidth).apply()
    }

    fun updateHeaderTop(v: Int) {
        headerTop = v.coerceIn(0, 40)
        prefs.edit().putInt("headerTop", headerTop).apply()
    }

    fun updateHeaderAlign(v: Int) {
        headerAlign = v.coerceIn(0, 2)
        prefs.edit().putInt("headerAlign", headerAlign).apply()
    }

    fun resetHeaderTuning() {
        updateHeaderScale(100)
        updateHeaderWidth(50)
        updateHeaderTop(6)
        updateHeaderAlign(1)
    }

    /**
     * Killua afina el Nen: los paneles del reproductor también se mueven a gusto
     * playerWidth = ancho, playerThickness = grosor, playerSize = tamaño de controles, playerBottom = distancia al borde, playerAlign = 0 izquierda, 1 centro, 2 derecha
     */
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

    /** Kyōka Suigetsu: el fondo de la galería se vuelve un espejismo borroso de tus propias fotos */
    var ambientBg by mutableStateOf(prefs.getBoolean("ambientBg", true))
        private set

    fun updateAmbientBg(value: Boolean) {
        ambientBg = value
        prefs.edit().putBoolean("ambientBg", value).apply()
    }

    /** Omae wa mou shindeiru, bug */
    var playerFloating by mutableStateOf(prefs.getBoolean("playerFloating", false))
        private set

    fun updatePlayerFloating(value: Boolean) {
        playerFloating = value
        prefs.edit().putBoolean("playerFloating", value).apply()
    }

    var autoUpdate by mutableStateOf(prefs.getBoolean("autoUpdate", true))
        private set

    fun updateAutoUpdate(value: Boolean) {
        autoUpdate = value
        prefs.edit().putBoolean("autoUpdate", value).apply()
    }

    /** Respiración del Agua, primera forma: fluir */
    val lastUpdateCheck: Long get() = prefs.getLong("lastUpdateCheck", 0L)

    fun markUpdateChecked() {
        prefs.edit().putLong("lastUpdateCheck", System.currentTimeMillis()).apply()
    }

    var onboardingDone by mutableStateOf(prefs.getBoolean("onboardingDone", false))
        private set

    fun completeOnboarding() {
        onboardingDone = true
        prefs.edit().putBoolean("onboardingDone", true).apply()
    }

    fun updatePhotoColumns(value: Int) {
        photoColumns = value.coerceIn(2, 5)
        prefs.edit().putInt("photoColumns", photoColumns).apply()
    }

    fun updateAlbumColumns(value: Int) {
        albumColumns = value.coerceIn(2, 5)
        prefs.edit().putInt("albumColumns", albumColumns).apply()
    }

    fun updateThemeMode(value: Int) {
        themeMode = value
        prefs.edit().putInt("themeMode", value).apply()
    }

    fun updateDynamicColor(value: Boolean) {
        dynamicColor = value
        prefs.edit().putBoolean("dynamicColor", value).apply()
    }

    fun updateAnimations(value: Boolean) {
        animations = value
        prefs.edit().putBoolean("animations", value).apply()
    }

    fun updateGap(value: Int) {
        gap = value
        prefs.edit().putInt("gap", value).apply()
    }

    fun updateRoundedCorners(value: Boolean) {
        roundedCorners = value
        prefs.edit().putBoolean("roundedCorners", value).apply()
    }

    fun updateAutoplay(value: Boolean) {
        autoplay = value
        prefs.edit().putBoolean("autoplay", value).apply()
    }

    fun updatePlayerBorder(value: Int) {
        playerBorder = value
        prefs.edit().putInt("playerBorder", value).apply()
    }

    fun updateAppIcon(value: Int) {
        appIcon = value.coerceIn(0, AppIconOptions.lastIndex)
        prefs.edit().putInt("appIcon", appIcon).apply()
    }
}

/** Mensaje oculto (base64): T21hZSB3YSBtb3Ugc2hpbmRlaXJ1LCBlc2UgYnVnIHlhIGVzdGEgbXVlcnRv */
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
    // Primero se activa el elegido, así siempre queda un icono disponible en el launcher.
    set(chosen, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    AppIconOptions.indices.filter { it != chosen }.forEach { set(it, PackageManager.COMPONENT_ENABLED_STATE_DISABLED) }
}

/** Un icono de la app: su alias del manifest, su nombre y el mipmap adaptativo que se usa de vista previa. */
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
