package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState

/**
 * Drawer header component with gradient background and user information
 */
@Composable
fun DrawerHeader(homeUiState: HomeUiState) {
    // Header with gradient background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
            .semantics {
                contentDescription = "User profile header"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    .padding(4.dp)
                    .clearAndSetSemantics {
                        contentDescription = "User avatar"
                    }
            ) {
                // Here you could load the actual profile picture
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null, // Description already set in the Box
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User information
            if (homeUiState is HomeUiState.Success) {
                val userName = homeUiState.userName
                val adventureCount = homeUiState.recentAdventures.size
                
                Column(modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "User $userName with $adventureCount adventures logged"
                }) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$adventureCount adventures logged",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            } else {
                Text(
                    text = "Adventure Log",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = "Adventure Log app"
                    }
                )
            }
        }
    }
}