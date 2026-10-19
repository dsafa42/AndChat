package com.chat.aoita.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CrashScreen(report: String, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxSize()
            .background(cs.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text("上次运行崩溃了（可长按选择复制）", style = MaterialTheme.typography.titleMedium, color = cs.onBackground)
        TextButton(onClick = onDismiss) { Text("清除并进入应用") }
        SelectionContainer(Modifier.weight(1f)) {
            Text(
                report,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = cs.onBackground,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }
}
