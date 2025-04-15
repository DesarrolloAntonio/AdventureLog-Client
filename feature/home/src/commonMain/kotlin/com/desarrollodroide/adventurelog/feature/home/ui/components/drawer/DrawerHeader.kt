package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.home.ui.components.common.ProfileAvatar
import kotlin.random.Random

/**
 * The header component for the drawer showing user profile info
 */
@Composable
fun DrawerHeader(
    userName: String,
    email: String?,
    profileImageUrl: String?,
    visible: Boolean,
    onLogout: () -> Unit
) {
    // Animation for header
    val headerOffsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else (-20).dp,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = 50
        ),
        label = "headerOffsetY"
    )
    
    val headerAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = 50
        ),
        label = "headerAlpha"
    )
    
    // Header colors - matching the website dark theme
    val darkSlate = Color(0xFF1A1E25)  // Darker background matching website
    val slateAccent = Color(0xFF252A35) // Slightly lighter accent
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = headerOffsetY.toPx()
                alpha = headerAlpha
            }
            .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(darkSlate, slateAccent),
                    startY = 0f,
                    endY = 300f
                )
            )
            .drawBehind {
                // Create star/dot pattern to simulate night sky
                val starsColor = Color.White.copy(alpha = 0.2f)
                
                // Generate larger stars (less numerous)
                repeat(15) {
                    val x = (Random.nextDouble() * size.width).toFloat()
                    val y = (Random.nextDouble() * size.height).toFloat()
                    val starSize = (1f + Random.nextDouble() * 2f).toFloat()
                    
                    drawCircle(
                        color = starsColor,
                        radius = starSize,
                        center = Offset(x, y)
                    )
                }
                
                // Generate smaller stars (more numerous)
                repeat(30) {
                    val x = (Random.nextDouble() * size.width).toFloat()
                    val y = (Random.nextDouble() * size.height).toFloat()
                    val starSize = (0.5f + Random.nextDouble() * 1f).toFloat()
                    
                    drawCircle(
                        color = starsColor.copy(alpha = 0.1f),
                        radius = starSize,
                        center = Offset(x, y)
                    )
                }
                
                // Add purple glow at the top - matching website's purple accents
                val glowBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF5038FF).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2, 0f),
                    radius = size.width * 0.6f
                )
                
                drawRect(
                    brush = glowBrush,
                    size = size
                )
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // User avatar with glowing border
            Box(
                modifier = Modifier
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF5038FF).copy(alpha = 0.2f), // Purple glow matching website
                            radius = size.width / 1.8f,
                            center = center
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatar(
                    size = 80,
                    profileImageUrl = profileImageUrl
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User name
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White // White text
            )
            
            // Email
            if (email != null) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f) // Semi-transparent white text
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout button
            TextButton(
                onClick = onLogout,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(
                        Color(0xFF4F0E0E).copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFFF4081) // Pink-purple accent matching website
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    color = Color(0xFFFF4081), // Pink-purple accent matching website
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}