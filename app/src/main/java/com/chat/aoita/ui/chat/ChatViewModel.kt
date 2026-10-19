package com.chat.aoita.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.aoita.data.ChatMessage
import com.chat.aoita.data.ChatProvider
import com.chat.aoita.data.DemoProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** 对话状态和操作。放在 ViewModel 里，转屏等配置变更时对话不会丢。 */
class ChatViewModel : ViewModel() {
    /** 接入 Anthropic / OpenAI 时，替换成对应的 ChatProvider 实现。 */
    var provider: ChatProvider = DemoProvider()

    val messages = mutableStateListOf<ChatMessage>()

    var busy by mutableStateOf(false)
        private set

    /** 已播放过入场动画的消息 id，避免滚动或转屏后重复播放。 */
    val animatedIds = HashSet<Long>()

    private var nextId = 1L
    private var job: Job? = null

    fun send(text: String) {
        if (busy) return
        messages += ChatMessage(nextId++, fromUser = true, text = text)
        generate()
    }

    /** 停止生成，已生成的部分保留。 */
    fun stop() {
        job?.cancel()
    }

    fun newChat() {
        cancelGeneration()
        messages.clear()
    }

    /** 重新生成最后一条助手回复。 */
    fun regenerate() {
        if (busy) return
        while (messages.lastOrNull()?.fromUser == false) messages.removeAt(messages.lastIndex)
        if (messages.isNotEmpty()) generate()
    }

    /** 取出某条用户消息的文本，并丢弃它及其之后的所有内容，供输入框重新编辑后发送。 */
    fun takeForEdit(id: Long): String? {
        val index = messages.indexOfFirst { it.id == id && it.fromUser }
        if (index < 0) return null
        cancelGeneration()
        val text = messages[index].text
        while (messages.size > index) messages.removeAt(messages.lastIndex)
        return text
    }

    /** 再次点同一个评价会取消评价。 */
    fun setFeedback(id: Long, value: Int) {
        update(id) { it.copy(feedback = if (it.feedback == value) 0 else value) }
    }

    private fun cancelGeneration() {
        job?.cancel()
        job = null
        busy = false
    }

    private fun update(id: Long, transform: (ChatMessage) -> ChatMessage) {
        val i = messages.indexOfFirst { it.id == id }
        if (i >= 0) messages[i] = transform(messages[i])
    }

    private fun generate() {
        val history = messages.toList()
        val id = nextId++
        messages += ChatMessage(id, fromUser = false, text = "", streaming = true)
        busy = true

        // 先 LAZY 创建、赋值给 job 再启动，保证 finally 里能正确识别“自己”
        val self = viewModelScope.launch(start = CoroutineStart.LAZY) {
            try {
                provider.stream(history).collect { chunk ->
                    update(id) { it.copy(text = it.text + chunk) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                update(id) { it.copy(error = e.message ?: e.javaClass.simpleName) }
            } finally {
                val current = messages.firstOrNull { it.id == id }
                when {
                    current == null -> Unit
                    current.text.isEmpty() && current.error == null -> messages.removeAll { it.id == id }
                    else -> update(id) { it.copy(streaming = false) }
                }
                if (job === coroutineContext[Job]) {
                    job = null
                    busy = false
                }
            }
        }
        job = self
        self.start()
    }
}
