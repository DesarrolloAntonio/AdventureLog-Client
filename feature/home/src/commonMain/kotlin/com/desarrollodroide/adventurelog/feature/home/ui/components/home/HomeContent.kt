package com.desarrollodroide.adventurelog.feature.home.ui.components.home

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.ui.components.AdventureItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Home screen main content composable
 */
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onAdventureClick: (Adventure) -> Unit = {}
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
                    Text("No adventures yet")
                }
            }
            is HomeUiState.Success -> {
                HomeContentSuccess(
                    userName = homeUiState.userName,
                    stats = homeUiState.userStats,
                    recentAdventures = homeUiState.recentAdventures,
                    onOpenDetails = onAdventureClick
                )
            }
        }
    }
}

@Composable
private fun HomeContentSuccess(
    userName: String,
    stats: UserStats,
    recentAdventures: List<Adventure>,
    onOpenDetails: (Adventure) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Stats Card
        SwipeableStatsCard(stats = stats)

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Adventures section title
        Text(
            text = "Recent adventures",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        // Recent Adventures content replaced with Column to avoid nested scrolling issues
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            recentAdventures.forEach { item ->
                AdventureItem(
                    adventure = item,
                    onClick = { onOpenDetails(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeableStatsCard(
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    
    // Auto-alternate between pages
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // Switch every 10 seconds
            scope.launch {
                pagerState.animateScrollToPage(
                    page = (pagerState.currentPage + 1) % 2
                )
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2632) // Dark background color like in the image
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager for swipeable stats pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> {
                        // First page - Adventures and Countries
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CompactStatItem(
                                value = stats.adventureCount,
                                label = "Total adventures",
                                icon = Icons.Default.AirplanemodeActive,
                                iconColor = Color(0xFFE91E63) // Pink color
                            )
                            
                            CompactStatItem(
                                value = stats.visitedCountryCount,
                                label = "Countries visited",
                                icon = Icons.Default.Public,
                                iconColor = Color(0xFF673AB7) // Purple color
                            )
                        }
                    }
                    1 -> {
                        // Second page - Regions and Cities
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CompactStatItem(
                                value = stats.visitedRegionCount,
                                label = "Regions visited",
                                icon = Icons.Default.Terrain,
                                iconColor = Color(0xFF009688) // Teal color
                            )
                            
                            CompactStatItem(
                                value = stats.visitedCityCount,
                                label = "Cities visited",
                                icon = Icons.Default.LocationCity,
                                iconColor = Color(0xFF03A9F4) // Light Blue color
                            )
                        }
                    }
                }
            }
            
            // Page indicator dots
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { index ->
                    val isSelected = pagerState.currentPage == index
                    IndicatorDot(isSelected = isSelected)
                    if (index < 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IndicatorDot(isSelected: Boolean) {
    val color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f)
    val size = if (isSelected) 8.dp else 6.dp
    
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
        // Title in English at the top
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        // Row with icon and value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            // Icon first
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            // Value with color
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = iconColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}