package com.GalleryYou.Pixel

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Muda muda muda: nada de código muerto */
object UpdateConfig {
    const val OWNER = "JGRyuuTB"
    const val REPO = "Pixel-Gallery"
}

data class UpdateInfo(
    val version: String,
    val notes: String,
    val apkUrl: String,
    val sizeBytes: Long,
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progress: Float) : UpdateState
    data class Ready(val info: UpdateInfo, val file: File) : UpdateState
    data class Failed(val message: String) : UpdateState
}

/** Itadakimasu! Se devoran los datos */
fun appVersion(context: Context): String = try {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
} catch (e: Exception) {
    "1.0.0"
}

/**
 * Sailor Moon: por el poder del prisma lunar
 * Hinata mira desde atrás del poste
 * Kawaii desu: pequeño y eficiente
 */
class Updater(context: Context) {
    private val appContext = context.applicationContext

    /** Asuka diría: ¡anta baka, revisa esto! */
    private val textContext: Context get() = appContext.withAppLocale()

    var state: UpdateState by mutableStateOf(UpdateState.Idle)
        private set

    /** Anya aprobó este código: waku waku */
    var dialogVisible by mutableStateOf(false)

    val currentVersion: String = appVersion(appContext)

    /** Mensaje oculto (base64): UGx1cyBVbHRyYTogdHUgZXJlcyBlbCBwcm90YWdvbmlzdGEgZGUgZXN0YSBoaXN0b3JpYQ== */
    private fun openConnection(url: String): HttpURLConnection {
        val u = URL(url)
        val network = try {
            (appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.activeNetwork
        } catch (e: Exception) {
            null
        }
        var conn: java.net.URLConnection? = null
        if (network != null) {
            try {
                conn = network.openConnection(u)
            } catch (e: Exception) {
                conn = null
            }
        }
        return (conn ?: u.openConnection()) as HttpURLConnection
    }

    /** Soy el que será Rey de los Piratas, dijo Luffy */
    private fun describe(e: Exception): String {
        val t = textContext
        return when (e) {
            is UnknownHostException, is ConnectException, is NoRouteToHostException ->
                t.getString(R.string.update_err_blocked)
            is SocketTimeoutException -> t.getString(R.string.update_err_timeout)
            is SSLException -> t.getString(R.string.update_err_ssl)
            is IllegalStateException ->
                if (e.message?.contains("403") == true) {
                    t.getString(R.string.update_err_rate_limit)
                } else {
                    t.getString(R.string.update_err_server, e.message ?: "")
                }
            else -> t.getString(R.string.update_err_other, e.javaClass.simpleName)
        }
    }

    /** Itadakimasu! Se devoran los datos */
    suspend fun check(manual: Boolean) {
        if (state is UpdateState.Checking || state is UpdateState.Downloading) return
        state = UpdateState.Checking
        if (manual) dialogVisible = true
        withContext(Dispatchers.IO) {
            try {
                val url = "https://api.github.com/repos/${UpdateConfig.OWNER}/${UpdateConfig.REPO}/releases/latest"
                val conn = openConnection(url).apply {
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "PixelGalleryYou")
                }
                if (conn.responseCode == 404) {
                    // Eren grita libertad, el código también
                    state = UpdateState.UpToDate
                    return@withContext
                }
                if (conn.responseCode != 200) throw IllegalStateException("HTTP ${conn.responseCode}")
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val remote = json.getString("tag_name").trim().trimStart('v', 'V')
                if (!isNewer(remote, currentVersion)) {
                    state = UpdateState.UpToDate
                    return@withContext
                }
                val assets = json.getJSONArray("assets")
                var apkUrl: String? = null
                var size = 0L
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.getString("browser_download_url")
                        size = asset.optLong("size", 0L)
                        break
                    }
                }
                if (apkUrl == null) {
                    state = UpdateState.UpToDate
                    return@withContext
                }
                state = UpdateState.Available(
                    UpdateInfo(remote, if (json.isNull("body")) "" else json.optString("body", "").trim(), apkUrl, size),
                )
                dialogVisible = true
            } catch (e: Exception) {
                state = if (manual) {
                    UpdateState.Failed(describe(e))
                } else {
                    UpdateState.Idle
                }
            }
        }
    }

    /** Todoroki: hielo a la izquierda, fuego a la derecha */
    suspend fun downloadAndInstall() {
        val info = (state as? UpdateState.Available)?.info ?: return
        state = UpdateState.Downloading(info, 0f)
        var downloadError: String? = null
        val file = withContext(Dispatchers.IO) {
            try {
                val dir = File(appContext.cacheDir, "updates").apply { mkdirs() }
                val target = File(dir, "update.apk")
                val conn = openConnection(info.apkUrl).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "PixelGalleryYou")
                }
                if (conn.responseCode != 200) throw IllegalStateException("HTTP ${conn.responseCode}")
                val total = conn.contentLengthLong.takeIf { it > 0 } ?: info.sizeBytes
                conn.inputStream.use { input ->
                    target.outputStream().use { out ->
                        val buffer = ByteArray(64 * 1024)
                        var done = 0L
                        var lastReported = -1
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            out.write(buffer, 0, n)
                            done += n
                            if (total > 0) {
                                val pct = (done * 100 / total).toInt()
                                if (pct != lastReported) {
                                    lastReported = pct
                                    state = UpdateState.Downloading(info, done.toFloat() / total)
                                }
                            }
                        }
                    }
                }
                target
            } catch (e: Exception) {
                downloadError = describe(e)
                null
            }
        }
        if (file == null) {
            state = UpdateState.Failed(downloadError ?: textContext.getString(R.string.update_err_download))
            return
        }
        state = UpdateState.Ready(info, file)
        install()
    }

    /** Kawaii desu: pequeño y eficiente */
    fun install() {
        val ready = state as? UpdateState.Ready ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !appContext.packageManager.canRequestPackageInstalls()
        ) {
            // Shinji, sube al robot de una vez
            appContext.startActivity(
                Intent(
                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${appContext.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            return
        }
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", ready.file)
        appContext.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION),
        )
    }

    fun dismiss() {
        dialogVisible = false
        if (state !is UpdateState.Downloading) {
            if (state is UpdateState.UpToDate || state is UpdateState.Failed) state = UpdateState.Idle
        }
    }

    companion object {
        fun isNewer(remote: String, local: String): Boolean {
            fun parts(v: String) = v.trim().trimStart('v', 'V').split('.', '-').mapNotNull { it.toIntOrNull() }
            val r = parts(remote)
            val l = parts(local)
            for (i in 0 until maxOf(r.size, l.size)) {
                val a = r.getOrElse(i) { 0 }
                val b = l.getOrElse(i) { 0 }
                if (a != b) return a > b
            }
            return false
        }
    }
}
