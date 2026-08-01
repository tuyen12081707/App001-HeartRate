package com.tdev.heartrate.shared.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tdev.heartrate.shared.domain.model.DashboardPoint
import kotlin.math.max
import kotlin.math.min

@Composable
fun HeartRateChart(
    points: List<DashboardPoint>,
    modifier: Modifier = Modifier
) {
    val surface = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    if (points.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(24.dp)).background(surface),
            contentAlignment = Alignment.Center
        ) { Text("No trend data yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        return
    }

    val values = points.map { it.averageBpm }
    val maxBpm = max(120, values.maxOrNull() ?: 120)
    val minBpm = min(60, values.minOrNull() ?: 60)
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Seven-day trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("BPM", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
            val width = size.width
            val height = size.height
            val step = if (values.size > 1) width / (values.size - 1) else width
            val range = max(1, maxBpm - minBpm).toFloat()
            val coordinates = values.mapIndexed { index, bpm ->
                Offset(index * step, (1f - ((bpm - minBpm) / range)) * height)
            }
            val line = Path().apply {
                moveTo(coordinates.first().x, coordinates.first().y)
                for (index in 0 until coordinates.lastIndex) {
                    val current = coordinates[index]
                    val next = coordinates[index + 1]
                    val midpoint = (current.x + next.x) / 2f
                    cubicTo(midpoint, current.y, midpoint, next.y, next.x, next.y)
                }
            }
            val fill = Path().apply {
                addPath(line)
                lineTo(coordinates.last().x, height)
                lineTo(coordinates.first().x, height)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(primary.copy(alpha = .28f), Color.Transparent)))
            drawPath(line, primary, style = Stroke(width = 4.dp.toPx()))
            coordinates.forEach { point ->
                drawCircle(Color.White, radius = 6.dp.toPx(), center = point)
                drawCircle(primary, radius = 4.dp.toPx(), center = point)
            }
        }
    }
}
