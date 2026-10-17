package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float,
    var color: Color
)

@Composable
fun LowPolyBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "lowpoly_loop")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val particles = remember {
        List(35) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.001f,
                vy = -Random.nextFloat() * 0.002f - 0.0005f,
                size = Random.nextFloat() * 3.5f + 1.5f,
                alpha = Random.nextFloat() * 0.5f + 0.2f,
                color = if (Random.nextBoolean()) NeonCyan else NeonPurple
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Subtle 2.5D Isometric Triangular Grid on the horizon
        val rows = 6
        val cols = 10
        val cellW = width / cols
        val cellH = (height * 0.45f) / rows
        val baseY = height * 0.55f

        for (r in 0 until rows) {
            val y1 = baseY + r * cellH
            val y2 = baseY + (r + 1) * cellH
            val alphaRow = (0.04f + (r.toFloat() / rows) * 0.08f)

            for (c in 0 until cols) {
                val x1 = c * cellW
                val x2 = (c + 1) * cellW
                val offsetPhase = (phase * cellW) % cellW

                val path = Path().apply {
                    moveTo(x1 - offsetPhase, y1)
                    lineTo(x2 - offsetPhase, y1)
                    lineTo((x1 + x2) / 2f - offsetPhase, y2)
                    close()
                }

                drawPath(
                    path = path,
                    color = if ((r + c) % 2 == 0) NeonCyan.copy(alpha = alphaRow) else NeonPurple.copy(alpha = alphaRow * 0.7f),
                    style = Stroke(width = 1f)
                )
            }
        }

        // 2. Animated Floating Embers / Sparks (Particle system)
        particles.forEach { p ->
            p.y += p.vy
            p.x += p.vx
            if (p.y < 0f) p.y = 1f
            if (p.x < 0f) p.x = 1f
            if (p.x > 1f) p.x = 0f

            val px = p.x * width
            val py = p.y * height
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(px, py)
            )
        }

        // 3. Vignette gradient to keep UI clean and readable
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.55f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.85f)
                )
            )
        )
    }
}
