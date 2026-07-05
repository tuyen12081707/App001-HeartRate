package com.tdev.heartrate.shared.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun HeartRateChart(
    dataPoints: List<Int>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("Chưa có dữ liệu. Hãy đo nhịp tim!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxBpm = max(120, dataPoints.maxOrNull() ?: 120)
    val minBpm = min(60, dataPoints.minOrNull() ?: 60)
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val gradientColor = primaryColor.copy(alpha = 0.3f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Text(
            text = "Xu Hướng Nhịp Tim",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        val path = remember { Path() }
        val fillPath = remember { Path() }
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val width = size.width
            val height = size.height
            val xStep = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width
            val range = max(1, maxBpm - minBpm).toFloat()

            path.reset()
            fillPath.reset()
            
            val points = dataPoints.mapIndexed { index, bpm ->
                val x = index * xStep
                // Y axis is flipped
                val normalizedY = 1f - ((bpm - minBpm) / range)
                val y = normalizedY * height
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y)
                fillPath.moveTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val current = points[i]
                    val next = points[i + 1]
                    
                    // Bezier curve calculation
                    val controlPoint1 = Offset((current.x + next.x) / 2f, current.y)
                    val controlPoint2 = Offset((current.x + next.x) / 2f, next.y)
                    
                    path.cubicTo(
                        controlPoint1.x, controlPoint1.y,
                        controlPoint2.x, controlPoint2.y,
                        next.x, next.y
                    )
                    
                    fillPath.cubicTo(
                        controlPoint1.x, controlPoint1.y,
                        controlPoint2.x, controlPoint2.y,
                        next.x, next.y
                    )
                }

                // Draw gradient under line
                fillPath.lineTo(points.last().x, height)
                fillPath.lineTo(points.first().x, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(gradientColor, Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw solid line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 4.dp.toPx())
                )
                
                // Draw dots
                points.forEach { point ->
                    drawCircle(
                        color = primaryColor,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }
    }
}
