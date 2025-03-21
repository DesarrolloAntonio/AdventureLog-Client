package com.desarrollodroide.adventurelog.feature.home.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import kotlinx.coroutines.delay

/**
 * Home screen main content composable
 */
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (homeUiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${homeUiState.message}")
                }
            }
            is HomeUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data available")
                }
            }
            is HomeUiState.Success -> {
                HomeContentSuccess(
                    userName = homeUiState.userName,
                    stats = homeUiState.userStats
                )
            }
        }
    }
}

@Composable
private fun HomeContentSuccess(
    userName: String,
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome header
        Text(
            text = "Bienvenido de nuevo, $userName!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Stats Card
        CompactStatsCard(stats = stats)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recent Adventures section title
        Text(
            text = "Aventuras recientes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Recent Adventures content will go here
    }
}

@Composable
private fun CompactStatsCard(
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    // Auto-alternate between two groups of stats - declaring at this level
    var showFirstGroup by remember { mutableStateOf(true) }
    
    // Animate the transition
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Switch every 5 seconds
            showFirstGroup = !showFirstGroup
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2632) // Dark background color like in the image
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main content area for stats
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                // Stats display depending on current state
                if (showFirstGroup) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CompactStatItem(
                            value = stats.adventureCount,
                            label = "Aventuras totales",
                            icon = Icons.Default.AirplanemodeActive,
                            iconColor = Color(0xFFE91E63) // Pink color
                        )
                        
                        CompactStatItem(
                            value = stats.visitedCountryCount,
                            label = "Países visitados",
                            icon = Icons.Default.Public,
                            iconColor = Color(0xFF673AB7) // Purple color
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CompactStatItem(
                            value = stats.visitedRegionCount,
                            label = "Regiones visitadas",
                            icon = Icons.Default.Terrain,
                            iconColor = Color(0xFF009688) // Teal color
                        )
                        
                        CompactStatItem(
                            value = stats.visitedCityCount,
                            label = "Ciudades visitadas",
                            icon = Icons.Default.LocationCity,
                            iconColor = Color(0xFF03A9F4) // Light Blue color
                        )
                    }
                }
            }
            
            // Vertical indicator dots on the right side
            Column(
                modifier = Modifier.padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VerticalIndicatorDot(active = showFirstGroup)
                VerticalIndicatorDot(active = !showFirstGroup)
            }
        }
    }
}

@Composable
private fun VerticalIndicatorDot(active: Boolean) {
    val color = if (active) Color.White else Color.White.copy(alpha = 0.3f)
    val size = if (active) 8.dp else 6.dp
    
    Box(
        modifier = Modifier
            .size(size)
            .background(color = color, shape = CircleShape)
    )
}

@Composable
private fun CompactStatItem(
    value: Int,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        // Value with accent color
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            color = iconColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        // Icon below value
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        
        // Label text
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}