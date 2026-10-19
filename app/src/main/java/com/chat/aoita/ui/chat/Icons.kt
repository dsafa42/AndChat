package com.chat.aoita.ui.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

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

@Composable
fun CopyIcon(color: Color, size: Dp = 18.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val st = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawRoundRect(
        color,
        topLeft = Offset(w * .34f, w * .34f),
        size = Size(w * .5f, w * .5f),
        cornerRadius = CornerRadius(w * .1f),
        style = st
    )
    val back = Path().apply {
        moveTo(w * .34f, w * .66f)
        lineTo(w * .16f, w * .66f)
        lineTo(w * .16f, w * .16f)
        lineTo(w * .66f, w * .16f)
        lineTo(w * .66f, w * .34f)
    }
    drawPath(back, color, style = st)
}

@Composable
fun CheckIcon(color: Color, size: Dp = 18.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val st = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    val p = Path().apply {
        moveTo(w * .2f, w * .52f)
        lineTo(w * .42f, w * .72f)
        lineTo(w * .8f, w * .3f)
    }
    drawPath(p, color, style = st)
}

@Composable
fun RefreshIcon(color: Color, size: Dp = 18.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.7.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val r = w * .32f
    val cx = w * .5f
    val cy = w * .5f
    drawArc(
        color,
        startAngle = -50f,
        sweepAngle = 290f,
        useCenter = false,
        topLeft = Offset(cx - r, cy - r),
        size = Size(r * 2f, r * 2f),
        style = st
    )
    // 箭头画在圆弧末端（240°），沿顺时针切线方向
    val th = Math.toRadians(240.0)
    val tipX = cx + r * cos(th).toFloat()
    val tipY = cy + r * sin(th).toFloat()
    val dx = (-sin(th)).toFloat()
    val dy = cos(th).toFloat()
    val px = -dy
    val py = dx
    val len = w * .2f
    drawLine(
        color, Offset(tipX, tipY),
        Offset(tipX - dx * len + px * len * .6f, tipY - dy * len + py * len * .6f),
        sw, StrokeCap.Round
    )
    drawLine(
        color, Offset(tipX, tipY),
        Offset(tipX - dx * len - px * len * .6f, tipY - dy * len - py * len * .6f),
        sw, StrokeCap.Round
    )
}
