package com.desarrollodroide.adventurelog.feature.settings.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.PublicUser
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileState
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.UsersViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.components.SimpleSearchBar
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import org.koin.compose.viewmodel.koinViewModel

/**
 * Everyone on this server with a public profile - the web's Usuarios section.
 *
 * The call was already here: collection sharing uses the same list to decide who a collection can
 * be shared with. Nothing had ever shown it on its own.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UsersScreen(
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingIndicator(Modifier.align(Alignment.Center))

            state.error != null -> StateMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Couldn't load people",
                body = state.error.orEmpty(),
                badgeColor = MaterialTheme.colorScheme.errorContainer,
                badgeContent = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.fillMaxSize().padding(32.dp)
            ) {
                Button(onClick = viewModel::load, shapes = ButtonDefaults.shapes()) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Try again", modifier = Modifier.padding(start = 8.dp))
                }
            }

            else -> ContentColumn {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = if (state.users.size == 1) {
                                "1 public profile"
                            } else {
                                "${state.users.size} public profiles"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (state.users.size > 8) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SimpleSearchBar(
                                searchQuery = state.query,
                                onSearchQueryChange = viewModel::onQueryChange,
                                placeholder = "Search people",
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            )
                        }
                    }

                    if (state.users.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) { NobodyYet() }
                    } else {
                        val people = state.filtered
                        items(people.size, key = { people[it].uuid }) { index ->
                            UserCard(
                                people[index],
                                shape = SegmentedListDefaults.shapeFor(index, people.size),
                                onClick = { viewModel.openProfile(people[index]) }
                            )
                        }
                    }
                }
            }
        }
    }

    state.profile?.let { profile ->
        ProfileSheet(
            profile = profile,
            onRetry = viewModel::retryProfile,
            onDismiss = viewModel::closeProfile
        )
    }
}

@Composable
private fun NobodyYet() {
    StateMessage(
        icon = Icons.Outlined.Group,
        title = "Nobody has a public profile yet.",
        body = "When other travellers make their profile public, they show up here.",
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp)
    )
}

/** An empty or failed screen: its icon on a cookie, what happened, and what to do about it. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StateMessage(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    badgeColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    badgeContent: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(88.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = badgeContent, modifier = Modifier.size(40.dp))
        }
        Text(text = title, style = AdventureType.display(28.sp), textAlign = TextAlign.Center)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Box(Modifier.padding(top = 4.dp)) { action() }
        }
    }
}

@Composable
private fun UserCard(user: PublicUser, shape: Shape, onClick: () -> Unit) {
    // A row of one segmented group, as the canvas draws People, in the settings rows' colour:
    // white against the daylight backdrop, one step up from black against the dark one.
    val scheme = MaterialTheme.colorScheme
    val rowColor = if (scheme.background.luminance() < 0.5f) {
        scheme.surfaceContainerLow
    } else {
        scheme.surfaceContainerLowest
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = rowColor
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 72.dp)
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Avatar(user, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun Avatar(user: PublicUser, size: Dp) {
    val imageLoader = LocalImageLoader.current
    val photo = user.profilePic?.takeIf { it.isNotBlank() }
    if (photo != null) {
        Image(
            painter = rememberAsyncImagePainter(model = photo, imageLoader = imageLoader),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape)
        )
    } else {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(size)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = user.displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Someone's public profile: who they are and their numbers, which is how the web's profile page
 * opens. Their public places and collections, the page's second half, are not here yet - each would
 * need a way to open another person's record, which the app does not have.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileSheet(profile: ProfileState, onRetry: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Avatar(profile.person, size = 64.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = profile.person.displayName,
                        style = AdventureType.display(30.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${profile.person.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val stats = profile.stats
            when {
                profile.isLoading -> Box(
                    Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }

                profile.error != null -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = profile.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(onClick = onRetry, shapes = ButtonDefaults.shapes()) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Try again", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                stats != null -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileNumber("Places", stats.locationCount.toString(), Modifier.weight(1f))
                        ProfileNumber("Visited", stats.visitedLocationCount.toString(), Modifier.weight(1f))
                        ProfileNumber("Trips", stats.tripsCount.toString(), Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileNumber("Countries", stats.visitedCountryCount.toString(), Modifier.weight(1f))
                        ProfileNumber("Regions", stats.visitedRegionCount.toString(), Modifier.weight(1f))
                        ProfileNumber("Cities", stats.visitedCityCount.toString(), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileNumber(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = AdventureType.display(if (value.length > 3) 24.sp else 30.sp), maxLines = 1)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

