package com.chat.aoita.data.provider

import com.chat.aoita.data.model.ChatMessage
import javax.inject.Inject
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 聊天后端抽象。以后接 Anthropic / OpenAI 时各写一个实现：
 * 把 history 转成对应请求，流式返回文本增量（delta）。
 * 取消协程即停止生成。
 */
interface ChatProvider {
    fun stream(history: List<ChatMessage>): Flow<String>
}

/** 本地演示实现：不联网，流式吐出一段带 Markdown 的示例回复。 */
class DemoProvider @Inject constructor() : ChatProvider {
    override fun stream(history: List<ChatMessage>): Flow<String> = flow {
        val lastMessage = history.lastOrNull { it.fromUser }
        val last = lastMessage?.text.orEmpty()
        val files = lastMessage?.attachments.orEmpty().joinToString("、") { it.name }
        delay(900) // 模拟“思考中”
        val reply = demoReply(last, files)
        var i = 0
        while (i < reply.length) {
            val end = min(i + (2..5).random(), reply.length)
            emit(reply.substring(i, end))
            i = end
            delay(22)
        }
    }

    private fun demoReply(question: String, files: String) = """
## 这是一条演示回复

你刚才说的是：**$question**
${if (files.isEmpty()) "" else "\n（收到附件：$files）\n"}
下面是支持的 Markdown 效果：

- 行内 `代码`、**加粗**、*斜体*
- 链接：[Anthropic](https://www.anthropic.com)
- 列表项会自动换行对齐，长一点的内容也能正常折行显示

1. 有序列表第一项
2. 有序列表第二项

> 这是一段引用文字。

```kotlin
fun greet(name: String): String {
    return "Hello, ${'$'}name!"
}
```

---

把 `DemoProvider` 换成真实的 Anthropic 或 OpenAI 实现后，这里就是模型的回复。
""".trimIndent()
}
