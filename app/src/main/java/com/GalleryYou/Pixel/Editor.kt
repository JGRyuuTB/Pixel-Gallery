package com.GalleryYou.Pixel

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Edward Elric: para obtener algo, hay que dar algo a cambio (el original no se toca, se guarda una copia)
 * Editor de fotos: recortar con proporciones, girar, espejo, ajustes de color y filtros.
 */

/** Tanjiro repite la forma una y otra vez: giros y espejos se aplican en el orden en que se pulsan */
private enum class EditOp { ROTATE, FLIP_H, FLIP_V }

/** Gon y Killua: la foto grande para guardar (hasta 4096 px) y la ligera para mirar mientras se edita */
private class EditorBitmaps(val full: Bitmap, val preview: Bitmap)

/** Light Yagami anota el tamaño final de la libreta tras los giros */
private class OpsGeometry(val matrix: android.graphics.Matrix, val width: Int, val height: Int)

private val FullCrop = Rect(0f, 0f, 1f, 1f)

/** Todoroki marca qué lado del hielo se toca */
private enum class CropHandle { NONE, MOVE, TL, TR, BL, BR, L, T, R, B }

// ------------------------------------------------------------------ Geometría y color

/** Sailor Moon gira el cetro: calcula la matriz que deja la foto como pidió el usuario */
private fun opsGeometry(w: Int, h: Int, ops: List<EditOp>): OpsGeometry {
    val m = android.graphics.Matrix()
    var cw = w
    var ch = h
    for (op in ops) {
        when (op) {
            EditOp.ROTATE -> {
                m.postRotate(90f)
                m.postTranslate(ch.toFloat(), 0f)
                val t = cw
                cw = ch
                ch = t
            }
            EditOp.FLIP_H -> {
                m.postScale(-1f, 1f)
                m.postTranslate(cw.toFloat(), 0f)
            }
            EditOp.FLIP_V -> {
                m.postScale(1f, -1f)
                m.postTranslate(0f, ch.toFloat())
            }
        }
    }
    return OpsGeometry(m, cw, ch)
}

private fun applyOps(src: Bitmap, ops: List<EditOp>): Bitmap {
    if (ops.isEmpty()) return src
    val g = opsGeometry(src.width, src.height, ops)
    val out = Bitmap.createBitmap(g.width, g.height, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas(out).drawBitmap(src, g.matrix, android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG))
    return out
}

private fun satMatrix(s: Float) = android.graphics.ColorMatrix().apply { setSaturation(s) }

private fun contrastMatrix(c: Float): android.graphics.ColorMatrix {
    val t = 128f * (1f - c)
    return android.graphics.ColorMatrix(
        floatArrayOf(
            c, 0f, 0f, 0f, t,
            0f, c, 0f, 0f, t,
            0f, 0f, c, 0f, t,
            0f, 0f, 0f, 1f, 0f,
        ),
    )
}

private fun shiftMatrix(r: Float, g: Float, b: Float) = android.graphics.ColorMatrix(
    floatArrayOf(
        1f, 0f, 0f, 0f, r,
        0f, 1f, 0f, 0f, g,
        0f, 0f, 1f, 0f, b,
        0f, 0f, 0f, 1f, 0f,
    ),
)

/**
 * Receta de Sanji: filtro de base (0 = original) más brillo, contraste, saturación y calidez
 * Los valores de los deslizadores van de -100 a 100. El resultado sirve igual para la vista previa y para guardar.
 */
