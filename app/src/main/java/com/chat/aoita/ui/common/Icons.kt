package com.chat.aoita.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 用 Canvas 手绘的简单图标，避免额外依赖 material-icons-extended。

@Composable
fun MenuIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 2.dp.toPx()
    drawLine(color, Offset(w * .15f, w * .32f), Offset(w * .85f, w * .32f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .15f, w * .68f), Offset(w * .6f, w * .68f), sw, StrokeCap.Round)
}

@Composable
fun PlusIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 2.dp.toPx()
    drawLine(color, Offset(w * .5f, w * .2f), Offset(w * .5f, w * .8f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .2f, w * .5f), Offset(w * .8f, w * .5f), sw, StrokeCap.Round)
}

@Composable
fun ArrowUpIcon(color: Color, size: Dp = 20.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 2.2.dp.toPx()
    drawLine(color, Offset(w * .5f, w * .82f), Offset(w * .5f, w * .2f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .22f, w * .46f), Offset(w * .5f, w * .18f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .78f, w * .46f), Offset(w * .5f, w * .18f), sw, StrokeCap.Round)
}

@Composable
fun ArrowDownIcon(color: Color, size: Dp = 20.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 2.2.dp.toPx()
    drawLine(color, Offset(w * .5f, w * .18f), Offset(w * .5f, w * .8f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .22f, w * .54f), Offset(w * .5f, w * .82f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .78f, w * .54f), Offset(w * .5f, w * .82f), sw, StrokeCap.Round)
}

@Composable
fun StopIcon(color: Color, size: Dp = 20.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    drawRoundRect(
        color,
        topLeft = Offset(w * .3f, w * .3f),
        size = Size(w * .4f, w * .4f),
        cornerRadius = CornerRadius(2.dp.toPx())
    )
}

/** 加载 res/drawable 里的 Material Symbols 矢量图标并着色。 */
@Composable
fun MIcon(@DrawableRes id: Int, tint: Color, size: Dp = 20.dp, description: String? = null) {
    Icon(painterResource(id), description, Modifier.size(size), tint)
}
