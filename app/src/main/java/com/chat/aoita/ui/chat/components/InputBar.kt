package com.chat.aoita.ui.chat.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.chat.aoita.R
import com.chat.aoita.data.model.Attachment
import com.chat.aoita.ui.common.ArrowUpIcon
import com.chat.aoita.ui.common.MIcon
import com.chat.aoita.ui.common.PlusIcon
import com.chat.aoita.ui.common.StopIcon

private enum class ActionMode { Stop, Send, Voice }

/**
 * 输入区：附件预览 + 多行输入框 + 底部工具栏。
 * 右下角按钮随状态切换：生成中=停止；有内容=发送；空=语音输入。
 */
@Composable
fun InputBar(
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    draft: List<Attachment>,
    busy: Boolean,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onPickPhotos: () -> Unit,
    onPickFiles: () -> Unit,
    onRemoveAttachment: (Long) -> Unit,
    onVoice: () -> Unit,
    focus: FocusRequester,
) {
    val cs = MaterialTheme.colorScheme
    val canSend = text.text.isNotBlank() || draft.isNotEmpty()
    val mode = when {
        busy -> ActionMode.Stop
        canSend -> ActionMode.Send
        else -> ActionMode.Voice
    }
    val buttonColor by animateColorAsState(
        if (mode == ActionMode.Voice) cs.surfaceVariant else cs.primary,
        label = "actionButton"
    )
    var menuOpen by remember { mutableStateOf(false) }
    // 横屏矮屏幕时少显示几行，避免输入框占满屏幕
    val maxLines = if (LocalConfiguration.current.screenHeightDp < 480) 3 else 6

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
            // animateContentSize：附件出现 / 输入行数变化时，输入区平滑增高
            Column(Modifier.animateContentSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
                if (draft.isNotEmpty()) {
                    AttachmentStrip(draft, onRemove = onRemoveAttachment)
                    Spacer(Modifier.height(10.dp))
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = cs.onSurface)),
                    cursorBrush = SolidColor(cs.primary),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    minLines = 1,
                    maxLines = maxLines,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus)
                        // 实体键盘：Enter 发送，Shift+Enter 换行（软键盘的回车 deviceId 为负，不拦截）
                        .onPreviewKeyEvent { e ->
                            val hardwareEnter = e.type == KeyEventType.KeyDown && e.key == Key.Enter &&
                                !e.isShiftPressed && e.nativeKeyEvent.deviceId > 0
                            if (hardwareEnter && canSend && !busy) {
                                onSend(text.text.trim())
                                true
                            } else {
                                false
                            }
                        }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    decorationBox = { inner ->
                        Box {
                            if (text.text.isEmpty()) Text(
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
                    Box {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(cs.surfaceVariant).clickable { menuOpen = true },
                            Alignment.Center
                        ) { PlusIcon(cs.onSurface, 18.dp) }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = cs.surface) {
                            DropdownMenuItem(
                                text = { Text("照片") },
                                leadingIcon = { MIcon(R.drawable.ic_image, cs.onSurfaceVariant) },
                                onClick = { menuOpen = false; onPickPhotos() }
                            )
                            DropdownMenuItem(
                                text = { Text("文件") },
                                leadingIcon = { MIcon(R.drawable.ic_description, cs.onSurfaceVariant) },
                                onClick = { menuOpen = false; onPickFiles() }
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(buttonColor)
                            .clickable {
                                when (mode) {
                                    ActionMode.Stop -> onStop()
                                    ActionMode.Send -> onSend(text.text.trim())
                                    ActionMode.Voice -> onVoice()
                                }
                            },
                        Alignment.Center
                    ) {
                        Crossfade(targetState = mode, label = "actionIcon") { m ->
                            when (m) {
                                ActionMode.Stop -> StopIcon(cs.onPrimary)
                                ActionMode.Send -> ArrowUpIcon(cs.onPrimary)
                                ActionMode.Voice -> MIcon(R.drawable.ic_mic, cs.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}
