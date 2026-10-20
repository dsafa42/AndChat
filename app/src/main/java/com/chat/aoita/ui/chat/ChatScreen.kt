package com.chat.aoita.ui.chat

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chat.aoita.ui.chat.components.DrawerContent
import com.chat.aoita.ui.chat.components.EditDialog
import com.chat.aoita.ui.chat.components.Greeting
import com.chat.aoita.ui.chat.components.InputBar
import com.chat.aoita.ui.chat.components.MessageActions
import com.chat.aoita.ui.chat.components.MessageList
import com.chat.aoita.ui.chat.components.TopBar
import kotlinx.coroutines.launch

private val ExpandedWidth = 840.dp // 达到此宽度时抽屉常驻
private val ContentMaxWidth = 760.dp // 聊天区最大宽度
private val DrawerWidth = 300.dp
private const val MAX_PICK = 10 // 单次最多选择的图片数

/** 有状态入口：从 ViewModel 取状态、把操作交回去。 */
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatContent(state, viewModel::onAction)
}

/** 无状态界面：只负责渲染 state，并把用户操作作为 ChatAction 抛出。 */
@Composable
fun ChatContent(state: ChatUiState, onAction: (ChatAction) -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focus = remember { FocusRequester() }
    // 以下是纯界面状态，不进 ViewModel：输入草稿、正在编辑的消息、待保存的文本
    var input by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val saving = remember { arrayOfNulls<String>(1) }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        val text = saving[0]
        if (uri != null && text != null) onAction(ChatAction.Export(uri, text))
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(MAX_PICK)) {
        onAction(ChatAction.AddAttachments(it))
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
        onAction(ChatAction.AddAttachments(it))
    }

    fun insertAtCursor(s: String) {
        val sel = input.selection
        input = TextFieldValue(input.text.replaceRange(sel.min, sel.max, s), TextRange(sel.min + s.length))
        focus.requestFocus()
    }

    // 语音输入：调起系统语音识别界面，识别结果插入到光标处
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let(::insertAtCursor)
    }
    fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话")
        try {
            voiceLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "当前设备不支持语音输入", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.notice) {
        state.notice?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onAction(ChatAction.NoticeShown)
        }
    }

    fun dispatch(action: ChatAction) {
        when (action) {
            is ChatAction.Send, ChatAction.NewChat, is ChatAction.Branch, is ChatAction.Open -> input = TextFieldValue()
            else -> Unit
        }
        onAction(action)
    }

    fun quote(text: String) {
        val merged = text.lines().joinToString("\n") { "> $it" } + "\n\n" + input.text
        input = TextFieldValue(merged, TextRange(merged.length))
        focus.requestFocus()
    }

    val actions = MessageActions(
        regenerate = { dispatch(ChatAction.Regenerate(it)) },
        quote = ::quote,
        edit = { editingId = it.id },
        save = { saving[0] = it.text; saveLauncher.launch("aoita-message.md") },
        branch = { dispatch(ChatAction.Branch(it)) },
        feedback = { id, v -> dispatch(ChatAction.Feedback(id, v)) },
        speak = { dispatch(ChatAction.Speak(it.id)) },
        speakingId = state.speakingId,
    )

    val cs = MaterialTheme.colorScheme
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(cs.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        // 宽屏（平板 / 横屏大屏）：抽屉常驻；窄屏：抽屉可滑出
        val expanded = maxWidth >= ExpandedWidth
        val drawer: @Composable () -> Unit = {
            DrawerContent(
                history = state.history,
                onNewChat = { dispatch(ChatAction.NewChat); scope.launch { drawerState.close() } },
                onOpen = { dispatch(ChatAction.Open(it.id)); scope.launch { drawerState.close() } }
            )
        }
        val chat: @Composable () -> Unit = {
            Column(
                Modifier.fillMaxSize().statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 宽屏时抽屉里已有“新对话”，不再显示顶栏
                if (!expanded) {
                    TopBar(
                        onMenu = { scope.launch { drawerState.open() } },
                        onNew = { dispatch(ChatAction.NewChat) }
                    )
                }
                // 聊天区居中并限制最大宽度
                Column(Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth().weight(1f)) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        if (state.messages.isEmpty()) {
                            Greeting()
                        } else {
                            MessageList(state.messages, state.streamingId, actions)
                        }
                    }
                    InputBar(
                        text = input,
                        onTextChange = { input = it },
                        draft = state.draft,
                        busy = state.streamingId != null,
                        onSend = { dispatch(ChatAction.Send(it)) },
                        onStop = { dispatch(ChatAction.Stop) },
                        onPickPhotos = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onPickFiles = { filePicker.launch(arrayOf("*/*")) },
                        onRemoveAttachment = { dispatch(ChatAction.RemoveAttachment(it)) },
                        onVoice = ::startVoice,
                        focus = focus
                    )
                }
            }
        }
        if (expanded) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        Modifier.width(DrawerWidth),
                        drawerContainerColor = cs.background,
                        windowInsets = WindowInsets.statusBars
                    ) { drawer() }
                    VerticalDivider(color = cs.outline)
                },
                content = chat
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor = cs.background, windowInsets = WindowInsets.statusBars) { drawer() }
                },
                content = chat
            )
        }
    }

    state.messages.firstOrNull { it.id == editingId }?.let { m ->
        EditDialog(
            initial = m.text,
            confirmLabel = if (m.fromUser) "发送" else "保存",
            onConfirm = { dispatch(ChatAction.Edit(m.id, it)); editingId = null },
            onDismiss = { editingId = null }
        )
    }
}
