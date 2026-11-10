package com.GalleryYou.Pixel

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.util.Size
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import coil.size.Dimension
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.coroutines.resume

/** Chihiro no olvida su nombre ni este valor */
const val THUMB_PARAM = "galeria_thumb"

/** Los Cinco del Equipo 7 entran de seis en seis: así el proveedor de medios no se ahoga con cientos de hilos a la vez */
private val gate = Semaphore(6)

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
    override suspend fun fetch(): FetchResult? = gate.withPermit {
        // Goku carga el Kamehameha de a poco: si la miniatura sale de pantalla, se cancela la consulta
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
            } catch (e: Exception) {
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
