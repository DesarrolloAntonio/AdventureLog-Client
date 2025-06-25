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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.desarrollodroide.adventurelog.feature.ui.components.LoadingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onAdventureClick: (Adventure) -> Unit = { },
    sessionToken: String = ""
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        when (homeUiState) {
            is HomeUiState.Loading -> {
                LoadingDialog(
                    isLoading = true,
                    showMessage = false
                )
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
                    onAdventureClick = onAdventureClick,
                    sessionToken = sessionToken
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
    onAdventureClick: (Adventure) -> Unit = { },
    sessionToken: String = "",
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 80.dp // Extra padding for last item visibility while allowing overscroll
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SwipeableStatsCard(stats = stats)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = "Recent adventures",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(recentAdventures) { adventure ->
            AdventureItem(
                adventure = adventure,
                onClick = { onAdventureClick(adventure) },
                sessionToken = sessionToken
            )
        }
    }
}

@Composable
private fun SwipeableStatsCard(
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> {
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            
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