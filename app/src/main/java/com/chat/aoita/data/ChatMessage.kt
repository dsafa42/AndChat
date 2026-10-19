package com.chat.aoita.data

data class ChatMessage(
    val id: Long,
    val fromUser: Boolean,
    val text: String,
    val streaming: Boolean = false,
    val error: String? = null,
    /** 1 = 赞，-1 = 踩，0 = 无 */
    val feedback: Int = 0,
)
