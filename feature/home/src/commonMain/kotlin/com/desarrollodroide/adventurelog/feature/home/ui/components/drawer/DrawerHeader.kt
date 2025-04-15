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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.home.ui.components.common.ProfileAvatar

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
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = headerOffsetY.toPx()
                alpha = headerAlpha
            }
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // User avatar
            ProfileAvatar(
                size = 80,
                profileImageUrl = profileImageUrl
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User name
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // Email
            if (email != null) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout button
            TextButton(
                onClick = onLogout,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    color = Color(0xFFE53935),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}