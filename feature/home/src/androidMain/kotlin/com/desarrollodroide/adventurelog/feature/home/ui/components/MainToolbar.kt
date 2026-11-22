package com.desarrollodroide.adventurelog.feature.home.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen

object MainToolbarDefaults {
    /** The canvas's pill: 64dp tall, an item 48dp. */
    val Height: Dp = 64.dp
    val ItemSize: Dp = 48.dp

    /** Gap between the toolbar and the gesture bar. */
    val BottomGap: Dp = 24.dp

    /**
     * Bottom padding a list under the toolbar needs so its last item stops above the toolbar,
     * not counting the navigation bar - see [clearance] for the value with it.
     */
    val Clearance: Dp = Height + BottomGap + 16.dp

    /** Where the toolbar sits: [BottomGap] above the navigation bar. */
    val PositionModifier: Modifier = Modifier
        .navigationBarsPadding()
        .padding(bottom = BottomGap)

    /** [Clearance] plus the navigation bar, for content drawn edge to edge. */
    @Composable
    fun clearance(): Dp = Clearance + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
}

/**
 * The phone's five destinations, as the Expressive canvas draws them: a dark pill floating over
 * the content, the current destination a light pill with its name, the others icons alone.
 *
 * It takes the bottom bar's place on a compact window only. On a wider one the rail stays - a
 * toolbar centred under a tablet's content is a long way from either hand.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainToolbar(
    current: CurrentScreen,
    onSelect: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier.height(MainToolbarDefaults.Height),
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = colors.inverseSurface,
            toolbarContentColor = colors.inverseOnSurface,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Destination.entries.forEach { destination ->
                if (destination.screen == current) {
                    SelectedDestination(destination)
                } else {
                    // An icon button is interactive, which the toolbar's first item must be.
                    IconButton(
                        onClick = { onSelect(destination.screen) },
                        modifier = Modifier.size(MainToolbarDefaults.ItemSize),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = colors.inverseOnSurface),
                    ) {
                        Icon(destination.icon, contentDescription = destination.label)
                    }
                }
            }
        }
    }
}

/** The current destination: icon and name on a light pill. Not a button - it is where you are. */
@Composable
private fun SelectedDestination(destination: Destination) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .height(MainToolbarDefaults.ItemSize)
            .widthIn(max = 168.dp)
            .clip(CircleShape)
            .background(colors.primaryContainer)
            .selectable(selected = true, role = Role.Tab, onClick = {})
            .padding(start = 14.dp, end = 18.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            destination.selectedIcon,
            contentDescription = null,
            tint = colors.onPrimaryContainer,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = destination.label,
            color = colors.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
