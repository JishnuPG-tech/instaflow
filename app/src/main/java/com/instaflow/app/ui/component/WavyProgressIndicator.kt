package com.instaflow.app.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularWavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp,
    waveCount: Int = 8,
    waveAmplitude: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_progress")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.size(48.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val ampPx = waveAmplitude.toPx()
        val strokePx = strokeWidth.toPx()

        val path = Path()
        val points = 120
        for (i in 0..points) {
            val angle = (2 * PI * i / points).toFloat()
            val wave = sin(angle * waveCount + phase) * ampPx
            val r = radius - strokePx / 2f + wave
            
            val x = center.x + r * cos(angle)
            val y = center.y + r * sin(angle)
            
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}
