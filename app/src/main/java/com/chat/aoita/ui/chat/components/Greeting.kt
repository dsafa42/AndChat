package com.chat.aoita.ui.chat.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Greeting() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AccentMark()
        Spacer(Modifier.height(20.dp))
        Text(
            "今天想聊点什么？",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun AccentMark() {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(36.dp)) {
        drawCircle(accent.copy(alpha = .25f), radius = size.minDimension / 2)
        drawCircle(accent, radius = size.minDimension / 4)
    }
}
