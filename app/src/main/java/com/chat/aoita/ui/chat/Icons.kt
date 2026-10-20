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

// ---- 输入框「+」面板用到的图标（保持 Canvas 手绘，零依赖）----

/** 相机：机身 + 镜头。 */
@Composable
fun CameraIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // 机身
    drawRoundRect(
        color,
        topLeft = Offset(w * .12f, w * .3f),
        size = Size(w * .76f, w * .52f),
        cornerRadius = CornerRadius(w * .1f),
        style = st
    )
    // 顶部取景突起
    val top = Path().apply {
        moveTo(w * .34f, w * .3f)
        lineTo(w * .4f, w * .18f)
        lineTo(w * .6f, w * .18f)
        lineTo(w * .66f, w * .3f)
    }
    drawPath(top, color, style = st)
    // 镜头
    drawCircle(color, radius = w * .16f, center = Offset(w * .5f, w * .56f), style = st)
}

/** 图片：相框 + 小太阳 + 山形。 */
@Composable
fun ImageIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawRoundRect(
        color,
        topLeft = Offset(w * .14f, w * .18f),
        size = Size(w * .72f, w * .64f),
        cornerRadius = CornerRadius(w * .1f),
        style = st
    )
    drawCircle(color, radius = w * .06f, center = Offset(w * .34f, w * .36f))
    val hill = Path().apply {
        moveTo(w * .18f, w * .74f)
        lineTo(w * .4f, w * .5f)
        lineTo(w * .54f, w * .64f)
        lineTo(w * .64f, w * .54f)
        lineTo(w * .82f, w * .74f)
    }
    drawPath(hill, color, style = st)
}

/** 文件：带折角的文档。 */
@Composable
fun FileIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val doc = Path().apply {
        moveTo(w * .24f, w * .14f)
        lineTo(w * .58f, w * .14f)
        lineTo(w * .76f, w * .32f)
        lineTo(w * .76f, w * .86f)
        lineTo(w * .24f, w * .86f)
        close()
    }
    drawPath(doc, color, style = st)
    // 折角
    val fold = Path().apply {
        moveTo(w * .58f, w * .14f)
        lineTo(w * .58f, w * .32f)
        lineTo(w * .76f, w * .32f)
    }
    drawPath(fold, color, style = st)
    // 文本线
    drawLine(color, Offset(w * .33f, w * .52f), Offset(w * .67f, w * .52f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .33f, w * .68f), Offset(w * .58f, w * .68f), sw, StrokeCap.Round)
}

/** 连接器：两个互扣的环。 */
@Composable
fun ConnectorIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawCircle(color, radius = w * .2f, center = Offset(w * .36f, w * .5f), style = st)
    drawCircle(color, radius = w * .2f, center = Offset(w * .64f, w * .5f), style = st)
}

/** 技能：闪电。 */
@Composable
fun SkillIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val bolt = Path().apply {
        moveTo(w * .56f, w * .12f)
        lineTo(w * .3f, w * .54f)
        lineTo(w * .48f, w * .54f)
        lineTo(w * .42f, w * .88f)
        lineTo(w * .7f, w * .44f)
        lineTo(w * .52f, w * .44f)
        close()
    }
    drawPath(bolt, color, style = st)
}

/** 右箭头：列表项尾部指示。 */
@Composable
fun ChevronRightIcon(color: Color, size: Dp = 18.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.7.dp.toPx()
    drawLine(color, Offset(w * .38f, w * .26f), Offset(w * .62f, w * .5f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .62f, w * .5f), Offset(w * .38f, w * .74f), sw, StrokeCap.Round)
}

// ---- 「添加到会话」面板图标 ----

/** 关闭：叉号。 */
@Composable
fun CloseIcon(color: Color, size: Dp = 20.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.8.dp.toPx()
    drawLine(color, Offset(w * .26f, w * .26f), Offset(w * .74f, w * .74f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .74f, w * .26f), Offset(w * .26f, w * .74f), sw, StrokeCap.Round)
}

/** 照片：与 ImageIcon 同形，保留独立命名以对应「照片」语义。 */
@Composable
fun PhotoIcon(color: Color, size: Dp = 22.dp) = ImageIcon(color, size)

/** 项目：顶盖 + 分层抽屉。 */
@Composable
fun ProjectIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    // 顶盖
    drawLine(color, Offset(w * .14f, w * .32f), Offset(w * .86f, w * .32f), sw, StrokeCap.Round)
    drawRoundRect(
        color,
        topLeft = Offset(w * .16f, w * .16f),
        size = Size(w * .68f, w * .16f),
        cornerRadius = CornerRadius(w * .04f),
        style = st
    )
    // 抽屉体
    drawRoundRect(
        color,
        topLeft = Offset(w * .16f, w * .36f),
        size = Size(w * .68f, w * .46f),
        cornerRadius = CornerRadius(w * .06f),
        style = st
    )
    drawLine(color, Offset(w * .16f, w * .58f), Offset(w * .84f, w * .58f), sw, StrokeCap.Round)
}

/** 权限：手掌。 */
@Composable
fun PermissionIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val hand = Path().apply {
        moveTo(w * .36f, w * .84f)
        lineTo(w * .24f, w * .6f)
        lineTo(w * .3f, w * .54f)
        lineTo(w * .4f, w * .64f)
        lineTo(w * .4f, w * .26f)
        lineTo(w * .48f, w * .26f)
        lineTo(w * .48f, w * .5f)
        lineTo(w * .56f, w * .5f)
        lineTo(w * .56f, w * .22f)
        lineTo(w * .64f, w * .22f)
        lineTo(w * .64f, w * .5f)
    }
    drawPath(hand, color, style = st)
    val thumb = Path().apply {
        moveTo(w * .64f, w * .5f)
        lineTo(w * .74f, w * .4f)
        lineTo(w * .8f, w * .46f)
        lineTo(w * .72f, w * .6f)
        lineTo(w * .7f, w * .84f)
        lineTo(w * .36f, w * .84f)
    }
    drawPath(thumb, color, style = st)
}

/** Skills：文档 + 折角（与 FileIcon 区分：更紧凑、带内框）。 */
@Composable
fun SkillsIcon(color: Color, size: Dp = 22.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val doc = Path().apply {
        moveTo(w * .22f, w * .18f)
        lineTo(w * .6f, w * .18f)
        lineTo(w * .78f, w * .36f)
        lineTo(w * .78f, w * .82f)
        lineTo(w * .22f, w * .82f)
        close()
    }
    drawPath(doc, color, style = st)
    val fold = Path().apply {
        moveTo(w * .6f, w * .18f)
        lineTo(w * .6f, w * .36f)
        lineTo(w * .78f, w * .36f)
    }
    drawPath(fold, color, style = st)
    drawLine(color, Offset(w * .34f, w * .54f), Offset(w * .66f, w * .54f), sw, StrokeCap.Round)
    drawLine(color, Offset(w * .34f, w * .68f), Offset(w * .56f, w * .68f), sw, StrokeCap.Round)
}

/** 连接器右侧标记：一个小圆角方块中的短线。 */
@Composable
fun ConnectorMark(color: Color, size: Dp = 20.dp) = Canvas(Modifier.size(size)) {
    val w = this.size.width
    val sw = 1.6.dp.toPx()
    val st = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
    drawRoundRect(
        color,
        topLeft = Offset(w * .22f, w * .22f),
        size = Size(w * .56f, w * .56f),
        cornerRadius = CornerRadius(w * .16f),
        style = st
    )
    drawLine(color, Offset(w * .36f, w * .5f), Offset(w * .64f, w * .5f), sw, StrokeCap.Round)
}
