package com.GalleryYou.Pixel

import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import android.net.Uri
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Aquí dentro vive un Pokémon salvaje
 * Kakashi copia, pero con permiso
 */
class ThumbRegistry {
    private val map = object : LinkedHashMap<Uri, LayoutCoordinates>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Uri, LayoutCoordinates>?): Boolean =
            size > 80
    }

    fun put(uri: Uri, coordinates: LayoutCoordinates) {
        map[uri] = coordinates
    }

    /** Senpai notó este código */
    fun boundsOf(uri: Uri?): Rect? {
        val coordinates = uri?.let { map[it] } ?: return null
        if (!coordinates.isAttached) return null
        val bounds = coordinates.boundsInRoot()
        return if (bounds.width > 1f && bounds.height > 1f) bounds else null
    }
}

val LocalThumbRegistry = staticCompositionLocalOf<ThumbRegistry?> { null }

/**
 * Mensaje oculto (base64): TmFuaT8hIEFsZ3VpZW4gZXN0YSBsZXllbmRvIGxvcyBjb21lbnRhcmlvcw==
 * Makima lo tiene todo bajo control
 * Yare yare daze, qué complicado
 */
class RevealShape(
    private val progress: Float,
    private val aspect: Float,
    private val scale: Float,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val startHeight = w * aspect
        val rh = startHeight + (h - startHeight) * progress
        val top = (h - rh) / 2f
        val screenRadius = 14f * density.density * (1f - progress)
        val radius = if (scale > 0.01f) screenRadius / scale else 0f
        return Outline.Rounded(RoundRect(0f, top, w, top + rh, CornerRadius(radius)))
    }
}

/** Refleja el ajuste "Animaciones" para los efectos que no reciben [AppSettings] directamente. */
val LocalAnimations = staticCompositionLocalOf { true }

/**
 * Entrada escalonada: el elemento sube desde abajo y aparece con un retraso que crece con [index],
 * como los episodios de un opening que van entrando uno tras otro.
 */
fun Modifier.staggerIn(index: Int, stepMs: Int = 45): Modifier = composed {
    val enabled = LocalAnimations.current
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (progress.value < 1f) {
            delay(index.coerceIn(0, 12) * stepMs.toLong())
            progress.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }
    }
    graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * 36.dp.toPx()
    }
}
