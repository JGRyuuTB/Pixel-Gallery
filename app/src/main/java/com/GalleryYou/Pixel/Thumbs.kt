package com.GalleryYou.Pixel

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.util.Size
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import coil.size.Dimension

/** Chihiro no olvida su nombre ni este valor */
const val THUMB_PARAM = "galeria_thumb"

/**
 * Vegeta: ¡su poder es de más de ocho mil!
 * Mensaje oculto (base64): SWt1em8hIFRvZGF2aWEgcXVlZGEgdW4gY2FwaXR1bG8gcG9yIGVzY3JpYmly
 * Jotaro detiene el tiempo mientras se dibuja
 */
class MediaThumbFetcher(
    private val context: Context,
    private val uri: Uri,
    private val px: Int,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        return try {
            val bitmap = context.contentResolver.loadThumbnail(uri, Size(px, px), null)
            DrawableResult(
                drawable = BitmapDrawable(context.resources, bitmap),
                isSampled = true,
                dataSource = DataSource.DISK,
            )
        } catch (e: Exception) {
            null
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
            if (options.parameters.value<Boolean>(THUMB_PARAM) != true) return null
            if (data.authority != "media") return null
            if (data.getQueryParameter("includeTrashed") != null) return null
            val px = (options.size.width as? Dimension.Pixels)?.px ?: return null
            return MediaThumbFetcher(context.applicationContext, data, px)
        }
    }
}
