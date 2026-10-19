package com.chat.aoita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.chat.aoita.ui.CrashScreen
import com.chat.aoita.ui.chat.ChatScreen
import com.chat.aoita.ui.chat.ChatViewModel
import com.chat.aoita.ui.theme.AoitaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val vm = ViewModelProvider(this)[ChatViewModel::class.java]
        val crashFile = AoitaApp.crashFile(this)
        val lastCrash = if (crashFile.exists()) runCatching { crashFile.readText() }.getOrNull() else null

        setContent {
            AoitaTheme {
                var report by remember { mutableStateOf(lastCrash) }
                val current = report
                if (current != null) {
                    CrashScreen(current) {
                        crashFile.delete()
                        report = null
                    }
                } else {
                    ChatScreen(vm)
                }
            }
        }
    }
}
