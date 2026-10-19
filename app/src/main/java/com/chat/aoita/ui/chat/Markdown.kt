package com.chat.aoita.ui.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ───────────── 解析 ─────────────

sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Bullets(val items: List<String>) : MdBlock
    data class Numbered(val start: Int, val items: List<String>) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class Code(val lang: String, val code: String) : MdBlock
    data object Rule : MdBlock
}

private val headingRe = Regex("^(#{1,6})\\s+(.*)$")
private val ruleRe = Regex("^([-*_])(\\s*\\1){2,}$")
private val bulletRe = Regex("^[-*+]\\s+(.*)$")
private val numberedRe = Regex("^(\\d{1,9})[.)]\\s+(.*)$")

/** 轻量 Markdown 块级解析。对流式输出友好：未闭合的代码块按已有内容显示。 */
fun parseMarkdown(src: String): List<MdBlock> {
    val lines = src.replace("\r\n", "\n").split("\n")
    val out = ArrayList<MdBlock>()
    val para = ArrayList<String>()
    fun flush() {
        if (para.isNotEmpty()) {
            out += MdBlock.Paragraph(para.joinToString("\n"))
            para.clear()
        }
    }

    var i = 0
    while (i < lines.size) {
        val t = lines[i].trim()

        if (t.startsWith("```")) {
            flush()
            val lang = t.removePrefix("```").trim()
            val code = ArrayList<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                code += lines[i]
                i++
            }
            out += MdBlock.Code(lang, code.joinToString("\n"))
            i++ // 跳过结束围栏（未闭合时直接越过末尾）
            continue
        }
        if (t.isEmpty()) {
            flush(); i++; continue
        }
        val h = headingRe.find(t)
        if (h != null) {
            flush()
            out += MdBlock.Heading(h.groupValues[1].length, h.groupValues[2].trim())
            i++; continue
        }
        if (ruleRe.matches(t)) {
            flush(); out += MdBlock.Rule; i++; continue
        }
        if (t.startsWith(">")) {
            flush()
            val q = ArrayList<String>()
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                q += lines[i].trim().removePrefix(">").trim()
                i++
            }
            out += MdBlock.Quote(q.joinToString("\n"))
            continue
        }
        if (bulletRe.matches(t)) {
            flush()
            val items = ArrayList<String>()
            while (i < lines.size) {
                val m = bulletRe.find(lines[i].trim()) ?: break
                items += m.groupValues[1]
                i++
            }
            out += MdBlock.Bullets(items)
            continue
        }
        val num = numberedRe.find(t)
        if (num != null) {
            flush()
            val start = num.groupValues[1].toIntOrNull() ?: 1
            val items = ArrayList<String>()
            while (i < lines.size) {
                val m = numberedRe.find(lines[i].trim()) ?: break
                items += m.groupValues[2]
                i++
            }
            out += MdBlock.Numbered(start, items)
            continue
        }
        para += t
        i++
    }
    flush()
    return out
}

// ───────────── 行内样式 ─────────────

private class InlineColors(val codeBg: Color, val codeFg: Color, val link: Color)