private fun editColorArray(brightness: Int, contrast: Int, saturation: Int, warmth: Int, filter: Int): FloatArray {
    val cm = android.graphics.ColorMatrix()
    when (filter) {
        1 -> {
            cm.postConcat(satMatrix(1.35f))
            cm.postConcat(contrastMatrix(1.08f))
        }
        2 -> {
            cm.postConcat(shiftMatrix(-14f, 2f, 22f))
            cm.postConcat(satMatrix(1.05f))
        }
        3 -> cm.postConcat(shiftMatrix(24f, 6f, -22f))
        4 -> {
            cm.postConcat(satMatrix(0f))
            cm.postConcat(contrastMatrix(1.1f))
        }
        5 -> cm.postConcat(
            android.graphics.ColorMatrix(
                floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            ),
        )
        6 -> {
            cm.postConcat(contrastMatrix(0.82f))
            cm.postConcat(shiftMatrix(16f, 16f, 16f))
            cm.postConcat(satMatrix(0.78f))
        }
        7 -> {
            cm.postConcat(contrastMatrix(1.35f))
            cm.postConcat(satMatrix(0.85f))
            cm.postConcat(shiftMatrix(-8f, -8f, -8f))
        }
    }
    if (saturation != 0) cm.postConcat(satMatrix((1f + saturation / 100f).coerceIn(0f, 2f)))
    if (contrast != 0) cm.postConcat(contrastMatrix(1f + contrast / 100f * 0.5f))
    if (brightness != 0) {
        val b = brightness / 100f * 60f
        cm.postConcat(shiftMatrix(b, b, b))
    }
    if (warmth != 0) {
        val w = warmth / 100f
        cm.postConcat(shiftMatrix(w * 28f, w * 4f, -w * 28f))
    }
    return cm.array.copyOf()
}

/** Pikachu elige su forma: proporciones del recorte, 0 libre, 1 original, y luego 1:1, 4:3, 3:4, 16:9 y 9:16 */
private fun aspectValue(key: Int, w: Int, h: Int): Float? = when (key) {
    0 -> null
    1 -> if (h > 0) w.toFloat() / h else null
    2 -> 1f
    3 -> 4f / 3f
    4 -> 3f / 4f
    5 -> 16f / 9f
    else -> 9f / 16f
}

/** Ichigo corta justo por el centro: el rectángulo normalizado más grande con la proporción pedida */
private fun aspectRect(a: Float?, w: Int, h: Int): Rect {
    if (a == null || w <= 0 || h <= 0) return FullCrop
    val imgA = w.toFloat() / h
    return if (a >= imgA) {
        val nh = imgA / a
        Rect(0f, (1f - nh) / 2f, 1f, (1f + nh) / 2f)
    } else {
        val nw = a / imgA
        Rect((1f - nw) / 2f, 0f, (1f + nw) / 2f, 1f)
    }
}

private fun squareThumb(src: Bitmap, px: Int): Bitmap {
    val side = minOf(src.width, src.height)
    val x = (src.width - side) / 2
    val y = (src.height - side) / 2
    val sq = Bitmap.createBitmap(src, x, y, side, side)
    val out = Bitmap.createScaledBitmap(sq, px, px, true)
    if (sq !== src && sq !== out) sq.recycle()
    return out
}

// ------------------------------------------------------------------ Carga y guardado

/** Naruto invoca mil clones: carga la foto (con su orientación EXIF) a 4096 px como máximo y prepara la versión ligera */
private suspend fun loadEditorBitmaps(context: Context, loader: ImageLoader, uri: Uri): EditorBitmaps? =
    withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(4096, 4096)
                .allowHardware(false)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build()
            val full = ((loader.execute(request) as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
                ?: return@withContext null
            val scale = 1600f / maxOf(full.width, full.height)
            val preview = if (scale >= 1f) {
                full
            } else {
                Bitmap.createScaledBitmap(
                    full,
                    (full.width * scale).roundToInt().coerceAtLeast(1),
                    (full.height * scale).roundToInt().coerceAtLeast(1),
                    true,
                )
            }
            EditorBitmaps(full, preview)
        } catch (e: Exception) {
            null
        }
    }

