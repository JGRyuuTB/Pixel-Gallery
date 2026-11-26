package com.GalleryYou.Pixel.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RoundedCorner
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.GalleryYou.Pixel.AppLanguages
import com.GalleryYou.Pixel.LanguageDialog
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.appVersion
import com.GalleryYou.Pixel.data.AppSettings
import com.GalleryYou.Pixel.data.findActivity
import com.GalleryYou.Pixel.ui.components.MorphPolygonShape
import com.GalleryYou.Pixel.ui.components.PolygonShape
import com.GalleryYou.Pixel.ui.theme.APP_PALETTES
import com.GalleryYou.Pixel.ui.theme.DYNAMIC_PALETTE_ID
import com.GalleryYou.Pixel.ui.theme.ThemeMode
import com.GalleryYou.Pixel.util.rememberGalleryHaptics
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

private const val ONBOARDING_PAGE_COUNT = 6

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    settings: AppSettings,
    granted: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberGalleryHaptics()
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val isLastPage = pagerState.currentPage == ONBOARDING_PAGE_COUNT - 1
    var showLanguage by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    ),
                    start = Offset.Zero,
                    end = Offset(1400f, 2200f),
                ),
            ),
    ) {
        OnboardingBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(PolygonShape(MaterialShapes.Cookie9Sided))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TextButton(
                        onClick = {
                            haptics.subtle()
                            onFinish()
                        },
                    ) {
                        Text(stringResource(R.string.ob_skip))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 1,
                verticalAlignment = Alignment.Top,
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                Box(
                    modifier = Modifier.graphicsLayer {
                        val fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                        alpha = 0.3f + 0.7f * fraction
                        val scale = 0.94f + 0.06f * fraction
                        scaleX = scale
                        scaleY = scale
                    },
                ) {
                    when (page) {
                        0 -> WelcomePage(settings = settings, onOpenLanguage = { showLanguage = true })
                        1 -> PermissionPage(
                            granted = granted,
                            onRequest = onRequestPermission,
                            onOpenAppSettings = onOpenAppSettings,
                        )
                        2 -> ThemeAndColorsPage(settings = settings)
                        3 -> LayoutPage(settings = settings)
                        4 -> PlayerSetupPage(settings = settings)
                        else -> DonePage()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val progress by animateFloatAsState(
                targetValue = (pagerState.currentPage + 1) / ONBOARDING_PAGE_COUNT.toFloat(),
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "onboardingProgress",
            )
            LinearWavyProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pagerState.currentPage > 0) {
                    FilledTonalIconButton(
                        onClick = {
                            haptics.subtle()
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.ob_back),
                        )
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }

                val canAdvance = pagerState.currentPage != 1 || granted
                Button(
                    onClick = {
                        haptics.confirm()
                        if (isLastPage) {
                            onFinish()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    enabled = canAdvance,
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text(
                        text = stringResource(if (isLastPage) R.string.ob_finish else R.string.ob_next),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isLastPage) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
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
                    context.findActivity()?.recreate()
                }
            },
            onDismiss = { showLanguage = false },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OnboardingBackdrop() {
    val transition = rememberInfiniteTransition(label = "obBackdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing), RepeatMode.Restart),
        label = "obDrift",
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 12.dp)
                .size(140.dp)
                .graphicsLayer {
                    rotationZ = drift
                    alpha = 0.08f
                }
                .clip(PolygonShape(MaterialShapes.SoftBurst))
                .background(MaterialTheme.colorScheme.primary),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 120.dp, start = 8.dp)
                .size(110.dp)
                .graphicsLayer {
                    rotationZ = -drift * 0.6f
                    alpha = 0.08f
                }
                .clip(PolygonShape(MaterialShapes.Sunny))
                .background(MaterialTheme.colorScheme.tertiary),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WelcomePage(settings: AppSettings, onOpenLanguage: () -> Unit) {
    val context = LocalContext.current
    val transition = rememberInfiniteTransition(label = "obHero")
    val morphProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "obMorph",
    )
    val morph = remember { Morph(MaterialShapes.Cookie9Sided, MaterialShapes.SoftBurst) }
    val languageName = AppLanguages.firstOrNull { it.tag == settings.language }?.nativeName
        ?: stringResource(R.string.language_system)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(124.dp)
                .clip(MorphPolygonShape(morph, morphProgress))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(54.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboard_welcome),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Text(
                text = stringResource(R.string.version_stable, appVersion(context)),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.onboard_welcome_sub),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        FilledTonalButton(onClick = onOpenLanguage) {
            Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(languageName)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PermissionPage(
    granted: Boolean,
    onRequest: () -> Unit,
    onOpenAppSettings: () -> Unit,
) {
    PageShell(
        icon = Icons.Rounded.PhotoLibrary,
        title = stringResource(R.string.onboard_perm_title),
        subtitle = stringResource(R.string.onboard_perm_sub),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (granted) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.onboard_perm_granted),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onRequest,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.onboard_perm_grant))
                    }
                    TextButton(onClick = onOpenAppSettings) {
                        Text(stringResource(R.string.onboard_open_app_settings))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeAndColorsPage(settings: AppSettings) {
    val haptics = rememberGalleryHaptics()
    PageShell(
        icon = Icons.Rounded.Palette,
        title = stringResource(R.string.onboard_colors_title),
        subtitle = stringResource(R.string.onboard_colors_sub),
    ) {
        val modes = listOf(
            ThemeMode.SYSTEM to stringResource(R.string.theme_system),
            ThemeMode.LIGHT to stringResource(R.string.theme_light),
            ThemeMode.DARK to stringResource(R.string.theme_dark),
        )
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Brightness4, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.onboard_theme_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    modes.forEachIndexed { index, (mode, label) ->
                        ToggleButton(
                            checked = settings.themeMode == mode.storageId,
                            onCheckedChange = {
                                haptics.confirm()
                                settings.updateThemeMode(mode)
                            },
                            modifier = Modifier.weight(1f),
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                modes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            colors = ToggleButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                checkedContainerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Text(stringResource(R.string.sp_color_palette), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                val quickPalettes = listOf(DYNAMIC_PALETTE_ID) + listOf("electric", "lavender", "vintagefilm", "magenta", "emerald")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    quickPalettes.forEach { id ->
                        val selected = settings.colorPalette == id
                        val swatchColor = if (id == DYNAMIC_PALETTE_ID) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            APP_PALETTES.firstOrNull { it.id == id }?.seedPrimary ?: MaterialTheme.colorScheme.primary
                        }
                        val label = if (id == DYNAMIC_PALETTE_ID) {
                            stringResource(R.string.cp_dynamic_cd)
                        } else {
                            APP_PALETTES.firstOrNull { it.id == id }?.name ?: id
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    haptics.confirm()
                                    settings.updateColorPalette(id)
                                }
                                .padding(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor)
                                    .then(
                                        if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (id == DYNAMIC_PALETTE_ID) {
                                    Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                } else if (selected) {
                                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LayoutPage(settings: AppSettings) {
    val haptics = rememberGalleryHaptics()
    PageShell(
        icon = Icons.Rounded.GridView,
        title = stringResource(R.string.onboard_layout_title),
        subtitle = stringResource(R.string.onboard_layout_sub),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.settings_columns_photos), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    val cols = listOf(2, 3, 4, 5)
                    cols.forEachIndexed { idx, c ->
                        ToggleButton(
                            checked = settings.photoColumns == c,
                            onCheckedChange = {
                                haptics.tick()
                                settings.updatePhotoColumns(c)
                            },
                            modifier = Modifier.weight(1f),
                            shapes = when (idx) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                cols.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            colors = ToggleButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                checkedContainerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text("$c")
                        }
                    }
                }

                ToggleLine(
                    icon = Icons.Rounded.RoundedCorner,
                    title = stringResource(R.string.settings_rounded),
                    subtitle = stringResource(R.string.onboard_rounded_desc),
                    checked = settings.roundedCorners,
                    onChange = { settings.updateRoundedCorners(it) },
                )
                ToggleLine(
                    icon = Icons.Rounded.Wallpaper,
                    title = stringResource(R.string.settings_ambient_bg),
                    subtitle = stringResource(R.string.settings_ambient_bg_desc),
                    checked = settings.ambientBg,
                    onChange = { settings.updateAmbientBg(it) },
                )
                ToggleLine(
                    icon = Icons.Rounded.Animation,
                    title = stringResource(R.string.settings_animations),
                    subtitle = stringResource(R.string.onboard_animations_desc),
                    checked = settings.animations,
                    onChange = { settings.updateAnimations(it) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerSetupPage(settings: AppSettings) {
    val haptics = rememberGalleryHaptics()
    PageShell(
        icon = Icons.Rounded.PlayCircle,
        title = stringResource(R.string.settings_player),
        subtitle = stringResource(R.string.onboard_player_sub),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleLine(
                    icon = Icons.Rounded.PlayCircle,
                    title = stringResource(R.string.onboard_autoplay_title),
                    subtitle = stringResource(R.string.onboard_autoplay_desc),
                    checked = settings.autoplay,
                    onChange = { settings.updateAutoplay(it) },
                )
                ToggleLine(
                    icon = Icons.Rounded.Dock,
                    title = stringResource(R.string.settings_player_panel),
                    subtitle = stringResource(R.string.settings_player_panel_desc),
                    checked = settings.playerFloating,
                    onChange = { settings.updatePlayerFloating(it) },
                )
                ToggleLine(
                    icon = Icons.Rounded.BlurOn,
                    title = stringResource(R.string.onboard_blur_title),
                    subtitle = stringResource(R.string.onboard_blur_desc),
                    checked = settings.blur > 0,
                    onChange = { settings.updateBlur(if (it) 7 else 0) },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.BorderStyle, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.onboard_border_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                val borders = listOf(
                    stringResource(R.string.onboard_border_none),
                    stringResource(R.string.border_theme),
                    stringResource(R.string.border_animated),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    borders.forEachIndexed { idx, label ->
                        ToggleButton(
                            checked = settings.playerBorder == idx,
                            onCheckedChange = {
                                haptics.confirm()
                                settings.updatePlayerBorder(idx)
                            },
                            modifier = Modifier.weight(1f),
                            shapes = when (idx) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                borders.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            colors = ToggleButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                checkedContainerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DonePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(116.dp)
                .clip(PolygonShape(MaterialShapes.Sunny))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(52.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.onboard_done_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboard_done_sub),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PageShell(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(PolygonShape(MaterialShapes.Cookie9Sided))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(30.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun ToggleLine(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val haptics = rememberGalleryHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptics.toggle(!checked)
                onChange(!checked)
            }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                haptics.toggle(it)
                onChange(it)
            },
        )
    }
}
