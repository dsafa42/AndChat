package com.chat.aoita.ui.chat

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.chat.aoita.data.ChatMessage
import com.chat.aoita.data.ChatProvider
import com.chat.aoita.data.DemoProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(provider: ChatProvider = remember { DemoProvider() }) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var replying by remember { mutableStateOf<Job?>(null) }
    var input by remember { mutableStateOf("") }
    var showAttachSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val attachPicker = rememberAttachPicker(
        onPicked = { uri: Uri ->
            // TODO: 将 uri 接入发送流程（上传/引用）。当前仅做回显提示。
            val name = uri.lastPathSegment ?: uri.toString()
            Toast.makeText(context, "已选择：$name", Toast.LENGTH_SHORT).show()
        },
        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
    )

    fun stopReply() {
        replying?.cancel()
        replying = null
    }

    fun startReply() {
        val reply = ChatMessage(fromUser = false, text = "")
        val history = messages.toList()
        messages += reply
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                provider.stream(history).collect { delta ->
                    val i = messages.indexOfFirst { it.id == reply.id }
                    if (i >= 0) messages[i] = messages[i].copy(text = messages[i].text + delta)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val i = messages.indexOfFirst { it.id == reply.id }
                if (i >= 0) {
                    messages[i] = messages[i].copy(text = messages[i].text + "\n\n> ⚠️ 出错：${e.message ?: e.javaClass.simpleName}")
                }
            } finally {
                if (replying === coroutineContext[Job]) replying = null
            }
        }
        replying = job
        job.start()
    }

    fun send(text: String) {
        messages += ChatMessage(fromUser = true, text = text)
        input = ""
        startReply()
    }

    fun regenerate() {
        stopReply()
        while (messages.isNotEmpty() && !messages.last().fromUser) messages.removeAt(messages.lastIndex)
        if (messages.isNotEmpty()) startReply()
    }

    fun setFeedback(id: Long, value: Int) {
        val i = messages.indexOfFirst { it.id == id }
        if (i >= 0) messages[i] = messages[i].copy(feedback = value)
    }

    fun newChat() {
        stopReply()
        messages.clear()
        input = ""
    }

    val streamingId = if (replying != null) messages.lastOrNull()?.takeIf { !it.fromUser }?.id else null

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.background) {
                DrawerContent(onNewChat = {
                    newChat()
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
                onNew = ::newChat
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) {
                    Greeting()
                } else {
                    MessageList(
                        messages = messages,
                        streamingId = streamingId,
                        onRegenerate = ::regenerate,
                        onFeedback = ::setFeedback,
                    )
                }
            }
            InputBar(
                text = input,
                onTextChange = { input = it },
                busy = replying != null,
                onSend = ::send,
                onStop = ::stopReply,
                onAdd = { showAttachSheet = true },
            )
        }
    }

    if (showAttachSheet) {
        AttachmentSheet(
            onDismiss = { showAttachSheet = false },
            onAction = { action ->
                showAttachSheet = false
                when (action) {
                    AttachmentAction.Camera -> attachPicker.onPickCamera()
                    AttachmentAction.Photo -> attachPicker.onPickPhoto()
                    AttachmentAction.File -> attachPicker.onPickFile()
                    // 以下为占位项，待实现
                    AttachmentAction.AddToProject,
                    AttachmentAction.Permission,
                    AttachmentAction.Connector,
                    AttachmentAction.Skills -> Unit
                }
            },
        )
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
private fun Modifier.enterAnimation(enabled: Boolean): Modifier {
    val a = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (enabled) a.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }
    return this.graphicsLayer {
        alpha = a.value
        translationY = (1f - a.value) * 14.dp.toPx()
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    streamingId: Long?,
    onRegenerate: () -> Unit,
    onFeedback: (Long, Int) -> Unit,
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val seen = remember { HashSet<Long>() }
    var follow by remember { mutableStateOf(true) }
    val atBottom by remember { derivedStateOf { !state.canScrollForward } }
    val bottomIndex = messages.size // 末尾占位项的下标，滚到它即滚到最底
    val tailLength = messages.lastOrNull()?.text?.length ?: 0

    // 用户向上滑动 → 停止自动跟随；回到底部 → 恢复跟随
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress && state.lastScrolledBackward }
            .collect { backward -> if (backward) follow = false }
    }
    LaunchedEffect(atBottom) { if (atBottom) follow = true }
    // 新消息：回到跟随状态并滚到底
    LaunchedEffect(messages.size) {
        follow = true
        state.scrollToItem(bottomIndex)
    }
    // 流式输出增长时，若处于跟随状态则保持在底部
    LaunchedEffect(tailLength) {
        if (follow) state.scrollToItem(bottomIndex)
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(messages, key = { it.id }) { m ->
                val animate = remember { seen.add(m.id) }
                Box(Modifier.enterAnimation(animate)) {
                    if (m.fromUser) {
                        UserMessage(m.text)
                    } else {
                        AssistantMessage(
                            text = m.text,
                            streaming = m.id == streamingId,
                            isLast = m.id == messages.last().id,
                            feedback = m.feedback,
                            onRegenerate = onRegenerate,
                            onFeedback = { onFeedback(m.id, it) }
                        )
                    }
                }
            }
            item(key = "bottom") { Spacer(Modifier.height(1.dp)) }
        }
        ScrollToBottomButton(
            visible = !atBottom && !follow,
            onClick = {
                scope.launch {
                    follow = true
                    state.animateScrollToItem(bottomIndex)
                }
            }
        )
    }
}

@Composable
private fun BoxScope.ScrollToBottomButton(visible: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
    ) {
        Surface(
            color = cs.surface,
            shape = CircleShape,
            border = BorderStroke(1.dp, cs.outline),
            shadowElevation = 4.dp,
            modifier = Modifier.size(38.dp).clip(CircleShape).clickable(onClick = onClick)
        ) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { ArrowDownIcon(cs.onSurface, 18.dp) }
        }
    }
}

@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    busy: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onAdd: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val canSend = text.isNotBlank()
    val buttonColor by animateColorAsState(
        if (busy || canSend) cs.primary else cs.primary.copy(alpha = .35f),
        label = "sendButton"
    )
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
                    onValueChange = onTextChange,
                    textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = cs.onSurface)),
                    cursorBrush = SolidColor(cs.primary),
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
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
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cs.surfaceVariant)
                            .clickable(onClick = onAdd),
                        Alignment.Center
                    ) { PlusIcon(cs.onSurface, 18.dp) }
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(buttonColor)
                            .clickable(enabled = busy || canSend) {
                                if (busy) onStop() else onSend(text.trim())
                            },
                        Alignment.Center
                    ) {
                        Crossfade(targetState = busy, label = "sendIcon") { b ->
                            if (b) StopIcon(cs.onPrimary) else ArrowUpIcon(cs.onPrimary)
                        }
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