/** Kirito empuña dos espadas a la vez: giro, recorte y color se dibujan en una sola pasada sobre la foto grande */
private fun renderEdited(full: Bitmap, ops: List<EditOp>, crop: Rect, colors: FloatArray): Bitmap {
    val o = opsGeometry(full.width, full.height, ops)
    val left = (crop.left * o.width).roundToInt().coerceIn(0, o.width - 1)
    val top = (crop.top * o.height).roundToInt().coerceIn(0, o.height - 1)
    val right = (crop.right * o.width).roundToInt().coerceIn(left + 1, o.width)
    val bottom = (crop.bottom * o.height).roundToInt().coerceIn(top + 1, o.height)
    val out = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888)
    val m = android.graphics.Matrix(o.matrix)
    m.postTranslate(-left.toFloat(), -top.toFloat())
    val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG or android.graphics.Paint.ANTI_ALIAS_FLAG)
    paint.colorFilter = ColorMatrixColorFilter(android.graphics.ColorMatrix(colors))
    android.graphics.Canvas(out).drawBitmap(full, m, paint)
    return out
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun originalFolder(resolver: ContentResolver, uri: Uri): String? = try {
    resolver.query(uri, arrayOf(MediaStore.MediaColumns.RELATIVE_PATH), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getString(0)?.takeIf { it.isNotBlank() } else null
    }
} catch (e: Exception) {
    null
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun writeToMediaStore(context: Context, m: MediaFile, bitmap: Bitmap, fileName: String, mime: String, png: Boolean): Boolean {
    val resolver = context.contentResolver
    fun insert(folder: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, folder)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
    fun tryInsert(folder: String): Uri? = try {
        insert(folder)
    } catch (e: Exception) {
        null
    }
    // Naruto y su clon se quedan en la misma aldea: la copia va junto al original; si esa carpeta no admite fotos nuevas, cae en Pictures/Pixel Gallery
    val sameFolder = m.relativePath.takeIf { it.isNotBlank() } ?: originalFolder(resolver, m.uri)
    val fallback = Environment.DIRECTORY_PICTURES + "/Pixel Gallery"
    val target = (sameFolder?.let { tryInsert(it) } ?: tryInsert(fallback)) ?: return false
    val written = try {
        resolver.openOutputStream(target)?.use { stream ->
            bitmap.compress(if (png) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG, if (png) 100 else 95, stream)
        } ?: false
    } catch (e: Exception) {
        false
    }
    if (!written) {
        resolver.delete(target, null, null)
        return false
    }
    val done = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
    resolver.update(target, done, null, null)
    return true
}

/** Ash lanza la Poké Ball en Android 9 o menor: se escribe el archivo en Pictures/Pixel Gallery y se avisa al sistema para que lo vea */
@Suppress("DEPRECATION")
private fun writeToFile(context: Context, bitmap: Bitmap, fileName: String, mime: String, png: Boolean): Boolean {
    val dir = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Pixel Gallery")
    if (!dir.exists() && !dir.mkdirs()) return false
    val file = java.io.File(dir, fileName)
    val ok = java.io.FileOutputStream(file).use { stream ->
        bitmap.compress(if (png) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG, if (png) 100 else 95, stream)
    }
    android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(mime), null)
    return ok
}

/** Aang domina los cuatro elementos: renderiza, elige formato (PNG se queda PNG, el resto JPEG) y guarda la copia */
private suspend fun saveEditedCopy(
    context: Context,
    m: MediaFile,
    full: Bitmap,
    ops: List<EditOp>,
    crop: Rect,
    colors: FloatArray,
): Boolean = withContext(Dispatchers.IO) {
    var out: Bitmap? = null
    try {
        val rendered = renderEdited(full, ops, crop, colors)
        out = rendered
        val png = m.mime.equals("image/png", ignoreCase = true)
        val mime = if (png) "image/png" else "image/jpeg"
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val base = m.name.substringBeforeLast('.', m.name).ifBlank { "foto" }
        val fileName = base + "_edit_" + stamp + if (png) ".png" else ".jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToMediaStore(context, m, rendered, fileName, mime, png)
        } else {
            writeToFile(context, rendered, fileName, mime, png)
        }
    } catch (e: Exception) {
        false
    } finally {
        out?.recycle()
    }
}

// ------------------------------------------------------------------ Recorte con el dedo

