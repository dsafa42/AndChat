package com.chat.aoita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.chat.aoita.ui.chat.ChatScreen
import com.chat.aoita.ui.theme.AoitaTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val crashFile = File(filesDir, AoitaApp.CRASH_FILE)
        val crash = if (crashFile.exists()) runCatching { crashFile.readText() }.getOrNull() else null
        setContent {
            AoitaTheme {
                var report by remember { mutableStateOf(crash) }
                val current = report
                if (current != null) {
                    CrashScreen(current) { crashFile.delete(); report = null }
                } else {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
private fun CrashScreen(text: String, onDismiss: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("上次运行崩溃了（可长按选择复制）", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        TextButton(onClick = onDismiss) { Text("清除并进入应用") }
        SelectionContainer(Modifier.weight(1f)) {
            Text(
                text,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    AoitaTheme { ChatScreen() }
}
