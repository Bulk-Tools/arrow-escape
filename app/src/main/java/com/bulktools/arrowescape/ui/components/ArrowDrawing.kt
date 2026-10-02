package com.bulktools.arrowescape.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.bulktools.arrowescape.engine.Arrow
import com.bulktools.arrowescape.engine.Special
import com.bulktools.arrowescape.theme.ThemeDef

private val IcyBlue = Color(0xFFBDE0FE)

internal fun drawArrowRaw(
    scope: DrawScope,
    arrow: Arrow,
    def: ThemeDef,
    center: Offset,
    sizePx: Float,
    alpha: Float
) {
    val baseColor = when {
        arrow.frozen -> IcyBlue
        arrow.special == Special.GOLDEN -> def.gold
        arrow.special == Special.BOMB -> def.accent
        else -> def.arrow
    }
    if (arrow.frozen) {
        val half = sizePx * 0.62f
        scope.drawRoundRect(
            color = IcyBlue.copy(alpha = 0.22f * alpha),
            topLeft = center - Offset(half, half),
            size = Size(half * 2f, half * 2f),
            cornerRadius = CornerRadius(sizePx * 0.3f, sizePx * 0.3f)
        )
    }
    scope.rotate(arrow.direction.angleDeg, center) {
        drawLine(
            color = baseColor.copy(alpha = alpha),
            start = center + Offset(-0.32f * sizePx, 0f),
            end = center + Offset(0.18f * sizePx, 0f),
            strokeWidth = 0.16f * sizePx,
            cap = StrokeCap.Round
        )
        val head = Path().apply {
            moveTo(center.x + 0.46f * sizePx, center.y)
            lineTo(center.x + 0.10f * sizePx, center.y - 0.24f * sizePx)
            lineTo(center.x + 0.10f * sizePx, center.y + 0.24f * sizePx)
            close()
        }
        drawPath(head, baseColor.copy(alpha = alpha))
        if (arrow.frozen) {
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 0.09f * sizePx,
                center = center + Offset(-0.26f * sizePx, -0.26f * sizePx)
            )
        }
    }
}

@Composable
fun DrawArrow(arrow: Arrow, def: ThemeDef, drawScope: DrawScope, center: Offset, sizePx: Float) {
    drawArrowRaw(drawScope, arrow, def, center, sizePx, 1f)
}
