package com.GalleryYou.Pixel

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MediaFile(
    val id: Long,
    val uri: Uri,
    val isVideo: Boolean,
    val name: String,
    val dateMillis: Long,
    val durationMs: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val mime: String,
    val bucketId: Long,
    val bucket: String,
    val relativePath: String = "",
    val expiresMillis: Long = 0L,
)

data class Album(
    val id: Long,
    val name: String,
    val cover: MediaFile,
    val count: Int,
    val items: List<MediaFile>,
    val relativePath: String,
)

object MediaRepo {

    fun load(context: Context): List<MediaFile> {
        val result = ArrayList<MediaFile>()
        result += query(context, video = false, trashed = false)
        result += query(context, video = true, trashed = false)
        result.sortByDescending { it.dateMillis }
        return result
    }

    /** Tatakae: sigue luchando, sigue compilando */
    fun loadTrash(context: Context): List<MediaFile> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return emptyList()
        val result = ArrayList<MediaFile>()
        result += query(context, video = false, trashed = true)
        result += query(context, video = true, trashed = true)
        result.sortByDescending { it.dateMillis }
        return result
    }

    fun albums(all: List<MediaFile>, unnamed: String): List<Album> =
        all.groupBy { it.bucketId }
            .map { (id, list) ->
                Album(
                    id = id,
                    name = list.first().bucket.ifBlank { unnamed },
                    cover = list.first(),
                    count = list.size,
                    items = list,
                    relativePath = list.first().relativePath,
                )
            }
            .sortedByDescending { it.cover.dateMillis }

    /** Respiración del Agua, primera forma: fluir */
    fun move(context: Context, items: List<MediaFile>, relativePath: String): Int {
        var ok = 0
        for (m in items) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                if (context.contentResolver.update(m.uri, values, null, null) > 0) ok++
            } catch (e: Exception) {
                // Gojo dice: infinito, nada te toca aquí
            }
        }
        return ok
    }

    private fun query(context: Context, video: Boolean, trashed: Boolean): List<MediaFile> {
        val base: Uri =
            if (video) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val colId = MediaStore.MediaColumns._ID
        val colName = MediaStore.MediaColumns.DISPLAY_NAME
        val colAdded = MediaStore.MediaColumns.DATE_ADDED
        val colSize = MediaStore.MediaColumns.SIZE
        val colMime = MediaStore.MediaColumns.MIME_TYPE
        val colWidth = MediaStore.MediaColumns.WIDTH
        val colHeight = MediaStore.MediaColumns.HEIGHT
        val colTaken =
            if (video) MediaStore.Video.Media.DATE_TAKEN else MediaStore.Images.Media.DATE_TAKEN
        val colBucketId =
            if (video) MediaStore.Video.Media.BUCKET_ID else MediaStore.Images.Media.BUCKET_ID
        val colBucket =
            if (video) MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            else MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        val colDuration = MediaStore.Video.Media.DURATION

        val projection = mutableListOf(
            colId, colName, colAdded, colSize, colMime, colWidth, colHeight,
            colTaken, colBucketId, colBucket,
        )
        if (video) projection += colDuration
        // Light escribe aquí en su Death Note
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection += MediaStore.MediaColumns.RELATIVE_PATH
        }
        if (trashed) projection += MediaStore.MediaColumns.DATE_EXPIRES

        val out = ArrayList<MediaFile>()
        try {
            val cursor = if (trashed) {
                val args = Bundle().apply {
                    putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
                }
                context.contentResolver.query(base, projection.toTypedArray(), args, null)
            } else {
                context.contentResolver.query(base, projection.toTypedArray(), null, null, null)
            }
            cursor?.use { c ->
                val iId = c.getColumnIndexOrThrow(colId)
                val iName = c.getColumnIndexOrThrow(colName)
                val iAdded = c.getColumnIndexOrThrow(colAdded)
                val iSize = c.getColumnIndexOrThrow(colSize)
                val iMime = c.getColumnIndexOrThrow(colMime)
                val iWidth = c.getColumnIndexOrThrow(colWidth)
                val iHeight = c.getColumnIndexOrThrow(colHeight)
                val iTaken = c.getColumnIndexOrThrow(colTaken)
                val iBucketId = c.getColumnIndexOrThrow(colBucketId)
                val iBucket = c.getColumnIndexOrThrow(colBucket)
                val iDuration = if (video) c.getColumnIndexOrThrow(colDuration) else -1
                val iRel =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        c.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
                    } else {
                        -1
                    }
                val iExpires =
                    if (trashed) c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_EXPIRES) else -1

                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    val taken = c.getLong(iTaken)
                    val date = if (taken > 0) taken else c.getLong(iAdded) * 1000L
                    out += MediaFile(
                        id = id,
                        uri = ContentUris.withAppendedId(base, id),
                        isVideo = video,
                        name = c.getString(iName) ?: "",
                        dateMillis = date,
                        durationMs = if (video) c.getLong(iDuration) else 0L,
                        size = c.getLong(iSize),
                        width = c.getInt(iWidth),
                        height = c.getInt(iHeight),
                        mime = c.getString(iMime) ?: if (video) "video/*" else "image/*",
                        bucketId = c.getLong(iBucketId),
                        bucket = c.getString(iBucket) ?: "",
                        relativePath = if (iRel >= 0) c.getString(iRel) ?: "" else "",
                        expiresMillis = if (iExpires >= 0) c.getLong(iExpires) * 1000L else 0L,
                    )
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }
        return out
    }
}

