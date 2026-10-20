package com.chat.aoita.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp

/** 让内容在顶部 / 底部边缘渐渐淡出（高度为 0 则不淡出）。 */
fun Modifier.fadingEdges(top: Dp, bottom: Dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val t = top.toPx()
        val b = bottom.toPx()
        if (t > 0f) {
            drawRect(
                Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black, startY = 0f, endY = t),
                size = Size(size.width, t),
                blendMode = BlendMode.DstIn
            )
        }
        if (b > 0f) {
            drawRect(
                Brush.verticalGradient(0f to Color.Black, 1f to Color.Transparent, startY = size.height - b, endY = size.height),
                topLeft = Offset(0f, size.height - b),
                size = Size(size.width, b),
                blendMode = BlendMode.DstIn
            )
        }
    }
