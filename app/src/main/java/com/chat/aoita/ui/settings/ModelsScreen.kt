package com.chat.aoita.ui.settings

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chat.aoita.data.AppSettings
import com.chat.aoita.data.ModelEntry
import com.chat.aoita.data.SettingsRepository
import com.chat.aoita.data.maskApiKey
import com.chat.aoita.ui.chat.BackIcon
import com.chat.aoita.ui.chat.CheckIcon
import com.chat.aoita.ui.chat.ChevronRightIcon
import com.chat.aoita.ui.chat.PlusIcon
import com.chat.aoita.ui.chat.TrashIcon
import kotlinx.coroutines.launch

/** 模型管理页：列出已添加的模型，右上角 + 号新增。 */
@Composable
fun ModelsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember(context) { SettingsRepository(context.applicationContext) }
    val settings by repo.settings.collectAsState(initial = AppSettings())
    val wide = LocalConfiguration.current.screenWidthDp >= 720
    val scope = rememberCoroutineScope()

    var editing by remember { mutableStateOf<ModelEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ModelEntry?>(null) }

    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
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
            ModelsTopBar(
                onBack = onBack,
                onAdd = {
                    editing = null
                    showEditor = true
                },
            )

            if (settings.models.isEmpty()) {
                EmptyModels()
            } else {
                Spacer(Modifier.height(4.dp))
                settings.models.forEach { m ->
                    ModelCard(
                        entry = m,
                        active = m.id == settings.activeModel?.id,
                        onSelect = {
                            scope.launch { repo.setActiveModel(m.id) }
                        },
                        onClick = {
                            editing = m
                            showEditor = true
                        },
                        onDelete = { pendingDelete = m },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showEditor) {
        ModelEditorSheet(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = { entry ->
                scope.launch { repo.upsertModel(entry) }
                showEditor = false
            },
        )
    }

    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除模型？") },
            text = { Text("将移除「${entry.name.ifBlank { entry.modelId }}」的配置。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repo.deleteModel(entry.id) }
                    pendingDelete = null
                }) { Text("删除", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun ModelsTopBar(onBack: () -> Unit, onAdd: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp)) {
        Box(
            Modifier.align(Alignment.CenterStart).size(44.dp).clip(CircleShape).clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) { BackIcon(cs.onBackground) }
        Text(
            "模型",
            style = MaterialTheme.typography.titleMedium,
            color = cs.onBackground,
            modifier = Modifier.align(Alignment.Center)
        )
        Box(
            Modifier.align(Alignment.CenterEnd).size(44.dp).clip(CircleShape).clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) { PlusIcon(cs.onBackground) }
    }
}

@Composable
private fun EmptyModels() {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(top = 120.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("还没有模型", style = MaterialTheme.typography.titleMedium, color = cs.onBackground)
        Spacer(Modifier.height(8.dp))
        Text(
            "点击右上角 + 添加你的第一个模型",
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant
        )
    }
}

@Composable
private fun ModelCard(
    entry: ModelEntry,
    active: Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surface)
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.name.ifBlank { entry.modelId.ifBlank { "未命名" } },
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (active) {
                    Spacer(Modifier.size(6.dp))
                    CheckIcon(cs.primary, 16.dp)
                }
            }
            Text(
                entry.provider.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (entry.apiKey.isNotBlank()) {
                Text(
                    maskApiKey(entry.apiKey),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(
            Modifier.size(38.dp).clip(CircleShape).clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) { ChevronRightIcon(cs.onSurfaceVariant) }
        Box(
            Modifier.size(38.dp).clip(CircleShape).clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) { TrashIcon(cs.onSurfaceVariant, 18.dp) }
    }
}
