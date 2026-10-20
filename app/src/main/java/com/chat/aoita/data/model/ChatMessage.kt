package com.chat.aoita.data.model

import java.util.concurrent.atomic.AtomicLong

private val idCounter = AtomicLong(0)

/** feedback: 1 = 赞，-1 = 踩，0 = 无 */
data class ChatMessage(
    val id: Long = idCounter.incrementAndGet(),
    val fromUser: Boolean,
    val text: String,
    val feedback: Int = 0,
    val attachments: List<Attachment> = emptyList(),
)

/** 已归档的历史对话（id 由仓库分配，与消息 id 无关）。 */
class Conversation(val id: Long, val messages: List<ChatMessage>) {
    val title = messages.firstOrNull { it.fromUser }?.text?.lineSequence()?.firstOrNull()?.take(30) ?: "新对话"
}
