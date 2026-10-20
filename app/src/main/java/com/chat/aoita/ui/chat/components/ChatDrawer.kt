package com.chat.aoita.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chat.aoita.data.model.Conversation
import com.chat.aoita.ui.common.PlusIcon

@Composable
fun DrawerContent(history: List<Conversation>, onNewChat: () -> Unit, onOpen: (Conversation) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Aoita", style = MaterialTheme.typography.titleLarge, color = cs.onBackground)
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onNewChat)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlusIcon(cs.primary)
            Spacer(Modifier.size(12.dp))
            Text("新对话", style = MaterialTheme.typography.titleMedium, color = cs.onBackground)
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = cs.outline)
        Text("最近", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        if (history.isEmpty()) {
            Text("（历史对话将显示在这里）", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        } else {
            LazyColumn {
                items(history, key = { it.id }) { c ->
                    Text(
                        c.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpen(c) }
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