private fun fitInside(boxW: Float, boxH: Float, w: Float, h: Float, pad: Float): Rect {
    val availW = (boxW - 2f * pad).coerceAtLeast(1f)
    val availH = (boxH - 2f * pad).coerceAtLeast(1f)
    val scale = minOf(availW / w.coerceAtLeast(1f), availH / h.coerceAtLeast(1f))
    val dw = w * scale
    val dh = h * scale
    val left = (boxW - dw) / 2f
    val top = (boxH - dh) / 2f
    return Rect(left, top, left + dw, top + dh)
}

private fun cropToPx(c: Rect, r: Rect) = Rect(
    r.left + c.left * r.width,
    r.top + c.top * r.height,
    r.left + c.right * r.width,
    r.top + c.bottom * r.height,
)

private fun pxToCrop(p: Rect, r: Rect) = Rect(
    ((p.left - r.left) / r.width).coerceIn(0f, 1f),
    ((p.top - r.top) / r.height).coerceIn(0f, 1f),
    ((p.right - r.left) / r.width).coerceIn(0f, 1f),
    ((p.bottom - r.top) / r.height).coerceIn(0f, 1f),
)

/** Hinata mira con el Byakugan: qué esquina, lado o interior del recorte se ha tocado */
private fun pickHandle(p: Offset, c: Rect, slop: Float): CropHandle {
    fun near(x: Float, y: Float) = (p - Offset(x, y)).getDistance() <= slop
    return when {
        near(c.left, c.top) -> CropHandle.TL
        near(c.right, c.top) -> CropHandle.TR
        near(c.left, c.bottom) -> CropHandle.BL
        near(c.right, c.bottom) -> CropHandle.BR
        abs(p.x - c.left) <= slop && p.y >= c.top && p.y <= c.bottom -> CropHandle.L
        abs(p.x - c.right) <= slop && p.y >= c.top && p.y <= c.bottom -> CropHandle.R
        abs(p.y - c.top) <= slop && p.x >= c.left && p.x <= c.right -> CropHandle.T
        abs(p.y - c.bottom) <= slop && p.x >= c.left && p.x <= c.right -> CropHandle.B
        c.contains(p) -> CropHandle.MOVE
        else -> CropHandle.NONE
    }
}

/**
 * Gear Second de Luffy: mueve o estira el recorte dentro de la foto [r]
 * Con proporción fija se ancla el lado opuesto al que se arrastra; sin ella, cada lado va por libre.
 */
private fun dragCrop(h: CropHandle, c: Rect, d: Offset, r: Rect, minPx: Float, aspect: Float?): Rect {
    if (h == CropHandle.MOVE) {
        val l = (c.left + d.x).coerceIn(r.left, r.right - c.width)
        val t = (c.top + d.y).coerceIn(r.top, r.bottom - c.height)
        return Rect(l, t, l + c.width, t + c.height)
    }
    val movesLeft = h == CropHandle.TL || h == CropHandle.BL || h == CropHandle.L
    val movesRight = h == CropHandle.TR || h == CropHandle.BR || h == CropHandle.R
    val movesTop = h == CropHandle.TL || h == CropHandle.TR || h == CropHandle.T
    val movesBottom = h == CropHandle.BL || h == CropHandle.BR || h == CropHandle.B
    if (aspect == null) {
        var l = c.left
        var t = c.top
        var rt = c.right
        var b = c.bottom
        if (movesLeft) l = (l + d.x).coerceIn(r.left, rt - minPx)
        if (movesRight) rt = (rt + d.x).coerceIn(l + minPx, r.right)
        if (movesTop) t = (t + d.y).coerceIn(r.top, b - minPx)
        if (movesBottom) b = (b + d.y).coerceIn(t + minPx, r.bottom)
        return Rect(l, t, rt, b)
    }
    val horizontal = movesLeft || movesRight
    val vertical = movesTop || movesBottom
    val ax = if (movesLeft) c.right else if (movesRight) c.left else c.center.x
    val ay = if (movesTop) c.bottom else if (movesBottom) c.top else c.center.y
    val roomX = if (movesLeft) ax - r.left else r.right - ax
    val roomY = if (movesTop) ay - r.top else r.bottom - ay
    var w: Float
    var hgt: Float
    if (horizontal) {
        val mx = (if (movesLeft) c.left else c.right) + d.x
        w = abs(mx - ax)
        w = if (vertical) {
            minOf(w, roomX, roomY * aspect)
        } else {
            val halfRoom = minOf(c.center.y - r.top, r.bottom - c.center.y)
            minOf(w, roomX, halfRoom * 2f * aspect)
        }
        w = maxOf(w, minPx).coerceAtMost(r.width)
        hgt = (w / aspect).coerceAtMost(r.height)
    } else {
        val my = (if (movesTop) c.top else c.bottom) + d.y
        val halfRoomX = minOf(c.center.x - r.left, r.right - c.center.x)
        val hMax = minOf(roomY, halfRoomX * 2f / aspect)
        hgt = maxOf(minOf(abs(my - ay), hMax), minPx / aspect).coerceAtMost(r.height)
        w = (hgt * aspect).coerceAtMost(r.width)
    }
    val left = when {
        movesLeft -> ax - w
        movesRight -> ax
        else -> c.center.x - w / 2f
    }
    val top = when {
        movesTop -> ay - hgt
        movesBottom -> ay
        else -> c.center.y - hgt / 2f
    }
    val l = left.coerceIn(r.left, (r.right - w).coerceAtLeast(r.left))
    val t = top.coerceIn(r.top, (r.bottom - hgt).coerceAtLeast(r.top))
    return Rect(l, t, l + w, t + hgt)
}

