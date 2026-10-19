package com.chat.aoita.data

import java.util.concurrent.atomic.AtomicLong

private val idCounter = AtomicLong(0)

/** feedback: 1 = 赞，-1 = 踩，0 = 无 */
data class ChatMessage(
    val id: Long = idCounter.incrementAndGet(),
    val fromUser: Boolean,
    val text: String,
    val feedback: Int = 0,
)
