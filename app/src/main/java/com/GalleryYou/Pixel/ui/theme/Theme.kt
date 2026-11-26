package com.GalleryYou.Pixel.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlin.math.abs

const val UI_SCALE_MIN = 0.85f
const val UI_SCALE_DEFAULT = 1f
const val UI_SCALE_MAX = 1.15f
val UI_SCALE_STEPS: List<Float> = listOf(0.85f, 0.90f, 0.95f, 1.00f, 1.05f, 1.10f, 1.15f)

fun nearestUiScaleStep(value: Float): Float =
    UI_SCALE_STEPS.minByOrNull { abs(it - value) } ?: UI_SCALE_DEFAULT

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryBlue,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = OnPrimaryBlue,
    secondary = SecondaryPurple,
    secondaryContainer = SecondaryPurpleContainer,
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
)

fun ColorScheme.withTintedPages(dark: Boolean): ColorScheme {
    val page = surfaceContainer
    val card = surfaceBright
    val (high, highest) = if (dark) {
        lerp(card, onSurface, 0.06f) to lerp(card, onSurface, 0.12f)
    } else {
        lerp(surfaceContainerHigh, onSurface, 0.05f) to lerp(surfaceContainerHighest, onSurface, 0.07f)
    }
    return copy(
        background = page,
        surface = page,
        surfaceContainerLow = lerp(page, card, 0.5f),
        surfaceContainer = card,
        surfaceContainerHigh = high,
        surfaceContainerHighest = highest,
    )
}

internal fun ColorScheme.toAmoled(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF101010),
    surfaceContainerHigh = Color(0xFF181818),
    surfaceContainerHighest = Color(0xFF202020),
)

internal val ColorScheme.onMediaScrim: Color
    get() = if (onSurface.luminance() >= inverseOnSurface.luminance()) onSurface else inverseOnSurface

private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

fun galleryColorScheme(
    context: Context,
    darkTheme: Boolean,
    colorPalette: String,
    amoledDark: Boolean,
    paletteStyle: PaletteStyle = PaletteStyle.TONAL_SPOT,
): ColorScheme {
    val useDynamic = colorPalette == DYNAMIC_PALETTE_ID
    val baseColorScheme = when {
        useDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> expressiveLightColorScheme()
    }
    val palettedScheme = if (useDynamic) {
        baseColorScheme
    } else {
        findPalette(colorPalette)?.let { buildPaletteColorScheme(it, darkTheme, paletteStyle) }
            ?: baseColorScheme
    }
    return if (darkTheme && amoledDark) palettedScheme.toAmoled() else palettedScheme
}

@Composable
fun isThemeDark(mode: ThemeMode): Boolean {
    val system = isSystemInDarkTheme()
    return when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> system
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PixelGalleryTheme(
    darkTheme: Boolean = true,
    colorPalette: String = DYNAMIC_PALETTE_ID,
    amoledDark: Boolean = false,
    uiScale: Float = UI_SCALE_DEFAULT,
    paletteStyle: PaletteStyle = PaletteStyle.TONAL_SPOT,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val paletteScheme = remember(context, configuration, darkTheme, colorPalette, amoledDark, paletteStyle) {
        galleryColorScheme(context, darkTheme, colorPalette, amoledDark, paletteStyle)
    }
    val targetScheme = remember(paletteScheme, darkTheme, amoledDark) {
        if (darkTheme && amoledDark) paletteScheme else paletteScheme.withTintedPages(darkTheme)
    }
    val colorScheme = rememberFadedColorScheme(targetScheme, fadeKey = colorPalette)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isStatusBarContrastEnforced = false
                window.isNavigationBarContrastEnforced = false
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = ExpressiveShapes,
        typography = GalleryTypography,
    ) {
        CompositionLocalProvider(
            LocalAppColorScheme provides colorScheme,
            LocalContentColor provides colorScheme.onBackground,
        ) {
            ScaledDensity(uiScale, content)
        }
    }
}