/**
 * Zoro afila las tres espadas: la foto en pantalla, con el recorte encima cuando se está recortando
 * Fuera del modo recorte se ve solo el resultado recortado, con el color ya aplicado.
 */
@Composable
private fun EditCanvas(
    image: ImageBitmap,
    crop: Rect,
    cropMode: Boolean,
    colorFilter: ColorFilter?,
    lockAspect: Float?,
    onCrop: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val pad = with(density) { 18.dp.toPx() }
    val slop = with(density) { 30.dp.toPx() }
    val minPx = with(density) { 64.dp.toPx() }
    val stroke = with(density) { 1.5.dp.toPx() }
    val handleLen = with(density) { 22.dp.toPx() }
    val handleW = with(density) { 4.dp.toPx() }
    val cropState by rememberUpdatedState(crop)
    val onCropState by rememberUpdatedState(onCrop)
    val lockState by rememberUpdatedState(lockAspect)

    Canvas(
        modifier
            .fillMaxSize()
            .pointerInput(cropMode, image) {
                if (cropMode) {
                    var handle = CropHandle.NONE
                    var current = cropState
                    detectDragGestures(
                        onDragStart = { pos ->
                            val r = fitInside(size.width.toFloat(), size.height.toFloat(), image.width.toFloat(), image.height.toFloat(), pad)
                            current = cropState
                            handle = pickHandle(pos, cropToPx(current, r), slop)
                        },
                        onDrag = { change, drag ->
                            if (handle != CropHandle.NONE) {
                                change.consume()
                                val r = fitInside(size.width.toFloat(), size.height.toFloat(), image.width.toFloat(), image.height.toFloat(), pad)
                                val moved = dragCrop(handle, cropToPx(current, r), drag, r, minPx, lockState)
                                current = pxToCrop(moved, r)
                                onCropState(current)
                            }
                        },
                        onDragEnd = { handle = CropHandle.NONE },
                        onDragCancel = { handle = CropHandle.NONE },
                    )
                }
            },
    ) {
        val iw = image.width
        val ih = image.height
        if (cropMode) {
            val r = fitInside(size.width, size.height, iw.toFloat(), ih.toFloat(), pad)
            drawImage(
                image = image,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(iw, ih),
                dstOffset = IntOffset(r.left.roundToInt(), r.top.roundToInt()),
                dstSize = IntSize(r.width.roundToInt(), r.height.roundToInt()),
                colorFilter = colorFilter,
            )
            val c = cropToPx(cropState, r)
            val dim = Color.Black.copy(alpha = 0.55f)
            drawRect(dim, topLeft = Offset(r.left, r.top), size = Size(r.width, c.top - r.top))
            drawRect(dim, topLeft = Offset(r.left, c.bottom), size = Size(r.width, r.bottom - c.bottom))
            drawRect(dim, topLeft = Offset(r.left, c.top), size = Size(c.left - r.left, c.height))
            drawRect(dim, topLeft = Offset(c.right, c.top), size = Size(r.right - c.right, c.height))
            // Regla de tercios: el ojo de Itachi encuentra el punto fuerte de la composición
            val grid = Color.White.copy(alpha = 0.35f)
            for (i in 1..2) {
                val x = c.left + c.width * i / 3f
                val y = c.top + c.height * i / 3f
                drawLine(grid, Offset(x, c.top), Offset(x, c.bottom), strokeWidth = 1f)
                drawLine(grid, Offset(c.left, y), Offset(c.right, y), strokeWidth = 1f)
            }
            drawRect(Color.White, topLeft = Offset(c.left, c.top), size = Size(c.width, c.height), style = Stroke(stroke))
            fun corner(x: Float, y: Float, sx: Float, sy: Float) {
                drawLine(Color.White, Offset(x, y), Offset(x + sx * handleLen, y), strokeWidth = handleW, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(x, y), Offset(x, y + sy * handleLen), strokeWidth = handleW, cap = StrokeCap.Round)
            }
            corner(c.left, c.top, 1f, 1f)
            corner(c.right, c.top, -1f, 1f)
            corner(c.left, c.bottom, 1f, -1f)
            corner(c.right, c.bottom, -1f, -1f)
        } else {
            val cw = (cropState.width * iw).coerceAtLeast(1f)
            val ch = (cropState.height * ih).coerceAtLeast(1f)
            val r = fitInside(size.width, size.height, cw, ch, pad)
            drawImage(
                image = image,
                srcOffset = IntOffset((cropState.left * iw).roundToInt(), (cropState.top * ih).roundToInt()),
                srcSize = IntSize(cw.roundToInt(), ch.roundToInt()),
                dstOffset = IntOffset(r.left.roundToInt(), r.top.roundToInt()),
                dstSize = IntSize(r.width.roundToInt(), r.height.roundToInt()),
                colorFilter = colorFilter,
            )
        }
    }
}

