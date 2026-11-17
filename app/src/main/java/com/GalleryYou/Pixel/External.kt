package com.GalleryYou.Pixel

import android.app.Activity
import android.content.ClipData
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap

/** Naruto: ¡me convertiré en la galería predeterminada! Qué piden las otras apps cuando me abren. */
const val EXT_VIEW = 1
const val EXT_PICK = 2

class ExternalRequest(
    val kind: Int,
    val multiple: Boolean = false,
    val mimes: List<String> = emptyList(),
    val uri: Uri? = null,
)

/** Lee el Intent con el que se abrió la app: ver un archivo (VIEW) o elegir fotos para otra app (GET_CONTENT / PICK) */
fun parseExternal(intent: Intent?): ExternalRequest? {
    intent ?: return null
    return when (intent.action) {
        Intent.ACTION_VIEW -> intent.data?.let { ExternalRequest(EXT_VIEW, uri = it) }
        Intent.ACTION_GET_CONTENT, Intent.ACTION_PICK -> {
            val types = buildList {
                intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)?.let { addAll(it) }
                if (isEmpty()) intent.type?.let { add(it) }
            }
            ExternalRequest(
                EXT_PICK,
                multiple = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false),
                mimes = types,
            )
        }
        else -> null
    }
}

// Sailor Moon: filtro por tipo (imágenes, vídeos, un formato concreto o todo). Sin filtro, todo pasa.
fun mimeMatches(m: MediaFile, types: List<String>): Boolean {
    if (types.isEmpty()) return true
    return types.any { t ->
        val x = t.lowercase()
        when {
            x == "*/*" || x == "*" -> true
            x == "image/*" || x.startsWith("vnd.android.cursor.dir/image") -> !m.isVideo
            x == "video/*" || x.startsWith("vnd.android.cursor.dir/video") -> m.isVideo
            x.endsWith("/*") -> m.mime.lowercase().startsWith(x.dropLast(1))
            else -> m.mime.equals(x, ignoreCase = true)
        }
    }
}

/** Busca el archivo recibido entre los medios ya cargados (así el visor permite deslizar a los vecinos) */
fun indexOfExternal(all: List<MediaFile>, uri: Uri): Int {
    val direct = all.indexOfFirst { it.uri == uri }
    if (direct >= 0) return direct
    if (uri.authority == "media") {
        val id = runCatching { ContentUris.parseId(uri) }.getOrNull() ?: return -1
        val video = uri.pathSegments.any { it.equals("video", ignoreCase = true) }
        return all.indexOfFirst { it.id == id && it.isVideo == video }
    }
    return -1
}

/** Archivo que no está en MediaStore (descargas, otras apps): se describe con lo que el sistema sepa de él */
fun externalMedia(context: Context, uri: Uri): MediaFile {
    val resolver = context.contentResolver
    val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
    val mime = resolver.getType(uri)
        ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        ?: "image/*"
    var name = uri.lastPathSegment.orEmpty().substringAfterLast('/')
    var size = 0L
    if (uri.scheme == "content") {
        runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    c.getString(0)?.let { name = it }
                    size = c.getLong(1)
                }
            }
        }
    }
    return MediaFile(
        id = 0L,
        uri = uri,
        isVideo = mime.startsWith("video/"),
        name = name,
        dateMillis = System.currentTimeMillis(),
        durationMs = 0L,
        size = size,
        width = 0,
        height = 0,
        mime = mime,
        bucketId = 0L,
        bucket = "",
    )
}

/** Devuelve lo elegido a la app que lo pidió (con permiso de lectura temporal) y cierra la galería */
fun finishPick(activity: Activity?, items: List<MediaFile>) {
    activity ?: return
    if (items.isEmpty()) return
    val result = Intent().apply {
        data = items[0].uri
        val clip = ClipData.newUri(activity.contentResolver, "media", items[0].uri)
        for (m in items.drop(1)) clip.addItem(ClipData.Item(m.uri))
        clipData = clip
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    activity.setResult(Activity.RESULT_OK, result)
    activity.finish()
}
