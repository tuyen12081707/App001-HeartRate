package com.tdev.heartrate.shared.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class BottomBarItem(
    val title: String,
    val icon: ImageVector,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun CustomBottomBar(
    items: List<BottomBarItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomBarItemView(item = item)
            }
        }
    }
}

@Composable
private fun BottomBarItemView(item: BottomBarItem) {
    val scale by animateFloatAsState(
        targetValue = if (item.isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val color by animateColorAsState(
        targetValue = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        label = "color"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (item.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent,
        label = "backgroundColor"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .background(backgroundColor, RoundedCornerShape(100))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = item.onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = color
            )
            AnimatedVisibility(visible = item.isSelected) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        color = color,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
