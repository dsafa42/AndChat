package com.chat.aoita.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chat.aoita.data.ModelEntry
import com.chat.aoita.data.ModelListResult
import com.chat.aoita.data.ModelProvider
import com.chat.aoita.data.fetchModels
import com.chat.aoita.ui.chat.ChevronDownIcon
import com.chat.aoita.ui.chat.RefreshIcon
import com.chat.aoita.ui.chat.SheetHandle
import kotlinx.coroutines.launch

/**
 * 添加 / 编辑模型：底部弹窗。
 * 1) API 地址：可手输，也可从预设服务商选择并自动填充
 * 2) API Key：默认遮盖（前 6 后 4 明文），可切换显示
 * 3) 模型 ID：可手输，也可点刷新自动拉取（不支持该接口的服务商会提示手输）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelEditorSheet(
    initial: ModelEntry?,
    onDismiss: () -> Unit,
    onSave: (ModelEntry) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val entry = remember { initial ?: ModelEntry() }
    var provider by remember { mutableStateOf(entry.provider) }
    var baseUrl by remember { mutableStateOf(entry.baseUrl) }
    var apiKey by remember { mutableStateOf(entry.apiKey) }
    var modelId by remember { mutableStateOf(entry.modelId) }

    var providerMenu by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var hint by remember { mutableStateOf<String?>(null) }
    var fetched by remember { mutableStateOf<List<String>>(emptyList()) }
    var modelMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cs.background,
        dragHandle = { SheetHandle() },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Text(
                if (initial == null) "添加模型" else "编辑模型",
                style = MaterialTheme.typography.titleMedium,
                color = cs.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(20.dp))

            // ---- 1. API 地址（可输入 + 可下拉选预设）----
            FieldLabel("API 地址")
            Box {
                FieldBox {
                    BasicTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.merge(
                            androidx.compose.ui.text.TextStyle(color = cs.onSurface)
                        ),
                        cursorBrush = SolidColor(cs.primary),
                        decorationBox = { inner ->
                            Box {
                                if (baseUrl.isEmpty()) FieldPlaceholder("https://api.example.com/v1")
                                inner()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { providerMenu = true },
                        contentAlignment = Alignment.Center
                    ) { ChevronDownIcon(cs.onSurfaceVariant) }
                }
                DropdownMenu(expanded = providerMenu, onDismissRequest = { providerMenu = false }) {
                    ModelProvider.entries.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.displayName) },
                            onClick = {
                                provider = p
                                if (p.defaultBaseUrl.isNotBlank()) baseUrl = p.defaultBaseUrl
                                providerMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "当前服务商：${provider.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            // ---- 2. API Key（输入时明文；保存后在列表中遮盖显示）----
            FieldLabel("API Key")
            FieldBox {
                BasicTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.merge(
                        androidx.compose.ui.text.TextStyle(color = cs.onSurface)
                    ),
                    cursorBrush = SolidColor(cs.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    decorationBox = { inner ->
                        Box {
                            if (apiKey.isEmpty()) FieldPlaceholder("sk-...")
                            inner()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "保存后自动隐藏中间部分",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
            Text(
                if (provider.supportsModelList) "支持自动获取模型列表"
                else "该服务商不提供模型列表接口，请手动输入模型 ID",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            // ---- 3. 模型 ID（可手输 + 刷新获取）----
            FieldLabel("模型 ID")
            Box {
                FieldBox {
                    BasicTextField(
                        value = modelId,
                        onValueChange = { modelId = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.merge(
                            androidx.compose.ui.text.TextStyle(color = cs.onSurface)
                        ),
                        cursorBrush = SolidColor(cs.primary),
                        decorationBox = { inner ->
                            Box {
                                if (modelId.isEmpty()) FieldPlaceholder("gpt-4o / deepseek-chat")
                                inner()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !loading) {
                                if (!provider.supportsModelList) {
                                    hint = "该服务商不支持自动获取，请手动输入"
                                    return@clickable
                                }
                                hint = null
                                loading = true
                                scope.launch {
                                    when (val r = fetchModels(baseUrl, apiKey)) {
                                        is ModelListResult.Success -> {
                                            fetched = r.models
                                            if (r.models.size == 1) modelId = r.models.first()
                                            else modelMenu = true
                                            hint = "已获取 ${r.models.size} 个模型"
                                        }

                                        is ModelListResult.Failure -> hint = r.message
                                    }
                                    loading = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = cs.primary
                            )
                        } else {
                            RefreshIcon(cs.onSurfaceVariant)
                        }
                    }
                }
                DropdownMenu(
                    expanded = modelMenu,
                    onDismissRequest = { modelMenu = false },
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    fetched.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = {
                                modelId = m
                                modelMenu = false
                            }
                        )
                    }
                }
            }
            hint?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = cs.primary)
            }

            Spacer(Modifier.height(24.dp))

            // ---- 保存 ----
            val canSave = baseUrl.isNotBlank() && apiKey.isNotBlank() && modelId.isNotBlank()
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (canSave) cs.primary else cs.primary.copy(alpha = .35f))
                    .clickable(enabled = canSave) {
                        onSave(
                            entry.copy(
                                provider = provider,
                                baseUrl = baseUrl.trim(),
                                apiKey = apiKey.trim(),
                                modelId = modelId.trim(),
                                name = entry.name.ifBlank { modelId.trim() },
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("保存", style = MaterialTheme.typography.titleMedium, color = cs.onPrimary)
            }
        }
    }
}

/** 字段标签。 */
@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FieldPlaceholder(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun FieldBox(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cs.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}
