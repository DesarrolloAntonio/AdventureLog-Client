package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModernMapMarker(
    color: Color,
    emoji: String? = null,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = 48.dp, height = 56.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .offset(y = 2.dp)
                .size(width = 44.dp, height = 52.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = 22.dp,
                    topEnd = 22.dp,
                    bottomStart = 22.dp,
                    bottomEnd = 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.2f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {}
        }
        
        // Main marker card
        Card(
            modifier = Modifier.size(width = 44.dp, height = 52.dp),
            shape = RoundedCornerShape(
                topStart = 22.dp,
                topEnd = 22.dp,
                bottomStart = 22.dp,
                bottomEnd = 2.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = color
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 8.dp else 4.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                // White circle for emoji/icon
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        emoji != null -> {
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom pointer triangle
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-2).dp)
                .size(12.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}
