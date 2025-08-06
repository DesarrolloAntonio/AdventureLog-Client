package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomMapMarker(
    color: Color,
    emoji: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = 40.dp, height = 48.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Custom marker shape
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawMarkerShape(
                color = color,
                shadowColor = Color.Black.copy(alpha = 0.3f)
            )
        }
        
        // Inner circle with emoji
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (emoji != null) {
                Text(
                    text = emoji,
                    fontSize = 18.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

private fun DrawScope.drawMarkerShape(
    color: Color,
    shadowColor: Color
) {
    val width = size.width
    val height = size.height
    val radius = width * 0.45f
    val centerX = width / 2f
    val centerY = radius + 2.dp.toPx()
    
    // Draw shadow with smooth path
    val shadowPath = Path().apply {
        val shadowOffset = 2.dp.toPx()
        
        // Start from bottom point
        moveTo(centerX + shadowOffset, height)
        
        // Left curve to circle
        cubicTo(
            centerX - radius * 0.4f + shadowOffset, centerY + radius * 0.8f,
            centerX - radius + shadowOffset, centerY + radius * 0.4f,
            centerX - radius + shadowOffset, centerY
        )
        
        // Top circle arc
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                offset = Offset(centerX - radius + shadowOffset, centerY - radius),
                size = Size(radius * 2, radius * 2)
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        
        // Right curve to bottom
        cubicTo(
            centerX + radius + shadowOffset, centerY + radius * 0.4f,
            centerX + radius * 0.4f + shadowOffset, centerY + radius * 0.8f,
            centerX + shadowOffset, height
        )
        
        close()
    }
    
    drawPath(
        path = shadowPath,
        color = shadowColor,
        style = Fill
    )
    
    // Draw main shape with smooth curves
    val mainPath = Path().apply {
        // Start from bottom point
        moveTo(centerX, height - 4.dp.toPx())
        
        // Left curve to circle
        cubicTo(
            centerX - radius * 0.4f, centerY + radius * 0.8f,
            centerX - radius, centerY + radius * 0.4f,
            centerX - radius, centerY
        )
        
        // Top circle arc
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                offset = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        
        // Right curve to bottom
        cubicTo(
            centerX + radius, centerY + radius * 0.4f,
            centerX + radius * 0.4f, centerY + radius * 0.8f,
            centerX, height - 4.dp.toPx()
        )
        
        close()
    }
    
    drawPath(
        path = mainPath,
        color = color,
        style = Fill
    )
    
    // Draw white border
    drawPath(
        path = mainPath,
        color = Color.White,
        style = Stroke(width = 2.dp.toPx())
    )
}
