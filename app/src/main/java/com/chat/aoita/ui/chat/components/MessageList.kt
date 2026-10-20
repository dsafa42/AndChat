package com.chat.aoita.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.chat.aoita.ui.common.fadingEdges
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.chat.aoita.data.model.ChatMessage
import com.chat.aoita.ui.common.ArrowDownIcon
import kotlinx.coroutines.launch

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

private val FadeHeight = 28.dp

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    streamingId: Long?,
    actions: MessageActions,
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val seen = remember { HashSet<Long>() }
    var follow by remember { mutableStateOf(true) }
    val atBottom by remember { derivedStateOf { !state.canScrollForward } }
    val canScrollUp by remember { derivedStateOf { state.canScrollBackward } }
    // 内容滑到顶部 / 底部边缘时渐渐淡出，而不是被生硬截断
    val topFade by animateDpAsState(if (canScrollUp) FadeHeight else 0.dp, label = "topFade")
    val bottomFade by animateDpAsState(if (atBottom) 0.dp else FadeHeight, label = "bottomFade")
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
            modifier = Modifier.fillMaxSize().fadingEdges(topFade, bottomFade),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(messages, key = { it.id }) { m ->
                val animate = remember { seen.add(m.id) }
                Box(Modifier.enterAnimation(animate)) {
                    if (m.fromUser) {
                        UserMessage(m, actions)
                    } else {
                        AssistantMessage(m, streaming = m.id == streamingId, isLast = m.id == messages.last().id, actions = actions)
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
