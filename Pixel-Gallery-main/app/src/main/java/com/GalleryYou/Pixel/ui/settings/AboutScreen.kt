package com.GalleryYou.Pixel.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.GalleryYou.Pixel.R
import com.GalleryYou.Pixel.appVersion
import com.GalleryYou.Pixel.ui.components.PolygonShape
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val GITHUB_URL = "https://github.com/JGRyuuTB"
private const val GITHUB_AVATAR_URL = "https://github.com/JGRyuuTB.png?size=240"
private const val TELEGRAM_URL = "https://t.me/JG_ryuu"
private const val CHANNEL_URL = "https://t.me/dragonsbastion"

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val channelPrefs = remember { context.getSharedPreferences("about_cache", Context.MODE_PRIVATE) }
    val channelPhoto by produceState(initialValue = channelPrefs.getString("channelPhoto", null)) {
        val fresh = fetchChannelPhoto(CHANNEL_URL)
        if (fresh != null) {
            value = fresh
            channelPrefs.edit().putString("channelPhoto", fresh).apply()
        }
    }

    SettingsDetailScaffold(
        title = stringResource(R.string.settings_about),
        onBack = onBack,
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = scheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(PolygonShape(MaterialShapes.Cookie9Sided))
                                .background(scheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoLibrary,
                                contentDescription = null,
                                tint = scheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.about_tagline),
                                style = MaterialTheme.typography.bodyLarge,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                    Surface(shape = CircleShape, color = scheme.tertiaryContainer) {
                        Text(
                            text = stringResource(R.string.version_stable, appVersion(context)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        InfoChip(Icons.Rounded.Public, stringResource(R.string.about_chip_open_source))
                        InfoChip(Icons.Rounded.AutoAwesome, stringResource(R.string.about_chip_no_ads))
                        InfoChip(Icons.Rounded.Download, stringResource(R.string.about_chip_ota))
                        InfoChip(Icons.Rounded.Palette, "Material 3 Expressive")
                    }
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.about_maintainer)) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = scheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RemoteAvatar(
                            url = GITHUB_AVATAR_URL,
                            size = 64.dp,
                            fallback = {
                                Text(
                                    "JG",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onPrimaryContainer,
                                )
                            },
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "JG Ryuu",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                            )
                            Text(
                                stringResource(R.string.about_role),
                                style = MaterialTheme.typography.titleSmall,
                                color = scheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.about_building),
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LinkButton(Icons.Rounded.Code, "GitHub") { openUrl(context, GITHUB_URL) }
                            LinkButton(Icons.Rounded.Send, "Telegram") { openUrl(context, TELEGRAM_URL) }
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(title = stringResource(R.string.about_community)) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = scheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RemoteAvatar(
                                url = channelPhoto,
                                size = 56.dp,
                                fallback = {
                                    Icon(
                                        Icons.Rounded.Campaign,
                                        contentDescription = null,
                                        tint = scheme.onPrimaryContainer,
                                        modifier = Modifier.size(26.dp),
                                    )
                                },
                            )
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    "Dragons Bastion",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface,
                                )
                                Text(
                                    stringResource(R.string.about_channel_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                        }
                        Button(
                            onClick = { openUrl(context, CHANNEL_URL) },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.about_join_channel))
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = scheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.about_license),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                    )
                    Text(
                        stringResource(R.string.about_credits),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    val scheme = MaterialTheme.colorScheme
    Surface(shape = CircleShape, color = scheme.surfaceContainerHigh) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = scheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = scheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RemoteAvatar(url: String?, size: Dp, fallback: @Composable () -> Unit) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(size)
            .clip(PolygonShape(MaterialShapes.SoftBurst))
            .background(scheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        fallback()
        if (url != null) {
            AsyncImage(
                model = remember(url) { ImageRequest.Builder(context).data(url).crossfade(true).build() },
                imageLoader = context.imageLoader,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun LinkButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = description,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private suspend fun fetchChannelPhoto(pageUrl: String): String? = withContext(Dispatchers.IO) {
    try {
        val conn = (URL(pageUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("User-Agent", "Mozilla/5.0")
        }
        val html = conn.inputStream.bufferedReader().use { it.readText() }
        Regex("""<meta\s+property="og:image"\s+content="([^"]+)"""").find(html)?.groupValues?.get(1)
    } catch (_: Exception) {
        null
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        Toast.makeText(context, R.string.link_open_failed, Toast.LENGTH_SHORT).show()
    }
}
