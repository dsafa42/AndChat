package com.chat.aoita.ui.chat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chat.aoita.R
import com.chat.aoita.data.model.ChatMessage
import com.chat.aoita.ui.common.MIcon
import kotlinx.coroutines.delay

fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.setPrimaryClip(ClipData.newPlainText("Aoita", text))
}

/** 消息底部按钮的回调集合。 */
class MessageActions(
    val regenerate: (Long) -> Unit,
    val quote: (String) -> Unit,
    val edit: (ChatMessage) -> Unit,
    val save: (ChatMessage) -> Unit,
    val branch: (Long) -> Unit,
    val feedback: (Long, Int) -> Unit,
    val speak: (ChatMessage) -> Unit,
    val speakingId: Long?,
)

/** 用户消息：右侧圆角气泡，下方操作栏：复制 / 刷新 / 编辑 / 更多。 */
@Composable
fun UserMessage(m: ChatMessage, actions: MessageActions) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        if (m.attachments.isNotEmpty()) {
            AttachmentStrip(m.attachments, onRemove = null, Modifier.widthIn(max = 300.dp), imageSize = 96.dp)
            if (m.text.isNotBlank()) Spacer(Modifier.height(6.dp))
        }
        if (m.text.isNotBlank()) {
            SelectionContainer {
                Text(
                    m.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = cs.onBackground,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cs.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        ActionRow {
            CopyButton(m.text)
            ActionButton(R.drawable.ic_refresh) { actions.regenerate(m.id) }
            ActionButton(R.drawable.ic_edit) { actions.edit(m) }
            MoreButton(
                MenuEntry(R.drawable.ic_format_quote, "引用") { actions.quote(m.text) },
                MenuEntry(R.drawable.ic_download, "保存") { actions.save(m) },
                MenuEntry(R.drawable.ic_call_split, "分支") { actions.branch(m.id) },
            )
        }
    }
}

/** 助手消息：无气泡的 Markdown 排版，生成完后显示操作栏。 */
@Composable
fun AssistantMessage(m: ChatMessage, streaming: Boolean, isLast: Boolean, actions: MessageActions) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        if (m.text.isEmpty()) {
            if (streaming) ThinkingDots()
            else Text("（已停止）", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            SelectionContainer { MarkdownText(m.text, streaming) }
        }
        if (!streaming) {
            Spacer(Modifier.height(6.dp))
            val speaking = actions.speakingId == m.id
            ActionRow {
                if (m.text.isNotEmpty()) CopyButton(m.text)
                if (isLast) ActionButton(R.drawable.ic_refresh) { actions.regenerate(m.id) }
                if (m.text.isNotEmpty()) {
                    ActionButton(R.drawable.ic_format_quote) { actions.quote(m.text) }
                    ActionButton(R.drawable.ic_edit) { actions.edit(m) }
                    ActionButton(R.drawable.ic_download) { actions.save(m) }
                    ActionButton(
                        if (m.feedback == 1) R.drawable.ic_thumb_up_filled else R.drawable.ic_thumb_up,
                        tint = if (m.feedback == 1) MaterialTheme.colorScheme.primary else null
                    ) { actions.feedback(m.id, if (m.feedback == 1) 0 else 1) }
                    ActionButton(
                        if (m.feedback == -1) R.drawable.ic_thumb_down_filled else R.drawable.ic_thumb_down,
                        tint = if (m.feedback == -1) MaterialTheme.colorScheme.primary else null
                    ) { actions.feedback(m.id, if (m.feedback == -1) 0 else -1) }
                    MoreButton(
                        MenuEntry(
                            if (speaking) R.drawable.ic_volume_off else R.drawable.ic_volume_up,
                            if (speaking) "停止朗读" else "朗读"
                        ) { actions.speak(m) },
                        MenuEntry(R.drawable.ic_call_split, "分支") { actions.branch(m.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) { content() }
}

@Composable
private fun ActionButton(@DrawableRes icon: Int, tint: Color? = null, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onClick), Alignment.Center) {
        MIcon(icon, tint ?: cs.onSurfaceVariant)
    }
}

/** 复制：点击后图标短暂变为对勾。 */
@Composable
private fun CopyButton(text: String) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    ActionButton(
        if (copied) R.drawable.ic_check else R.drawable.ic_content_copy,
        tint = if (copied) MaterialTheme.colorScheme.primary else null
    ) {
        copyToClipboard(context, text)
        copied = true
    }
}

private class MenuEntry(@DrawableRes val icon: Int, val label: String, val onClick: () -> Unit)

@Composable
private fun MoreButton(vararg entries: MenuEntry) {
    val cs = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    Box {
        ActionButton(R.drawable.ic_more_horiz) { open = true }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = cs.surface) {
            entries.forEach { e ->
                DropdownMenuItem(
                    text = { Text(e.label) },
                    leadingIcon = { MIcon(e.icon, cs.onSurfaceVariant) },
                    onClick = { open = false; e.onClick() }
                )
            }
        }
    }
}

/** 编辑消息对话框。 */
@Composable
fun EditDialog(initial: String, confirmLabel: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("编辑消息") },
        text = { OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), maxLines = 10) },
        confirmButton = { TextButton({ onConfirm(text.trim()) }, enabled = text.isNotBlank()) { Text(confirmLabel) } },
        dismissButton = { TextButton(onDismiss) { Text("取消") } }
    )
}

/** “思考中”：三个依次亮起的圆点。 */
@Composable
fun ThinkingDots() {
    val t = rememberInfiniteTransition(label = "dots")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "phase"
    )
    val color = MaterialTheme.colorScheme.primary
    Row(Modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { i ->
            Box(
                Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        val d = ((phase - i) % 3f + 3f) % 3f
                        alpha = 0.25f + 0.75f * (1f - d / 3f)
                    }
                    .background(color, CircleShape)
            )
        }
    }
}