val LocalAppColorScheme = staticCompositionLocalOf<ColorScheme?> { null }

@Composable
fun appColorScheme(): ColorScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme

private const val PALETTE_FADE_MS = 320

@Composable
private fun rememberFadedColorScheme(target: ColorScheme, fadeKey: String): ColorScheme {
    var from by remember { mutableStateOf(target) }
    var to by remember { mutableStateOf(target) }
    var lastKey by remember { mutableStateOf(fadeKey) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(target) {
        if (target === to) return@LaunchedEffect
        val paletteChanged = fadeKey != lastKey
        lastKey = fadeKey
        if (!paletteChanged) {
            from = target
            to = target
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        from = lerpColorScheme(from, to, progress.value)
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, tween(PALETTE_FADE_MS))
    }

    val fraction = progress.value
    return if (fraction >= 1f) to else lerpColorScheme(from, to, fraction)
}

private fun lerpColorScheme(start: ColorScheme, stop: ColorScheme, fraction: Float): ColorScheme {
    if (fraction <= 0f) return start
    if (fraction >= 1f) return stop
    fun mix(a: Color, b: Color) = lerp(a, b, fraction)
    return stop.copy(
        primary = mix(start.primary, stop.primary),
        onPrimary = mix(start.onPrimary, stop.onPrimary),
        primaryContainer = mix(start.primaryContainer, stop.primaryContainer),
        onPrimaryContainer = mix(start.onPrimaryContainer, stop.onPrimaryContainer),
        inversePrimary = mix(start.inversePrimary, stop.inversePrimary),
        secondary = mix(start.secondary, stop.secondary),
        onSecondary = mix(start.onSecondary, stop.onSecondary),
        secondaryContainer = mix(start.secondaryContainer, stop.secondaryContainer),
        onSecondaryContainer = mix(start.onSecondaryContainer, stop.onSecondaryContainer),
        tertiary = mix(start.tertiary, stop.tertiary),
        onTertiary = mix(start.onTertiary, stop.onTertiary),
        tertiaryContainer = mix(start.tertiaryContainer, stop.tertiaryContainer),
        onTertiaryContainer = mix(start.onTertiaryContainer, stop.onTertiaryContainer),
        background = mix(start.background, stop.background),
        onBackground = mix(start.onBackground, stop.onBackground),
        surface = mix(start.surface, stop.surface),
        onSurface = mix(start.onSurface, stop.onSurface),
        surfaceVariant = mix(start.surfaceVariant, stop.surfaceVariant),
        onSurfaceVariant = mix(start.onSurfaceVariant, stop.onSurfaceVariant),
        surfaceTint = mix(start.surfaceTint, stop.surfaceTint),
        inverseSurface = mix(start.inverseSurface, stop.inverseSurface),
        inverseOnSurface = mix(start.inverseOnSurface, stop.inverseOnSurface),
        outline = mix(start.outline, stop.outline),
        outlineVariant = mix(start.outlineVariant, stop.outlineVariant),
        surfaceBright = mix(start.surfaceBright, stop.surfaceBright),
        surfaceDim = mix(start.surfaceDim, stop.surfaceDim),
        surfaceContainer = mix(start.surfaceContainer, stop.surfaceContainer),
        surfaceContainerHigh = mix(start.surfaceContainerHigh, stop.surfaceContainerHigh),
        surfaceContainerHighest = mix(start.surfaceContainerHighest, stop.surfaceContainerHighest),
        surfaceContainerLow = mix(start.surfaceContainerLow, stop.surfaceContainerLow),
        surfaceContainerLowest = mix(start.surfaceContainerLowest, stop.surfaceContainerLowest),
    )
}

@Composable
private fun ScaledDensity(scale: Float, content: @Composable () -> Unit) {
    if (scale == UI_SCALE_DEFAULT) {
        content()
        return
    }
    val current = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = current.density * scale,
            fontScale = current.fontScale,
        ),
        content = content,
    )
}
