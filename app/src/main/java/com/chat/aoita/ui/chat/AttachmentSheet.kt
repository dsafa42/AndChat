package com.chat.aoita.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** 「+」面板里可执行的动作。 */
enum class AttachmentAction { Camera, Photo, File, AddToProject, Permission, Connector, Skills }

/**
 * 从底部弹起的「添加到会话」面板：
 * 顶部左上角 X 关闭、标题居中；
 * 三宫格（相机 / 照片 / 文件）为卡片式，图标在上、文字在下，整体居中；
 * 下方为卡片式功能列表。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentSheet(
    onDismiss: () -> Unit,
    onAction: (AttachmentAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = cs.background,
        dragHandle = { SheetHandle() },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 24.dp)
        ) {
            // ---- 顶部栏：X + 居中标题 ----
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "添加到会话",
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onBackground
                )
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) { CloseIcon(cs.onBackground, 20.dp) }
            }

            Spacer(Modifier.height(12.dp))

            // ---- 三宫格卡片 ----
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GridCard("相机", AttachmentAction.Camera, Modifier.weight(1f), onAction) { CameraIcon(it) }
                GridCard("照片", AttachmentAction.Photo, Modifier.weight(1f), onAction) { PhotoIcon(it) }
                GridCard("文件", AttachmentAction.File, Modifier.weight(1f), onAction) { FileIcon(it) }
            }

            Spacer(Modifier.height(10.dp))

            // ---- 卡片式列表 ----
            SheetCardItem(
                title = "添加到项目",
                subtitle = "无",
                action = AttachmentAction.AddToProject,
                onAction = onAction,
            ) { ProjectIcon(it) }
            Spacer(Modifier.height(8.dp))
            SheetCardItem(
                title = "权限",
                subtitle = "手动",
                action = AttachmentAction.Permission,
                onAction = onAction,
            ) { PermissionIcon(it) }
            Spacer(Modifier.height(8.dp))
            SheetCardItem(
                title = "连接器",
                subtitle = null,
                action = AttachmentAction.Connector,
                onAction = onAction,
                trailing = { ConnectorMark(it) },
            ) { ConnectorIcon(it) }
            Spacer(Modifier.height(8.dp))
            SheetCardItem(
                title = "Skills",
                subtitle = null,
                action = AttachmentAction.Skills,
                onAction = onAction,
            ) { SkillsIcon(it) }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

/** 宫格卡片：整块圆角卡，图标圆形底在上、文字在下，内容居中。 */
@Composable
private fun GridCard(
    label: String,
    action: AttachmentAction,
    modifier: Modifier,
    onAction: (AttachmentAction) -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surface)
            .clickable { onAction(action) }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(cs.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { icon(cs.onSurfaceVariant) }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = cs.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 卡片式列表项：左圆形图标 + 标题/副标题 + 右侧尾部（默认箭头）。 */
@Composable
private fun SheetCardItem(
    title: String,
    subtitle: String?,
    action: AttachmentAction,
    onAction: (AttachmentAction) -> Unit,
    trailing: (@Composable (Color) -> Unit)? = null,
    icon: @Composable (Color) -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surface)
            .clickable { onAction(action) }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cs.surfaceVariant),
            contentAlignment = Alignment.Center
        ) { icon(cs.onSurfaceVariant) }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) {
            trailing(cs.primary)
        } else {
            ChevronRightIcon(cs.onSurfaceVariant)
        }
    }
}
