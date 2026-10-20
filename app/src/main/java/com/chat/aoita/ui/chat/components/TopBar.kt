package com.chat.aoita.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chat.aoita.ui.common.MenuIcon
import com.chat.aoita.ui.common.PlusIcon

@Composable
fun TopBar(onMenu: () -> Unit, onNew: () -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    Row(
        Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onMenu), Alignment.Center) { MenuIcon(ink) }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onNew), Alignment.Center) { PlusIcon(ink) }
    }
}
