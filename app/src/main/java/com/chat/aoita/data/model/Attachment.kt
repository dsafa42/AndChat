package com.chat.aoita.data.model

import android.net.Uri
import java.util.concurrent.atomic.AtomicLong

private val idCounter = AtomicLong(0)

/** 附件只保存来源 Uri 和元数据，不复制文件；发送时再按需流式读取。size 为 -1 表示未知。 */
data class Attachment(
    val id: Long = idCounter.incrementAndGet(),
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val size: Long,
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
}
