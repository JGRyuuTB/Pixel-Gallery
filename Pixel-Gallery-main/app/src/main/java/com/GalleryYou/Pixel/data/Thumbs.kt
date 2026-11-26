package com.GalleryYou.Pixel.data

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.request.Options
import coil.size.Dimension
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

const val THUMB_PARAM = "galeria_thumb"

val LocalGalleryLoader = staticCompositionLocalOf<ImageLoader> {
    error("ImageLoader not provided")
}

private val gate = Semaphore(6)

fun buildThumbRequest(context: Context, uri: Uri, size: Int): ImageRequest =
    ImageRequest.Builder(context)
        .data(uri)
        .size(size)
        .crossfade(140)
        .setParameter(THUMB_PARAM, true)
        .build()

@Composable
fun thumbRequest(uri: Uri, size: Int = 320): ImageRequest {
    val context = LocalContext.current
    return remember(uri, size) { buildThumbRequest(context, uri, size) }
}

fun thumbUriFor(m: MediaFile): Uri =
    if (m.expiresMillis > 0L && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        m.uri.buildUpon().appendQueryParameter("includeTrashed", "1").build()
    } else {
        m.uri
    }

fun thumbSizeFor(columns: Int): Int = when {
    columns >= 4 -> 256
    columns == 3 -> 384
    else -> 540
}

class MediaThumbFetcher(
    private val context: Context,
    private val uri: Uri,
    private val px: Int,
) : Fetcher {
    override suspend fun fetch(): FetchResult? = gate.withPermit {
        suspendCancellableCoroutine<FetchResult?> { cont ->
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            val result: FetchResult? = try {
                val bitmap = context.contentResolver.loadThumbnail(uri, Size(px, px), signal)
                DrawableResult(
                    drawable = BitmapDrawable(context.resources, bitmap),
                    isSampled = true,
                    dataSource = DataSource.DISK,
                )
            } catch (_: Exception) {
                null
            }
            if (cont.isActive) cont.resume(result)
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
