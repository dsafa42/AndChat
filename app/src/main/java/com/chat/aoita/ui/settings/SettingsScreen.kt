package com.chat.aoita.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chat.aoita.data.AppSettings
import com.chat.aoita.data.SettingsRepository
import com.chat.aoita.data.ThemeMode
import com.chat.aoita.ui.chat.BackIcon
import com.chat.aoita.ui.chat.ChevronRightIcon
import com.chat.aoita.ui.chat.ChipIcon
import com.chat.aoita.ui.chat.FontSizeIcon
import com.chat.aoita.ui.chat.MoonIcon
import com.chat.aoita.ui.chat.SunIcon
import com.chat.aoita.ui.chat.SystemIcon
import com.chat.aoita.ui.chat.TrashIcon
import kotlinx.coroutines.launch
import java.io.File

/** 设置页：外观 / 字体 / 模型入口 / 数据。 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember(context) { SettingsRepository(context.applicationContext) }
    val settings by repo.settings.collectAsState(initial = AppSettings())
    val scope = rememberCoroutineScope()
    val wide = LocalConfiguration.current.screenWidthDp >= 720

    var showModels by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var cacheText by remember { mutableStateOf(cacheSizeText(context)) }

    if (showModels) {
        ModelsScreen(onBack = { showModels = false })
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier
                .then(if (wide) Modifier.widthIn(max = 760.dp) else Modifier)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsTopBar(title = "设置", onBack = onBack)

            SettingsGroupLabel("外观")
            SettingsCard {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeOption(
                        label = "跟随系统",
                        selected = settings.themeMode == ThemeMode.System,
                        modifier = Modifier.weight(1f),
                        icon = { SystemIcon(it) },
                        onClick = { scope.launch { repo.setThemeMode(ThemeMode.System) } }
                    )
                    ThemeOption(
                        label = "浅色",
                        selected = settings.themeMode == ThemeMode.Light,
                        modifier = Modifier.weight(1f),
                        icon = { SunIcon(it) },
                        onClick = { scope.launch { repo.setThemeMode(ThemeMode.Light) } }
                    )
                    ThemeOption(
                        label = "深色",
                        selected = settings.themeMode == ThemeMode.Dark,
                        modifier = Modifier.weight(1f),
                        icon = { MoonIcon(it) },
                        onClick = { scope.launch { repo.setThemeMode(ThemeMode.Dark) } }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            SettingsGroupLabel("字体大小")
            SettingsCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FontSizeIcon(MaterialTheme.colorScheme.onSurfaceVariant, 20.dp, minus = true)
                        Spacer(Modifier.size(12.dp))
                        Text(
                            "字号 %.0f%%".format(settings.fontScale * 100),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        FontSizeIcon(MaterialTheme.colorScheme.onSurfaceVariant, 20.dp, minus = false)
                    }
                    Slider(
                        value = settings.fontScale,
                        onValueChange = { scope.launch { repo.setFontScale(it) } },
                        valueRange = 0.8f..1.4f,
                        steps = 5,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            SettingsGroupLabel("模型")
            SettingsCard {
                SettingsRow(
                    title = "模型",
                    subtitle = settings.activeModel?.let {
                        it.name.ifBlank { it.modelId.ifBlank { "未配置" } }
                    } ?: "未配置",
                    leading = { ChipIcon(it) },
                    onMore = { showModels = true },
                )
            }

            Spacer(Modifier.height(8.dp))
            SettingsGroupLabel("数据")
            SettingsCard {
                SettingsRow(
                    title = "清除缓存",
                    subtitle = cacheText,
                    onMore = {
                        context.cacheDir?.deleteRecursively()
                        cacheText = cacheSizeText(context)
                    }
                )
                SettingsDivider()
                SettingsRow(
                    title = "清除所有数据",
                    subtitle = "恢复默认设置并删除本地数据",
                    onMore = { showClearConfirm = true },
                    trailingIcon = { TrashIcon(it) },
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清除所有数据？") },
            text = { Text("将恢复默认设置，并删除本机保存的模型与 API 配置。此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repo.clearAll() }
                    showClearConfirm = false
                }) { Text("清除", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun SettingsTopBar(title: String, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp)) {
        Box(
            Modifier.align(Alignment.CenterStart).size(44.dp).clip(CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) { BackIcon(cs.onBackground) }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = cs.onBackground,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) { content() }
}

@Composable
private fun SettingsDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) cs.surfaceVariant else cs.background)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon(if (selected) cs.primary else cs.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) cs.primary else cs.onSurface
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onMore: () -> Unit,
    leading: (@Composable (Color) -> Unit)? = null,
    trailingIcon: (@Composable (Color) -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onMore)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading(cs.onSurfaceVariant)
            Spacer(Modifier.size(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailingIcon != null) trailingIcon(cs.onSurfaceVariant) else ChevronRightIcon(cs.onSurfaceVariant)
    }
}

private fun cacheSizeText(context: Context): String {
    val size = runCatching {
        File(context.cacheDir?.absolutePath ?: return@runCatching 0L).walkBottomUp()
            .filter { it.isFile }.sumOf { it.length() }
    }.getOrDefault(0L)
    return when {
        size >= 1_000_000 -> "%.1f MB".format(size / 1_000_000.0)
        size >= 1_000 -> "%.0f KB".format(size / 1_000.0)
        else -> "$size B"
    }
}
