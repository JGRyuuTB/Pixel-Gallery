package com.GalleryYou.Pixel.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.util.LruCache
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.SuccessResult
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.data.LocalGalleryLoader
import com.GalleryYou.Pixel.data.buildThumbRequest
import com.GalleryYou.Pixel.data.thumbRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val ambientColorCache = LruCache<Uri, Int>(256)

private suspend fun averageColorOf(context: Context, loader: ImageLoader, uri: Uri): Color? {
    ambientColorCache.get(uri)?.let { return Color(it) }
    return withContext(Dispatchers.Default) {
        try {
            val request = buildThumbRequest(context, uri, 32).newBuilder()
                .allowHardware(false)
                .crossfade(false)
                .build()
            val raw = ((loader.execute(request) as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
                ?: return@withContext null
            val small = Bitmap.createScaledBitmap(raw, 8, 8, true)
            val px = IntArray(64)
            small.getPixels(px, 0, 8, 0, 0, 8, 8)
            var r = 0L
            var g = 0L
            var b = 0L
            for (p in px) {
                r += (p shr 16) and 0xFF
                g += (p shr 8) and 0xFF
                b += p and 0xFF
            }
            if (small !== raw) small.recycle()
            val avg = android.graphics.Color.rgb((r / 64).toInt(), (g / 64).toInt(), (b / 64).toInt())
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(avg, hsv)
            hsv[1] = (hsv[1] * 1.35f + 0.08f).coerceIn(0f, 1f)
            hsv[2] = hsv[2].coerceIn(0.45f, 0.95f)
            val tuned = android.graphics.Color.HSVToColor(hsv)
            ambientColorCache.put(uri, tuned)
            Color(tuned)
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Kyoka Suigetsu: paints a soft multi-point aurora sampled from the photos currently
 * visible on screen, layered with a subtle blurred frame on Android 12+.
 */
@Composable
fun AmbientBackground(uris: List<Uri>, modifier: Modifier = Modifier) {
    if (uris.isEmpty()) return
    val context = LocalContext.current
    val loader = LocalGalleryLoader.current
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val colors by produceState(initialValue = emptyList<Color>(), uris) {
        value = uris.mapNotNull { averageColorOf(context, loader, it) }
    }
    val c0 by animateColorAsState(
        targetValue = colors.getOrNull(0) ?: Color.Transparent,
        animationSpec = tween(900),
        label = "ambientA",
    )
    val c1 by animateColorAsState(
        targetValue = colors.getOrNull(1) ?: colors.getOrNull(0) ?: Color.Transparent,
        animationSpec = tween(900),
        label = "ambientB",
    )
    val c2 by animateColorAsState(
        targetValue = colors.getOrNull(2) ?: colors.getOrNull(0) ?: Color.Transparent,
        animationSpec = tween(900),
        label = "ambientC",
    )
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strength = if (dark) 0.55f else 0.38f
            fun glow(c: Color, cx: Float, cy: Float, rad: Float) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(c.copy(alpha = c.alpha * strength), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = rad,
                    ),
                    radius = rad,
                    center = Offset(cx, cy),
                )
            }
            glow(c0, w * 0.10f, h * 0.10f, w * 1.05f)
            glow(c1, w * 0.95f, h * 0.42f, w * 0.95f)
            glow(c2, w * 0.15f, h * 0.88f, w * 1.05f)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Crossfade(
                targetState = uris[0],
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.42f },
                animationSpec = tween(600),
                label = "gridAmbient",
            ) { u ->
                AsyncImage(
                    model = thumbRequest(u, 192),
                    imageLoader = loader,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.4f
                            scaleY = 1.4f
                        }
                        .blur(48.dp, BlurredEdgeTreatment.Unbounded),
                )
            }
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        scheme.background.copy(alpha = 0.65f),
                        scheme.background.copy(alpha = 0.35f),
                        scheme.background.copy(alpha = 0.68f),
                    ),
                ),
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyState(
    padding: PaddingValues,
    text: String? = null,
    icon: ImageVector = Icons.Rounded.PhotoLibrary,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(PolygonShape(MaterialShapes.Cookie9Sided))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = text ?: stringResource(R.string.empty_media),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PermissionScreen(onRequest: () -> Unit, onSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(PolygonShape(MaterialShapes.SoftBurst))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(46.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.permission_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.permission_body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onRequest,
            shape = RoundedCornerShape(20.dp),
        ) {
            Text(stringResource(R.string.permission_allow))
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSettings) {
            Text(stringResource(R.string.action_open_settings))
        }
    }
}
