package com.example.test

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val heart_minus: ImageVector
  get() {
    if (_heart_minus != null) {
      return _heart_minus!!
    }
    _heart_minus =
      ImageVector.Builder(
          name = "heart_minus",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(11f, 11.48f)
            close()
            moveTo(11f, 21f)
            lineTo(7.83f, 18.15f)
            quadTo(6.03f, 16.52f, 4.74f, 15.25f)
            reflectiveQuadTo(2.61f, 12.85f)
            reflectiveQuadTo(1.39f, 10.68f)
            quadTo(1f, 9.63f, 1f, 8.48f)
            quadTo(1f, 6.13f, 2.58f, 4.56f)
            reflectiveQuadTo(6.5f, 3f)
            quadTo(7.8f, 3f, 8.98f, 3.55f)
            reflectiveQuadTo(11f, 5.1f)
            quadToRelative(0.85f, -1f, 2.03f, -1.55f)
            reflectiveQuadTo(15.5f, 3f)
            quadToRelative(2.1f, 0f, 3.83f, 1.47f)
            reflectiveQuadToRelative(1.72f, 4f)
            quadToRelative(0f, 0.35f, -0.05f, 0.74f)
            reflectiveQuadTo(20.85f, 10f)
            quadToRelative(0f, 0f, -0.34f, 0f)
            reflectiveQuadToRelative(-0.73f, 0f)
            reflectiveQuadToRelative(-0.73f, 0f)
            quadToRelative(-0.34f, 0f, -0.34f, 0f)
            quadToRelative(0.13f, -0.45f, 0.2f, -0.85f)
            reflectiveQuadTo(19f, 8.4f)
            quadTo(19f, 6.52f, 17.75f, 5.76f)
            reflectiveQuadTo(15.5f, 5f)
            quadTo(14.23f, 5f, 13.3f, 5.69f)
            reflectiveQuadTo(11.58f, 7.5f)
            horizontalLineTo(10.43f)
            quadTo(9.65f, 6.38f, 8.66f, 5.69f)
            quadTo(7.68f, 5f, 6.5f, 5f)
            quadTo(5.08f, 5f, 4.04f, 5.99f)
            quadTo(3f, 6.97f, 3f, 8.48f)
            quadTo(3f, 9.3f, 3.35f, 10.15f)
            reflectiveQuadTo(4.6f, 12.11f)
            quadToRelative(0.9f, 1.11f, 2.45f, 2.6f)
            reflectiveQuadTo(11f, 18.3f)
            quadToRelative(0.65f, -0.57f, 1.53f, -1.32f)
            reflectiveQuadToRelative(1.4f, -1.25f)
            quadToRelative(0f, 0f, 0.22f, 0.22f)
            quadToRelative(0.22f, 0.23f, 0.49f, 0.49f)
            reflectiveQuadToRelative(0.49f, 0.49f)
            reflectiveQuadToRelative(0.23f, 0.22f)
            quadToRelative(-0.55f, 0.5f, -1.4f, 1.24f)
            reflectiveQuadToRelative(-1.5f, 1.31f)
            lineTo(11f, 21f)
            close()
            moveToRelative(4f, -7f)
            verticalLineTo(12f)
            horizontalLineToRelative(8f)
            verticalLineToRelative(2f)
            horizontalLineTo(15f)
            close()
          }
        }
        .build()
    return _heart_minus!!
  }

private var _heart_minus: ImageVector? = null