// ------------------------------------------------------------------ Pantalla del editor

/**
 * Sailor Moon: ¡por el poder del editor lunar!
 * Pantalla completa con la foto arriba, las herramientas abajo y tres pestañas: Recortar, Ajustes y Filtros.
 * [onSaved] se llama cuando la copia ya está en la galería; [onClose] al salir (con aviso si hay cambios sin guardar).
 */
@Composable
fun EditorScreen(media: MediaFile, onClose: () -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val loader = LocalGalleryLoader.current
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme

    var loadFailed by remember { mutableStateOf(false) }
    val loaded by produceState<EditorBitmaps?>(initialValue = null, media.uri) {
        val result = loadEditorBitmaps(context, loader, media.uri)
        if (result == null) loadFailed = true
        value = result
    }
    LaunchedEffect(loadFailed) {
        if (loadFailed) {
            Toast.makeText(context, R.string.editor_load_failed, Toast.LENGTH_LONG).show()
            onClose()
        }
    }

    var ops by remember { mutableStateOf(emptyList<EditOp>()) }
    var crop by remember { mutableStateOf(FullCrop) }
    var aspectKey by remember { mutableIntStateOf(0) }
    var brightness by remember { mutableIntStateOf(0) }
    var contrast by remember { mutableIntStateOf(0) }
    var saturation by remember { mutableIntStateOf(0) }
    var warmth by remember { mutableIntStateOf(0) }
    var fx by remember { mutableIntStateOf(0) }
    var tab by remember { mutableIntStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var askDiscard by remember { mutableStateOf(false) }

    val bitmaps = loaded
    val oriented = remember(bitmaps, ops) { bitmaps?.let { applyOps(it.preview, ops) } }
    val orientedImage = remember(oriented) { oriented?.asImageBitmap() }
    val tileThumb = remember(oriented) { oriented?.let { squareThumb(it, 160) }?.asImageBitmap() }
    val colors = remember(brightness, contrast, saturation, warmth, fx) {
        editColorArray(brightness, contrast, saturation, warmth, fx)
    }
    val isIdentity = fx == 0 && brightness == 0 && contrast == 0 && saturation == 0 && warmth == 0
    val colorFilter = remember(colors, isIdentity) {
        if (isIdentity) null else ColorFilter.colorMatrix(ColorMatrix(colors))
    }
    val changed = ops.isNotEmpty() || crop != FullCrop || !isIdentity
    val ow = oriented?.width ?: 0
    val oh = oriented?.height ?: 0

    // Tanjiro repite la forma: cada giro o espejo reinicia el recorte para que no apunte a un lugar que ya no existe
    fun commitOps(newOps: List<EditOp>) {
        ops = newOps
        val b = bitmaps ?: return
        val g = opsGeometry(b.preview.width, b.preview.height, newOps)
        crop = aspectRect(aspectValue(aspectKey, g.width, g.height), g.width, g.height)
    }

    fun pickAspect(key: Int) {
        aspectKey = key
        if (key != 0) crop = aspectRect(aspectValue(key, ow, oh), ow, oh)
    }

    fun startSave() {
        val b = bitmaps ?: return
        if (saving) return
        saving = true
        val snapOps = ops
        val snapCrop = crop
        val snapColors = colors
        scope.launch {
            val ok = saveEditedCopy(context, media, b.full, snapOps, snapCrop, snapColors)
            saving = false
            if (ok) {
                Toast.makeText(context, R.string.editor_saved, Toast.LENGTH_SHORT).show()
                onSaved()
            } else {
                Toast.makeText(context, R.string.editor_save_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Aang pide permiso a los monjes del Templo del Aire (Android 9 o menor necesita permiso de escritura, una sola vez)
    val writeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startSave() else Toast.makeText(context, R.string.editor_save_failed, Toast.LENGTH_LONG).show()
    }

    fun requestSave() {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) writeLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE) else startSave()
    }

    fun tryClose() {
        if (saving) return
        if (changed) askDiscard = true else onClose()
    }

    BackHandler { tryClose() }

    val aspectLabels = listOf(
        stringResource(R.string.editor_aspect_free),
        stringResource(R.string.editor_aspect_original),
        "1:1",
        "4:3",
        "3:4",
        "16:9",
        "9:16",
    )
    val fxNames = listOf(
        stringResource(R.string.fx_original),
        stringResource(R.string.fx_vivid),
        stringResource(R.string.fx_cool),
        stringResource(R.string.fx_warm),
        stringResource(R.string.fx_mono),
        stringResource(R.string.fx_sepia),
        stringResource(R.string.fx_fade),
        stringResource(R.string.fx_drama),
    )

    // Totoro bloquea el camino: los toques no se cuelan al visor que está debajo
    Column(
        Modifier
            .fillMaxSize()
            .background(scheme.surface)
            .pointerInput(Unit) {}
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { tryClose() }) {
                Icon(AppIcons.Close, contentDescription = stringResource(R.string.editor_close))
            }
            Text(
                stringResource(R.string.editor_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
            Button(onClick = { requestSave() }, enabled = bitmaps != null && !saving) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current,
                    )
                } else {
                    Text(stringResource(R.string.editor_save))
                }
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val img = orientedImage
            if (img == null) {
                CircularProgressIndicator()
            } else {
                EditCanvas(
                    image = img,
                    crop = crop,
                    cropMode = tab == 0,
                    colorFilter = colorFilter,
                    lockAspect = aspectValue(aspectKey, ow, oh),
                    onCrop = { crop = it },
                )
            }
        }

        Surface(
            color = scheme.surfaceContainer,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Box(Modifier.fillMaxWidth().height(204.dp)) {
                    when (tab) {
                        0 -> Column(
                            Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Row(
                                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                aspectLabels.forEachIndexed { i, label ->
                                    FilterChip(
                                        selected = aspectKey == i,
                                        onClick = { pickAspect(i) },
                                        label = { Text(label) },
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                ToolButton(AppIcons.Refresh, stringResource(R.string.editor_rotate)) {
                                    commitOps(ops + EditOp.ROTATE)
                                }
                                ToolButton(AppIcons.FlipH, stringResource(R.string.editor_flip_h)) {
                                    commitOps(ops + EditOp.FLIP_H)
                                }
                                ToolButton(AppIcons.FlipV, stringResource(R.string.editor_flip_v)) {
                                    commitOps(ops + EditOp.FLIP_V)
                                }
                                ToolButton(AppIcons.Replay, stringResource(R.string.editor_reset)) {
                                    aspectKey = 0
                                    commitOps(emptyList())
                                }
                            }
                        }
                        1 -> Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            EditorSlider(stringResource(R.string.editor_adj_brightness), brightness) { brightness = it }
                            EditorSlider(stringResource(R.string.editor_adj_contrast), contrast) { contrast = it }
                            EditorSlider(stringResource(R.string.editor_adj_saturation), saturation) { saturation = it }
                            EditorSlider(stringResource(R.string.editor_adj_warmth), warmth) { warmth = it }
                            TextButton(
                                onClick = {
                                    brightness = 0
                                    contrast = 0
                                    saturation = 0
                                    warmth = 0
                                },
                                modifier = Modifier.align(Alignment.End).padding(end = 8.dp),
                            ) { Text(stringResource(R.string.editor_reset)) }
                        }
                        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LazyRow(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(count = fxNames.size) { i ->
                                    val selected = fx == i
                                    val shape = RoundedCornerShape(16.dp)
                                    Column(
                                        Modifier.width(76.dp).clickable { fx = i },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Box(
                                            Modifier
                                                .size(76.dp)
                                                .clip(shape)
                                                .border(2.dp, if (selected) scheme.primary else Color.Transparent, shape),
                                        ) {
                                            val thumb = tileThumb
                                            if (thumb != null) {
                                                Image(
                                                    bitmap = thumb,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    colorFilter = if (i == 0) {
                                                        null
                                                    } else {
                                                        ColorFilter.colorMatrix(ColorMatrix(editColorArray(0, 0, 0, 0, i)))
                                                    },
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            }
                                        }
                                        Text(
                                            fxNames[i],
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) scheme.primary else scheme.onSurfaceVariant,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 6.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EditorTab(AppIcons.Crop, stringResource(R.string.editor_tab_crop), tab == 0) { tab = 0 }
                    EditorTab(AppIcons.Tune, stringResource(R.string.editor_tab_adjust), tab == 1) { tab = 1 }
                    EditorTab(AppIcons.Sparkle, stringResource(R.string.editor_tab_filters), tab == 2) { tab = 2 }
                }
            }
        }
    }

    if (askDiscard) {
        AlertDialog(
            onDismissRequest = { askDiscard = false },
            title = { Text(stringResource(R.string.editor_discard_title)) },
            text = { Text(stringResource(R.string.editor_discard_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        askDiscard = false
                        onClose()
                    },
                ) { Text(stringResource(R.string.editor_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { askDiscard = false }) { Text(stringResource(R.string.editor_keep)) }
            },
        )
    }
}

/** Sanji sirve el plato: una fila con nombre, deslizador y valor */
@Composable
private fun EditorSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(96.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = -100f..100f,
            modifier = Modifier.weight(1f),
        )
        Text(
            value.toString(),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp),
        )
    }
}

/** Pokébola de herramientas: icono y nombre debajo */
@Composable
private fun ToolButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp), maxLines = 1)
    }
}

/** Jiraiya señala el camino: la pestaña elegida muestra su nombre, las demás solo el icono */
@Composable
private fun EditorTab(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (selected) scheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        )
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = scheme.onPrimaryContainer, maxLines = 1)
        }
    }
}
