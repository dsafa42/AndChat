package com.chat.aoita.ui.chat

import android.net.Uri
import com.chat.aoita.data.model.Attachment
import com.chat.aoita.data.model.ChatMessage
import com.chat.aoita.data.model.Conversation

/** 界面唯一的状态来源（单向数据流：状态向下，ChatAction 向上）。 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val streamingId: Long? = null,
    val speakingId: Long? = null,
    /** 输入框里待发送的附件。 */
    val draft: List<Attachment> = emptyList(),
    /** 一次性提示（如附件过大），界面显示后发 NoticeShown 清掉。 */
    val notice: String? = null,
    val history: List<Conversation> = emptyList(),
)

sealed interface ChatAction {
    data class Send(val text: String) : ChatAction
    data object Stop : ChatAction
    data class Regenerate(val id: Long) : ChatAction
    data class Edit(val id: Long, val text: String) : ChatAction
    data class Feedback(val id: Long, val value: Int) : ChatAction
    data class AddAttachments(val uris: List<Uri>) : ChatAction
    data class RemoveAttachment(val id: Long) : ChatAction
    data object NoticeShown : ChatAction
    data class Speak(val id: Long) : ChatAction
    data class Export(val uri: Uri, val text: String) : ChatAction
    data class Branch(val id: Long) : ChatAction
    data class Open(val conversationId: Long) : ChatAction
    data object NewChat : ChatAction
}
