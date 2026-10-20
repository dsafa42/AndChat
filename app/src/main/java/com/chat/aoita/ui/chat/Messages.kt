package com.chat.aoita.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.setPrimaryClip(ClipData.newPlainText("Aoita", text))
}

/** 用户消息：右侧圆角气泡，文本可选中，用系统浮动工具栏（与助手消息一致）。 */
@Composable
fun UserMessage(text: String) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        SelectionContainer {
            Text(
                text,
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
}

/** 助手消息：无气泡的 Markdown 排版，生成完后显示操作栏。 */
@Composable
fun AssistantMessage(
    text: String,
    streaming: Boolean,
    isLast: Boolean,
    feedback: Int,
    onRegenerate: () -> Unit,
    onFeedback: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        if (text.isEmpty()) {
            if (streaming) ThinkingDots()
            else Text("（已停止）", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            SelectionContainer { MarkdownText(text, streaming) }
        }
        if (!streaming) {
            Spacer(Modifier.height(6.dp))
            ActionRow(text, isLast, feedback, onRegenerate, onFeedback)
        }
    }
}

@Composable
private fun ActionRow(
    text: String,
    isLast: Boolean,
    feedback: Int,
    onRegenerate: () -> Unit,
    onFeedback: (Int) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        if (text.isNotEmpty()) {
            ActionButton(onClick = { copyToClipboard(context, text); copied = true }) {
                if (copied) CheckIcon(cs.primary) else CopyIcon(cs.onSurfaceVariant)
            }
        }
        if (isLast) {
            ActionButton(onClick = onRegenerate) { RefreshIcon(cs.onSurfaceVariant) }
        }
        if (text.isNotEmpty()) {
            ActionButton(onClick = { onFeedback(if (feedback == 1) 0 else 1) }, selected = feedback == 1) {
                Text("👍", fontSize = 15.sp, modifier = Modifier.alpha(if (feedback == 1) 1f else .55f))
            }
            ActionButton(onClick = { onFeedback(if (feedback == -1) 0 else -1) }, selected = feedback == -1) {
                Text("👎", fontSize = 15.sp, modifier = Modifier.alpha(if (feedback == -1) 1f else .55f))
            }
        }
    }
}

@Composable
private fun ActionButton(onClick: () -> Unit, selected: Boolean = false, content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (selected) cs.surfaceVariant else Color.Transparent)
            .clickable(onClick = onClick),
        Alignment.Center
    ) { content() }
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
