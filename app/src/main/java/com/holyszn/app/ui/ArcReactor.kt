package com.holyszn.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/** Animated HOLYCORE emblem: rotating arcs and a pulsing core. Red when armed, chrome when not. */
@Composable
fun ArcReactor(armed: Boolean, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "reactor")
    val a1 by t.animateFloat(
        0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "a1"
    )
    val a2 by t.animateFloat(
        360f, 0f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "a2"
    )
    val pulse by t.animateFloat(
        0.55f, 1f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val col = if (armed) Red else Chrome2

    Canvas(modifier.size(190.dp)) {
        val c = center
        val r = size.minDimension / 2f
        for (i in 0 until 36) {
            rotate(i * 10f, c) {
                val len = if (i % 3 == 0) 10.dp.toPx() else 5.dp.toPx()
                drawLine(ChromeDark, Offset(c.x, c.y - r), Offset(c.x, c.y - r + len), strokeWidth = 1.5f)
            }
        }
        rotate(a1, c) {
            for (i in 0 until 3) {
                drawArc(
                    col, startAngle = i * 120f + 10f, sweepAngle = 80f, useCenter = false,
                    topLeft = Offset(c.x - r * 0.86f, c.y - r * 0.86f),
                    size = Size(r * 1.72f, r * 1.72f),
                    style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        rotate(a2, c) {
            for (i in 0 until 3) {
                drawArc(
                    Chrome2, startAngle = i * 120f + 40f, sweepAngle = 50f, useCenter = false,
                    topLeft = Offset(c.x - r * 0.62f, c.y - r * 0.62f),
                    size = Size(r * 1.24f, r * 1.24f),
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        drawCircle(
            Brush.radialGradient(
                listOf(col.copy(alpha = 0.55f * pulse), Color.Transparent),
                center = c, radius = r * 0.5f
            ),
            radius = r * 0.5f, center = c
        )
        drawCircle(col, radius = r * 0.13f * (0.9f + 0.1f * pulse), center = c)
    }
}
