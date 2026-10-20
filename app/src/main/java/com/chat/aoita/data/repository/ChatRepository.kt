package com.chat.aoita.data.repository

import androidx.lifecycle.ViewModel
import com.chat.aoita.data.model.ChatMessage
import com.chat.aoita.data.model.Conversation
import com.chat.aoita.data.provider.ChatProvider
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.chat.aoita.data.model.Attachment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update

/** 数据统一入口：ViewModel 只依赖它，不直接接触具体的模型接口或存储。 */
interface ChatRepository {
    val conversations: StateFlow<List<Conversation>>

    /** 流式获取回复文本增量，取消收集即停止生成。 */
    fun reply(history: List<ChatMessage>): Flow<String>

    fun archive(messages: List<ChatMessage>)

    /** 取出（并从历史中移除）一条历史对话。 */
    fun take(id: Long): Conversation?

    /** 读取选择器返回的 Uri 的名称 / 类型 / 大小（不复制文件），并尽量申请长期读取权限。 */
    suspend fun resolveAttachment(uri: Uri): Attachment

    /** 把文本写入用户通过系统文件选择器选好的位置。 */
    suspend fun export(uri: Uri, text: String)
}

/** 目前历史只放内存；接入 Room 时只需改这里。 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val provider: ChatProvider,
) : ChatRepository {
    private val nextId = AtomicLong(0)
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    override val conversations: StateFlow<List<Conversation>> = _conversations

    override fun reply(history: List<ChatMessage>): Flow<String> =
        provider.stream(history).flowOn(Dispatchers.IO)

    override fun archive(messages: List<ChatMessage>) {
        val c = Conversation(nextId.incrementAndGet(), messages)
        _conversations.update { listOf(c) + it }
    }

    override fun take(id: Long): Conversation? {
        val c = _conversations.value.firstOrNull { it.id == id } ?: return null
        _conversations.update { it - c }
        return c
    }

    override suspend fun resolveAttachment(uri: Uri): Attachment = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var name = uri.lastPathSegment ?: "file"
        var size = -1L
        resolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = c.getString(it) ?: name }
                c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 && !c.isNull(it) }?.let { size = c.getLong(it) }
            }
        }
        val mime = resolver.getType(uri)
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(name.substringAfterLast('.', "").lowercase())
            ?: "application/octet-stream"
        // 让授权在重启应用后仍有效；不支持持久授权的 Uri 忽略即可
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
        }
        Attachment(uri = uri, name = name, mimeType = mime, size = size)
    }

    override suspend fun export(uri: Uri, text: String) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
        Unit
    }
}
