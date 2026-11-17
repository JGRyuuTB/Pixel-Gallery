package com.GalleryYou.Pixel

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Mob Psycho 100: el poder psíquico en movimiento.
 * Detecta fotos animadas de cualquier formato (GIF, WebP, AVIF, APNG) leyendo solo la cabecera del archivo.
 */
object AnimatedImages {
    private val cache = ConcurrentHashMap<String, Boolean>()

    private fun candidate(mime: String): Boolean {
        val m = mime.lowercase()
        return m == "image/webp" || m == "image/avif" || m == "image/png" || m == "image/apng" ||
            m == "image/heif" || m == "image/heic"
    }

    /** Respuesta inmediata cuando no hace falta abrir el archivo (null = hay que mirar la cabecera) */
    fun known(m: MediaFile): Boolean? {
        if (m.isVideo) return false
        if (m.mime.equals("image/gif", ignoreCase = true)) return true
        if (!candidate(m.mime)) return false
        return cache[m.uri.toString()]
    }

    suspend fun check(context: Context, m: MediaFile): Boolean {
        known(m)?.let { return it }
        val key = m.uri.toString()
        val result = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(m.uri)?.use { s ->
                    val buf = ByteArray(128)
                    var n = 0
                    while (n < buf.size) {
                        val r = s.read(buf, n, buf.size - n)
                        if (r <= 0) break
                        n += r
                    }
                    isAnimatedHeader(buf, n)
                } ?: false
            }.getOrDefault(false)
        }
        cache[key] = result
        return result
    }

    private fun tag(b: ByteArray, at: Int, s: String): Boolean {
        if (at < 0 || at + s.length > b.size) return false
        for (i in s.indices) if (b[at + i].toInt() != s[i].code) return false
        return true
    }

    private fun isAnimatedHeader(b: ByteArray, n: Int): Boolean {
        if (n < 16) return false
        // WebP: RIFF....WEBP y la bandera de animación (bit 1) en VP8X
        if (tag(b, 0, "RIFF") && tag(b, 8, "WEBP")) {
            if (tag(b, 12, "VP8X") && n > 20) return (b[20].toInt() and 0x02) != 0
            return false
        }
        // AVIF / HEIF: caja ftyp con la marca "avis" (secuencia animada) en la marca principal o compatibles
        if (tag(b, 4, "ftyp")) {
            val size = ((b[0].toInt() and 0xFF) shl 24) or ((b[1].toInt() and 0xFF) shl 16) or
                ((b[2].toInt() and 0xFF) shl 8) or (b[3].toInt() and 0xFF)
            val end = minOf(if (size > 8) size else n, n)
            var i = 8
            while (i + 4 <= end) {
                if (tag(b, i, "avis")) return true
                i += 4
            }
            return false
        }
        // APNG: el trozo acTL aparece antes de los datos de imagen
        if (b[0] == 0x89.toByte() && tag(b, 1, "PNG")) {
            for (i in 8 until n - 3) if (tag(b, i, "acTL")) return true
            return false
        }
        return false
    }
}

/** Foto animada de cualquier formato: se resuelve al instante o, si hace falta, leyendo la cabecera en segundo plano */
@Composable
fun rememberIsAnimated(m: MediaFile): Boolean {
    val initial = AnimatedImages.known(m)
    if (initial != null) return initial
    val context = LocalContext.current.applicationContext
    var value by remember(m.uri) { mutableStateOf(false) }
    LaunchedEffect(m.uri) { value = AnimatedImages.check(context, m) }
    return value
}
