package com.chat.aoita.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(val fromUser: Boolean, val text: String)

@Composable
fun ChatScreen() {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var replying by remember { mutableStateOf<Job?>(null) }

    fun send(text: String) {
        messages += ChatMessage(true, text)
        messages += ChatMessage(false, "")
        val idx = messages.lastIndex
        // 演示用：模拟流式输出。接入真实后端时替换这里。
        replying = scope.launch {
            val reply = "这是一条演示回复。把这里换成你自己的模型或接口返回的内容即可。"
            for (i in 1..reply.length) {
                messages[idx] = ChatMessage(false, reply.take(i))
                delay(35)
            }
            replying = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.background) {
                DrawerContent(onNewChat = {
                    replying?.cancel(); replying = null
                    messages.clear()
                    scope.launch { drawerState.close() }
                })
            }
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            TopBar(
                onMenu = { scope.launch { drawerState.open() } },
                onNew = { replying?.cancel(); replying = null; messages.clear() }
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) Greeting() else MessageList(messages)
            }
            InputBar(
                busy = replying != null,
                onSend = ::send,
                onStop = { replying?.cancel(); replying = null }
            )
        }
    }
}

@Composable
private fun TopBar(onMenu: () -> Unit, onNew: () -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    Row(
        Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onMenu), Alignment.Center) { MenuIcon(ink) }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onNew), Alignment.Center) { PlusIcon(ink) }
    }
}

@Composable
private fun Greeting() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AccentMark()
        Spacer(Modifier.height(20.dp))
        Text(
            "今天想聊点什么？",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/** 原创的小标记：同心圆点，作为占位品牌图形，可自行替换。 */
@Composable
private fun AccentMark() {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(36.dp)) {
        drawCircle(accent.copy(alpha = .25f), radius = size.minDimension / 2)
        drawCircle(accent, radius = size.minDimension / 4)
    }
}

@Composable
private fun MessageList(messages: List<ChatMessage>) {
    val state = rememberLazyListState()
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) state.animateScrollToItem(messages.lastIndex)
    }
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(messages.size) { i ->
            val m = messages[i]
            if (m.fromUser) UserBubble(m.text) else AssistantText(m.text)
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Box(Modifier.fillMaxWidth(), Alignment.CenterEnd) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun AssistantText(text: String) {
    // 助手回复不加气泡，直接铺开排版
    Text(
        if (text.isEmpty()) "…" else text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    )
}

@Composable
private fun InputBar(busy: Boolean, onSend: (String) -> Unit, onStop: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme
    // 外层负责安全边距：左右留白、避开导航栏和键盘
    Box(
        Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 4.dp)
    ) {
    Surface(
        color = cs.surface,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, cs.outline),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = cs.onSurface)),
                cursorBrush = SolidColor(cs.primary),
                maxLines = 6,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) Text(
                            "和我聊聊",
                            style = MaterialTheme.typography.bodyLarge,
                            color = cs.onSurfaceVariant
                        )
                        inner()
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(cs.surfaceVariant),
                    Alignment.Center
                ) { PlusIcon(cs.onSurface, 18.dp) }
                Spacer(Modifier.weight(1f))
                val canSend = text.isNotBlank()
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (busy || canSend) cs.primary else cs.primary.copy(alpha = .35f))
                        .clickable(enabled = busy || canSend) {
                            if (busy) onStop() else { onSend(text.trim()); text = "" }
                        },
                    Alignment.Center
                ) {
                    if (busy) StopIcon(cs.onPrimary) else ArrowUpIcon(cs.onPrimary)
                }
            }
        }
    }
    }
}

@Composable
private fun DrawerContent(onNewChat: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(20.dp)) {
        Text("Aoita", style = MaterialTheme.typography.titleLarge, color = cs.onBackground)
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onNewChat)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlusIcon(cs.primary)
            Spacer(Modifier.size(12.dp))
            Text("新对话", style = MaterialTheme.typography.titleMedium, color = cs.onBackground)
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = cs.outline)
        Text("最近", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text("（历史对话将显示在这里）", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
    }
}
