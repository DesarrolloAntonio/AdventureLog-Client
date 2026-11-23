package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.graphics.shapes.RoundedPolygon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * A centred message that the pull-to-refresh around it can still be pulled over.
 *
 * `PullToRefreshBox` only hears a drag that a scrollable child passes up to it, and a plain `Box`
 * is not one - so an empty list could not be refreshed at all. That is exactly when refreshing
 * matters: measured with a new account whose Collections said "No collections yet" while an
 * invitation waited on the server, and no pull brought it in (QA 09).
 */
@Composable
fun PullableStateBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(32.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Composable
fun NoSearchResultsState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    StateMessage(
        icon = Icons.Default.SearchOff,
        container = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
        title = "No results found",
        body = "No results match \"$searchQuery\"",
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    StateMessage(
        icon = Icons.Default.SentimentDissatisfied,
        container = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
        title = "Something went wrong",
        body = message,
        modifier = modifier,
    ) {
        if (onRetry != null) {
            Spacer(Modifier.height(4.dp))
            Button(onClick = onRetry, shapes = ButtonDefaults.shapes(), modifier = Modifier.height(48.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Try Again", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

/** An empty or error state: a cookie badge, a title in the display voice, a line that explains. */
@Composable
private fun StateMessage(
    icon: ImageVector,
    container: Color,
    content: Color,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CookieBadge(icon = icon, container = container, content = content, size = 96.dp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = AdventureType.display(28.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            action()
        }
    }
}

/**
 * An icon on a cookie in a tonal colour: the badge of the empty and error states and of the
 * dialogs that ask before something is lost.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CookieBadge(
    icon: ImageVector,
    container: Color,
    content: Color,
    size: Dp,
    polygon: RoundedPolygon = MaterialShapes.Cookie9Sided,
) {
    Box(
        Modifier
            .size(size)
            .clip(polygon.toShape())
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(size * 0.45f))
    }
}
