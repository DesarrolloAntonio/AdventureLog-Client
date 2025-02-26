package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.feature.home.ui.components.adventures.AdventureCard
import com.desarrollodroide.adventurelog.feature.home.ui.components.common.QuickActionsRow
import com.desarrollodroide.adventurelog.feature.home.ui.components.common.SectionHeader
import com.desarrollodroide.adventurelog.feature.home.ui.components.common.WelcomeHeader
import com.desarrollodroide.adventurelog.feature.home.ui.components.stats.QuickStatsRow


/**
 * Main content of the home screen when in success state
 */
@Composable
fun HomeContent(
    userName: String,
    adventures: List<Adventure>
) {
    val visibleState = remember {
        MutableTransitionState(false).apply {
            // Start the animation
            targetState = true
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(
            initialAlpha = 0.3f,
            animationSpec = tween(durationMillis = 500)
        ) + slideInVertically(
            initialOffsetY = { 100 },
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Personalized greeting
            item {
                WelcomeHeader(userName = userName)
            }

            // Quick stats
            item {
                QuickStatsRow(adventureCount = adventures.size)
            }

            // Recent adventures title
            item {
                SectionHeader(title = "Your recent adventures")
            }

            // Recent adventures list
            if (adventures.isEmpty()) {
                item {
                    Text(
                        text = "You don't have any recent adventures",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                itemsIndexed(adventures) { index, adventure ->
                    val animatedElevation by animateDpAsState(
                        targetValue = if (index % 2 == 0) 4.dp else 2.dp,
                        animationSpec = tween(durationMillis = 500),
                        label = "cardElevation"
                    )

                    AdventureCard(
                        adventure = adventure,
                        elevation = animatedElevation,
                        onClick = { /* Open adventure details */ }
                    )
                }
            }

            // Separator
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick actions
            item {
                SectionHeader(title = "Quick actions")
            }

            item {
                QuickActionsRow()
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}