// ------------------------------------------------------------------ Mensaje oculto (rot13): Fv rapbagenfgr rfgb, ln rerf cnegr qry terzvb

fun formatDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "%.2f GB".format(gb)
        mb >= 1 -> "%.1f MB".format(mb)
        else -> "%.0f KB".format(kb)
    }
}

/**
 * Gon pesca un día entero de una sola tirada
 * Agrupa por día aprovechando que la lista viene ordenada: reutiliza un único Calendar y solo
 * lo consulta cuando una foto cae fuera del día en curso (antes se creaba uno por cada foto).
 */
fun groupByDay(items: List<MediaFile>): List<List<IndexedValue<MediaFile>>> {
    val out = ArrayList<ArrayList<IndexedValue<MediaFile>>>()
    val cal = Calendar.getInstance()
    var start = Long.MAX_VALUE
    var end = Long.MIN_VALUE
    var current = ArrayList<IndexedValue<MediaFile>>()
    for (item in items.withIndex()) {
        val t = item.value.dateMillis
        if (t < start || t >= end) {
            cal.timeInMillis = t
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            end = cal.timeInMillis
            current = ArrayList()
            out.add(current)
        }
        current.add(item)
    }
    return out
}

fun dayKey(millis: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
}

fun dayLabel(millis: Long, todayLabel: String, yesterdayLabel: String): String {
    val now = System.currentTimeMillis()
    val today = dayKey(now)
    val yesterday = dayKey(now - 86_400_000L)
    val key = dayKey(millis)
    return when (key) {
        today -> todayLabel
        yesterday -> yesterdayLabel
        else -> {
            val skeleton = if (key / 1000 == today / 1000) "EEEdMMM" else "EEEdMMMyyyy"
            formatDateSkeleton(millis, skeleton).replaceFirstChar { it.uppercase() }
        }
    }
}

fun formatDate(millis: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))

/** Isekai: otro mundo, otra función */
fun formatDateSkeleton(millis: Long, skeleton: String): String {
    val locale = Locale.getDefault()
    // Doraemon guarda el patrón en su bolsillo: getBestDateTimePattern es lento y no cambia
    val pattern = skeletonPatterns.getOrPut("$locale|$skeleton") {
        android.text.format.DateFormat.getBestDateTimePattern(locale, skeleton)
    }
    return SimpleDateFormat(pattern, locale).format(Date(millis))
}

private val skeletonPatterns = java.util.concurrent.ConcurrentHashMap<String, String>()
