package com.chat.aoita.ui.chat.components

import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chat.aoita.R
import com.chat.aoita.data.model.Attachment
import com.chat.aoita.ui.common.MIcon
import com.chat.aoita.ui.common.rememberThumbnail

/** 一排附件：图片显示缩略图，其他文件显示文件名和大小。onRemove 为 null 时只读（消息气泡里）。 */
@Composable
fun AttachmentStrip(
    items: List<Attachment>,
    onRemove: ((Long) -> Unit)?,
    modifier: Modifier = Modifier,
    imageSize: Dp = 64.dp,
) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { a ->
            if (a.isImage) ImageTile(a, imageSize, onRemove) else FileTile(a, onRemove)
        }
    }
}

@Composable
private fun ImageTile(a: Attachment, size: Dp, onRemove: ((Long) -> Unit)?) {
    val bitmap = rememberThumbnail(a.uri, maxSizePx = 360)
    Box(Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        bitmap?.let { Image(it, a.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        if (onRemove != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = .55f))
                    .clickable { onRemove(a.id) },
                Alignment.Center
            ) { MIcon(R.drawable.ic_close, Color.White, 12.dp) }
        }
    }
}

@Composable
private fun FileTile(a: Attachment, onRemove: ((Long) -> Unit)?) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    Row(
        Modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cs.surfaceVariant)
            .padding(start = 10.dp, end = if (onRemove != null) 4.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MIcon(R.drawable.ic_description, cs.onSurfaceVariant, 22.dp)
        Spacer(Modifier.size(8.dp))
        Column(Modifier.widthIn(max = 140.dp)) {
            Text(
                a.name,
                style = MaterialTheme.typography.labelLarge,
                color = cs.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (a.size >= 0) Text(
                Formatter.formatShortFileSize(context, a.size),
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant
            )
        }
        if (onRemove != null) {
            Box(Modifier.size(28.dp).clip(CircleShape).clickable { onRemove(a.id) }, Alignment.Center) {
                MIcon(R.drawable.ic_close, cs.onSurfaceVariant, 16.dp)
            }
        }
    }
}
