package com.chat.aoita.data.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** 系统 TTS 朗读；同一时间只朗读一条消息，`speakingId` 为其 id。首次使用时才初始化引擎。 */
@Singleton
class Speaker @Inject constructor(@ApplicationContext private val context: Context) {
    private val _speakingId = MutableStateFlow<Long?>(null)
    val speakingId: StateFlow<Long?> = _speakingId

    private var engine: TextToSpeech? = null
    private var ready = false
    private var pending: Pair<Long, String>? = null

    fun toggle(id: Long, markdown: String) {
        if (_speakingId.value == id) return stop()
        pending = id to markdown.replace(Regex("[*_`#>~]"), "").replace(Regex("\\[([^]]*)]\\([^)]*\\)"), "$1")
        _speakingId.value = id
        if (engine == null) {
            engine = TextToSpeech(context) { status ->
                ready = status == TextToSpeech.SUCCESS
                if (ready) {
                    engine?.language = Locale.getDefault()
                    engine?.setOnUtteranceProgressListener(listener)
                    speakPending()
                } else {
                    _speakingId.value = null
                }
            }
        } else if (ready) {
            speakPending()
        }
    }

    fun stop() {
        pending = null
        engine?.stop()
        _speakingId.value = null
    }

    private fun speakPending() {
        val (id, text) = pending ?: return
        pending = null
        engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id.toString())
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {}
        override fun onDone(utteranceId: String?) = finished(utteranceId)

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) = finished(utteranceId)

        private fun finished(utteranceId: String?) {
            if (_speakingId.value?.toString() == utteranceId) _speakingId.value = null
        }
    }
}
