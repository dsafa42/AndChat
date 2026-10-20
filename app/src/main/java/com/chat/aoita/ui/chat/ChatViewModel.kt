package com.chat.aoita.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.aoita.data.model.ChatMessage
import com.chat.aoita.data.repository.ChatRepository
import com.chat.aoita.data.speech.Speaker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository,
    private val speaker: Speaker,
) : ViewModel() {
    private val current = MutableStateFlow(ChatUiState())
    private var job: Job? = null

    val uiState: StateFlow<ChatUiState> = combine(current, repo.conversations, speaker.speakingId) { s, h, speaking ->
        s.copy(history = h, speakingId = speaking)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun onAction(action: ChatAction) {
        if (action is ChatAction.Send || action is ChatAction.Regenerate || action is ChatAction.Edit ||
            action is ChatAction.Branch || action is ChatAction.Open || action == ChatAction.NewChat
        ) speaker.stop()
        when (action) {
            is ChatAction.Send -> {
                if (action.text.isBlank() && current.value.draft.isEmpty()) return
                current.update {
                    it.copy(
                        messages = it.messages + ChatMessage(fromUser = true, text = action.text, attachments = it.draft),
                        draft = emptyList()
                    )
                }
                startReply()
            }
            is ChatAction.AddAttachments -> addAttachments(action.uris)
            is ChatAction.RemoveAttachment -> current.update { s -> s.copy(draft = s.draft.filter { it.id != action.id }) }
            ChatAction.NoticeShown -> current.update { it.copy(notice = null) }
            ChatAction.Stop -> stop()
            is ChatAction.Regenerate -> regenerate(action.id)
            is ChatAction.Edit -> {
                val fromUser = current.value.messages.firstOrNull { it.id == action.id }?.fromUser ?: return
                updateMessage(action.id) { it.copy(text = action.text) }
                if (fromUser) regenerate(action.id)
            }
            is ChatAction.Speak -> current.value.messages.firstOrNull { it.id == action.id }?.let { speaker.toggle(it.id, it.text) }
            is ChatAction.Export -> viewModelScope.launch { repo.export(action.uri, action.text) }
            is ChatAction.Feedback -> updateMessage(action.id) { it.copy(feedback = action.value) }
            is ChatAction.Branch -> {
                val messages = current.value.messages
                val i = messages.indexOfFirst { it.id == action.id }
                if (i >= 0) switchTo(messages.take(i + 1))
            }
            is ChatAction.Open -> repo.take(action.conversationId)?.let { switchTo(it.messages) }
            ChatAction.NewChat -> switchTo(emptyList())
        }
    }

    private fun addAttachments(uris: List<Uri>) {
        viewModelScope.launch {
            for (uri in uris) {
                if (current.value.draft.size >= MAX_DRAFT) {
                    notice("最多添加 $MAX_DRAFT 个附件")
                    break
                }
                try {
                    val attachment = repo.resolveAttachment(uri)
                    current.update { it.copy(draft = it.draft + attachment) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    notice(e.message ?: "无法添加附件")
                }
            }
        }
    }

    private fun notice(text: String) = current.update { it.copy(notice = text) }

    private fun startReply() {
        val reply = ChatMessage(fromUser = false, text = "")
        val history = current.value.messages
        current.update { it.copy(messages = it.messages + reply, streamingId = reply.id) }
        job = viewModelScope.launch {
            try {
                repo.reply(history).collect { delta -> updateMessage(reply.id) { it.copy(text = it.text + delta) } }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                updateMessage(reply.id) { it.copy(text = it.text + "\n\n> ⚠️ 出错：${e.message ?: e.javaClass.simpleName}") }
            } finally {
                current.update { if (it.streamingId == reply.id) it.copy(streamingId = null) else it }
            }
        }
    }

    private fun stop() {
        job?.cancel()
        job = null
        current.update { it.copy(streamingId = null) }
    }

    /** 助手消息：丢弃它并重新生成；用户消息：丢弃其后的内容并重新发送。 */
    private fun regenerate(id: Long) {
        val messages = current.value.messages
        val i = messages.indexOfFirst { it.id == id }
        if (i < 0) return
        stop()
        val kept = messages.take(if (messages[i].fromUser) i + 1 else i)
        current.update { it.copy(messages = kept) }
        if (kept.lastOrNull()?.fromUser == true) startReply()
    }

    /** 当前对话归档到历史，再切换为给定消息列表。 */
    private fun switchTo(messages: List<ChatMessage>) {
        stop()
        current.value.messages.takeIf { it.isNotEmpty() }?.let(repo::archive)
        current.update { it.copy(messages = messages, draft = emptyList()) }
    }

    private fun updateMessage(id: Long, transform: (ChatMessage) -> ChatMessage) {
        current.update { s -> s.copy(messages = s.messages.map { if (it.id == id) transform(it) else it }) }
    }

    private companion object {
        const val MAX_DRAFT = 10
    }
}