private fun AnnotatedString.Builder.appendInline(s: String, c: InlineColors) {
    var i = 0
    val n = s.length
    while (i < n) {
        val ch = s[i]
        if (ch == '`') {
            val end = s.indexOf('`', i + 1)
            if (end > i + 1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 0.9.em,
                        background = c.codeBg,
                        color = c.codeFg
                    )
                ) { append(s.substring(i + 1, end)) }
                i = end + 1
                continue
            }
        }
        if (ch == '*' && s.startsWith("**", i)) {
            val end = s.indexOf("**", i + 2)
            if (end > i + 2) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    appendInline(s.substring(i + 2, end), c)
                }
                i = end + 2
                continue
            }
        }
        if (ch == '*' && i + 1 < n && !s[i + 1].isWhitespace() && s[i + 1] != '*') {
            val end = s.indexOf('*', i + 1)
            if (end > i + 1 && !s[end - 1].isWhitespace()) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    appendInline(s.substring(i + 1, end), c)
                }
                i = end + 1
                continue
            }
        }
        if (ch == '[') {
            val mid = s.indexOf("](", i + 1)
            if (mid > i) {
                val close = s.indexOf(')', mid + 2)
                if (close > mid) {
                    val label = s.substring(i + 1, mid)
                    val url = s.substring(mid + 2, close).trim()
                    // 只把 http(s) 链接渲染为可点击，其余按普通文本处理
                    if (url.startsWith("https://") || url.startsWith("http://")) {
                        withLink(
                            LinkAnnotation.Url(
                                url,
                                TextLinkStyles(
                                    SpanStyle(color = c.link, textDecoration = TextDecoration.Underline)
                                )
                            )
                        ) { append(label) }
                        i = close + 1
                        continue
                    }
                }
            }
        }
        append(ch)
        i++
    }
}

private fun inlineText(s: String, c: InlineColors): AnnotatedString =
    buildAnnotatedString { appendInline(s, c) }

// ───────────── 渲染 ─────────────

@Composable
fun MarkdownText(text: String, streaming: Boolean, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val body = MaterialTheme.typography.bodyLarge
    val blocks = remember(text) { parseMarkdown(text) }
    val colors = remember(cs) { InlineColors(cs.surfaceVariant, cs.onSurface, cs.primary) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        blocks.forEach { b ->
            when (b) {
                is MdBlock.Heading -> Text(
                    inlineText(b.text, colors),
                    style = if (b.level <= 2) MaterialTheme.typography.titleLarge
                    else body.copy(fontWeight = FontWeight.SemiBold),
                    color = cs.onBackground,
                )

                is MdBlock.Paragraph -> Text(inlineText(b.text, colors), style = body, color = cs.onBackground)

                is MdBlock.Bullets -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    b.items.forEach { item ->
                        Row {
                            Text("•", style = body, color = cs.onSurfaceVariant, modifier = Modifier.width(20.dp).padding(start = 4.dp))
                            Text(inlineText(item, colors), style = body, color = cs.onBackground, modifier = Modifier.weight(1f))
                        }
                    }
                }

                is MdBlock.Numbered -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    b.items.forEachIndexed { idx, item ->
                        Row {
                            Text("${b.start + idx}.", style = body, color = cs.onSurfaceVariant, modifier = Modifier.width(28.dp))
                            Text(inlineText(item, colors), style = body, color = cs.onBackground, modifier = Modifier.weight(1f))
                        }
                    }
                }

                is MdBlock.Quote -> Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(cs.outline)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(inlineText(b.text, colors), style = body, color = cs.onSurfaceVariant)
                }

                is MdBlock.Code -> CodeBlock(b.lang, b.code)

                MdBlock.Rule -> HorizontalDivider(color = cs.outline)
            }
        }
        if (streaming) PulseDot()
    }
}

@Composable
private fun CodeBlock(lang: String, code: String) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }
    Surface(
        color = cs.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(cs.outline.copy(alpha = .3f))
                    .padding(start = 14.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    lang.ifEmpty { "code" },
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { copyToClipboard(context, code); copied = true }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (copied) CheckIcon(cs.primary, 14.dp) else CopyIcon(cs.onSurfaceVariant, 14.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (copied) "已复制" else "复制",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (copied) cs.primary else cs.onSurfaceVariant
                    )
                }
            }
            Box(Modifier.horizontalScroll(rememberScrollState()).padding(14.dp)) {
                Text(
                    code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = cs.onSurface,
                    softWrap = false
                )
            }
        }
    }
}

/** 流式输出时显示在末尾的呼吸圆点。 */
@Composable
private fun PulseDot() {
    val t = rememberInfiniteTransition(label = "pulse")
    val a by t.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        Modifier
            .size(10.dp)
            .graphicsLayer { alpha = a }
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
}
